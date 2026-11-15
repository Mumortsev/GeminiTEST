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
import ru.withbaby.domain.*

data class Snapshot(val version: Int, val settings: Settings, val children: List<Child>, val trips: List<Trip>, val items: List<PackingItem>, val bags: List<Bag>, val stock: List<Stock>, val learning: List<Learning>)
object Backup {
 private val magic="BABY01".toByteArray()
 private fun key(password: CharArray,salt: ByteArray): SecretKeySpec {
  val spec=PBEKeySpec(password,salt,210000,256)
  return try { SecretKeySpec(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded,"AES") } finally { spec.clearPassword() }
 }
 suspend fun export(repo: Repository,password: CharArray): ByteArray {
  require(password.size >= 8) { "Минимум 8 символов в пароле" }
  val snapshot=repo.db.withTransaction { Snapshot(1,repo.preferences.settings.first(),repo.dao.children().first(),repo.dao.trips().first(),repo.dao.items().first(),repo.dao.bags().first(),repo.dao.stock().first(),repo.dao.allLearning()) }
  val salt=ByteArray(16).also { SecureRandom().nextBytes(it) }; val iv=ByteArray(12).also { SecureRandom().nextBytes(it) }
  val cipher=Cipher.getInstance("AES/GCM/NoPadding"); cipher.init(Cipher.ENCRYPT_MODE,key(password,salt),GCMParameterSpec(128,iv)); cipher.updateAAD(magic)
  return magic+salt+iv+cipher.doFinal(repo.gson.toJson(snapshot).toByteArray(Charsets.UTF_8))
 }
 suspend fun restore(repo: Repository,bytes: ByteArray,password: CharArray) {
  require(bytes.size in 50..10_000_000 && bytes.take(6).toByteArray().contentEquals(magic)) { "Неподдерживаемый файл копии" }
  val cipher=Cipher.getInstance("AES/GCM/NoPadding"); cipher.init(Cipher.DECRYPT_MODE,key(password,bytes.copyOfRange(6,22)),GCMParameterSpec(128,bytes.copyOfRange(22,34))); cipher.updateAAD(magic)
  val s=repo.gson.fromJson(String(cipher.doFinal(bytes.copyOfRange(34,bytes.size)),Charsets.UTF_8),Snapshot::class.java)
  require(s.version==1 && s.children.size<=100 && s.trips.size<=10000 && s.items.size<=100000 && s.stock.size<=10000)
  require(s.children.map { it.id }.distinct().size==s.children.size)
  s.children.forEach { require(it.name.isNotBlank() && !LocalDate.parse(it.birthday).isAfter(LocalDate.now())); Feeding.valueOf(it.feeding) }
  val childIds=s.children.map { it.id }.toSet(); val tripIds=s.trips.map { it.id }.toSet();val bagIds=s.bags.map { it.id }.toSet()
  s.trips.forEach { require(it.childId in childIds && it.hours in 1..336); Scenario.valueOf(it.scenario); Transport.valueOf(it.transport) }
  s.items.forEach { require(it.tripId in tripIds && it.count in 1..500 && it.name.isNotBlank() && it.feedback in -1..1) }
  s.stock.forEach { require(it.bagId in bagIds && it.count in 0..9999 && it.minimum in 0..9999) }
  require(s.settings.theme in listOf("Системная","Светлая","Тёмная") && s.settings.lat in -90.0..90.0 && s.settings.lon in -180.0..180.0)
  repo.db.withTransaction {
   repo.dao.clearTrips();repo.dao.clearChildren();repo.dao.clearBags();repo.dao.clearLearning();repo.dao.clearWeather()
   s.children.forEach { repo.dao.child(it) };s.trips.forEach { repo.dao.trip(it) };repo.dao.items(s.items)
   s.bags.forEach { repo.dao.bag(it) };s.stock.forEach { repo.dao.stock(it) };s.learning.filter { it.childId in childIds }.forEach { repo.dao.learning(it) }
  }
  repo.preferences.save(s.settings.copy(notifications=false))
 }
}
