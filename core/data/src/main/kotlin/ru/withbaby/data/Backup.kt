package ru.withbaby.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.first
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom
import java.time.LocalDate
import android.util.Base64
import ru.withbaby.domain.*

data class PhotoEntry(val name:String,val data:String)
data class Snapshot(val version:Int,val settings:Settings,val children:List<Child>,val trips:List<Trip>,val items:List<PackingItem>,val bags:List<Bag>,val stock:List<Stock>,val learning:List<Learning>,val wardrobe:List<WardrobeItem>?=null,val choices:List<OutfitChoice>?=null,val photos:List<PhotoEntry>?=null)
object Backup {
 const val MAX_BYTES=32_000_000
 private val magic="BABY01".toByteArray()
 private fun key(password:CharArray,salt:ByteArray):SecretKeySpec {
  val spec=PBEKeySpec(password,salt,210000,256)
  return try {SecretKeySpec(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded,"AES")} finally {spec.clearPassword()}
 }
 suspend fun export(repo:Repository,password:CharArray):ByteArray {
  require(password.size>=8)
  var s=repo.db.withTransaction {Snapshot(2,repo.preferences.settings.first(),repo.dao.children().first(),repo.dao.trips().first(),repo.dao.items().first(),repo.dao.bags().first(),repo.dao.stock().first(),repo.dao.allLearning(),repo.dao.wardrobe().first(),repo.dao.outfitChoices().first())}
  val names=(s.items.mapNotNull {it.photo}+s.stock.mapNotNull {it.photo}+s.wardrobe.orEmpty().mapNotNull {it.photo}).distinct()
  require(names.sumOf {repo.photos.file(it)?.length() ?: 0L}<=20_000_000)
  s=s.copy(photos=names.map {name->val f=repo.photos.file(name);require(f!=null && f.exists());PhotoEntry(name,Base64.encodeToString(f.readBytes(),Base64.NO_WRAP))})
  val plain=repo.gson.toJson(s).toByteArray(Charsets.UTF_8);require(plain.size<MAX_BYTES-100)
  val salt=ByteArray(16).also {SecureRandom().nextBytes(it)};val iv=ByteArray(12).also {SecureRandom().nextBytes(it)}
  val cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,key(password,salt),GCMParameterSpec(128,iv));cipher.updateAAD(magic)
  return magic+salt+iv+cipher.doFinal(plain)
 }
 suspend fun restore(repo:Repository,bytes:ByteArray,password:CharArray) {
  require(bytes.size in 50..MAX_BYTES && bytes.take(6).toByteArray().contentEquals(magic))
  val cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.DECRYPT_MODE,key(password,bytes.copyOfRange(6,22)),GCMParameterSpec(128,bytes.copyOfRange(22,34)));cipher.updateAAD(magic)
  val s=repo.gson.fromJson(String(cipher.doFinal(bytes.copyOfRange(34,bytes.size)),Charsets.UTF_8),Snapshot::class.java)
  require(s.version in 1..2 && s.children.size<=100 && s.trips.size<=10000 && s.items.size<=100000 && s.stock.size<=10000)
  require(s.children.map {it.id}.distinct().size==s.children.size)
  s.children.forEach {require(it.name.isNotBlank() && !LocalDate.parse(it.birthday).isAfter(LocalDate.now()));Feeding.valueOf(it.feeding)}
  val childIds=s.children.map {it.id}.toSet();val tripIds=s.trips.map {it.id}.toSet();val bagIds=s.bags.map {it.id}.toSet()
  s.trips.forEach {require(it.childId in childIds && it.hours in 1..1440);Scenario.valueOf(it.scenario);Transport.valueOf(it.transport);require(it.returnAt==0L || it.returnAt>it.departure && it.returnAt-it.departure<=1440*3600_000L && kotlin.math.ceil((it.returnAt-it.departure)/3600_000.0).toInt()==it.hours)}
  s.items.forEach {require(it.tripId in tripIds && it.count in 1..500 && it.name.isNotBlank() && it.feedback in -1..1)}
  s.stock.forEach {require(it.bagId in bagIds && it.count in 0..9999 && it.minimum in 0..9999)}
  val wardrobe=s.wardrobe.orEmpty();val choices=s.choices.orEmpty()
  require(wardrobe.size<=10000 && choices.size<=2000 && s.photos.orEmpty().size<=1000)
  wardrobe.forEach {require(it.childId in childIds && it.name.isNotBlank() && it.warmth in 0..3);GarmentGroup.valueOf(it.group)}
  val validRefs=wardrobe.map {"owned:"+it.id}.toSet()+GarmentCatalog.all.map {"catalog:"+it.id}
  choices.forEach {require(it.childId in childIds && it.slot in GarmentGroup.entries.map {g->g.name} && (it.reference.isBlank() || it.reference=="none" || it.reference in validRefs));if(it.reference.startsWith("owned:")) require(wardrobe.any {w->w.id==it.reference.removePrefix("owned:") && w.childId==it.childId})}
  require(s.settings.theme in listOf("Системная","Светлая","Тёмная") && s.settings.lat in -90.0..90.0 && s.settings.lon in -180.0..180.0)
  val needed=(s.items.mapNotNull {it.photo}+s.stock.mapNotNull {it.photo}+wardrobe.mapNotNull {it.photo}).toSet()
  val entries=s.photos.orEmpty();require(entries.map {it.name}.distinct().size==entries.size && needed.all {n->entries.any {it.name==n}})
  val decoded=entries.associate {p->require(repo.photos.file(p.name)!=null);val data=Base64.decode(p.data,Base64.NO_WRAP);repo.photos.validate(data);p.name to data}
  require(decoded.values.sumOf {it.size.toLong()}<=20_000_000)
  val mapping=mutableMapOf<String,String>()
  try {
   decoded.forEach {(old,data)->mapping[old]=repo.photos.put(data)}
   repo.db.withTransaction {
    repo.dao.clearChoices();repo.dao.clearWardrobe();repo.dao.clearTrips();repo.dao.clearChildren();repo.dao.clearBags();repo.dao.clearLearning();repo.dao.clearWeather()
    s.children.forEach {repo.dao.child(it)};s.trips.forEach {repo.dao.trip(it)};repo.dao.items(s.items.map {it.copy(photo=it.photo?.let(mapping::get))})
    s.bags.forEach {repo.dao.bag(it)};s.stock.forEach {repo.dao.stock(it.copy(photo=it.photo?.let(mapping::get)))};s.learning.filter {it.childId in childIds}.forEach {repo.dao.learning(it)}
    wardrobe.forEach {repo.dao.wardrobe(it.copy(photo=it.photo?.let(mapping::get)))};choices.forEach {repo.dao.choice(it)}
   }
  } catch(e:Exception) {mapping.values.forEach {repo.photos.file(it)?.delete()};throw e}
  repo.preferences.save(s.settings.copy(notifications=false,palette=s.settings.palette?.takeIf {it in listOf("blue","pink")} ?: "blue"))
  repo.cleanupPhotos()
 }
}
