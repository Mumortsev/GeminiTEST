package ru.withbaby.data

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import ru.withbaby.domain.*
import java.time.LocalDate
import java.util.UUID

fun newId(): String = UUID.randomUUID().toString()
class Repository(context: Context) {
 val appContext=context.applicationContext
 val photos=PhotoStore(appContext)
 val db = Room.databaseBuilder(context.applicationContext, BabyDatabase::class.java, "with-baby.db").addMigrations(MIGRATION_1_2).enableMultiInstanceInvalidation().build()
 val dao = db.dao()
 val preferences = Preferences(context.applicationContext)
 val gson = Gson()
 suspend fun createTrip(child: Child, scenario: Scenario, hours: Int, transport: Transport, departure: Long, swim: Boolean, sleep: Boolean, returnAt:Long=departure+hours*3600_000L): String {
  require(returnAt>departure && returnAt-departure<=1440*3600_000L)
  require(hours==kotlin.math.ceil((returnAt-departure)/3600_000.0).toInt())
  val cache = dao.weather().first()
  val settings = preferences.settings.first()
  val at = if (settings.weatherEnabled && cache != null && cache.matches(settings) && !cache.isStale()) cache.at(departure) else null
  val rule = dao.reference().first()?.let { runCatching { gson.fromJson(it.json, RuleConfig::class.java) }.getOrNull() } ?: RuleConfig()
  val trip = Trip(newId(), child.id, child.name, scenario.name, hours, transport.name, departure, swim=swim, sleep=sleep, returnAt=returnAt,
   weatherNote = if (at != null) "${cache?.city}: ${at.temperature.toInt()} °C, осадки ${at.rain} мм; сохранено ${java.time.Instant.ofEpochMilli(cache!!.fetched)}" else "Без свежего прогноза — проверьте погоду самостоятельно")
  val rules = PackingRules.generate(LocalDate.parse(child.birthday), Feeding.valueOf(child.feeding), hours, scenario, transport, child.needs, at?.conditions(), swim, sleep, dao.learning(child.id).associate { it.name to it.delta }, rule)
  db.withTransaction { dao.trip(trip); dao.items(rules.map { PackingItem(newId(),trip.id,it.name,it.count,it.group,it.reason) }) }
  return trip.id
 }
 suspend fun repeatTrip(trip: Trip): String {
  val child = dao.children().first().find { it.id == trip.childId } ?: error("Профиль ребёнка удалён")
  val start=System.currentTimeMillis()
  return createTrip(child, Scenario.valueOf(trip.scenario), trip.hours, Transport.valueOf(trip.transport), start, trip.swim, trip.sleep, start+if(trip.returnAt>trip.departure) trip.returnAt-trip.departure else trip.hours*3600_000L)
 }
 suspend fun finish(trip: Trip) { dao.trip(trip.copy(completed=true)) }
 suspend fun feedback(trip: Trip, item: PackingItem, value: Int) {
  db.withTransaction {
   val persisted=dao.item(item.id) ?: return@withTransaction
   val previous = dao.learning(trip.childId).find { it.name == item.name }?.delta ?: 0
   dao.learning(Learning(trip.childId, item.name, (previous + value - persisted.feedback).coerceIn(-10, 10)))
   dao.items(listOf(item.copy(feedback=value)))
  }
 }
 suspend fun initializeBags() {
  if (dao.bags().first().isEmpty()) db.withTransaction {
   val bag = Bag(newId(), "На каждый день"); dao.bag(bag)
   listOf("Подгузники" to 4, "Влажные салфетки" to 1, "Сменная одежда" to 1).forEach { (n,q) -> dao.stock(Stock(newId(),bag.id,n,q,1)) }
  }
 }
 suspend fun bagFromTrip(trip: Trip) {
  db.withTransaction {
   val bag=Bag(newId(), "${Scenario.valueOf(trip.scenario).title} · ${trip.childName}"); dao.bag(bag)
   dao.items().first().filter { it.tripId == trip.id }.forEach { dao.stock(Stock(newId(),bag.id,it.name,it.count,1,it.packed,it.imageKey,it.photo)) }
  }
 }
 suspend fun refreshWeather() {
  val s = preferences.settings.first()
  if (!s.weatherEnabled || s.city.isBlank()) return
  val forecast = Network.weather.forecast(s.lat, s.lon)
  require(forecast.hourly.time.isNotEmpty())
  dao.weather(WeatherCache(city=s.city,lat=s.lat,lon=s.lon,json=gson.toJson(forecast),fetched=System.currentTimeMillis()))
 }
 suspend fun deleteChild(child:Child) {
  val ids=dao.trips().first().filter {it.childId==child.id}.map {it.id}.toSet()
  val names=(dao.wardrobe().first().filter {it.childId==child.id}.mapNotNull {it.photo}+dao.items().first().filter {it.tripId in ids}.mapNotNull {it.photo}).toSet()
  db.withTransaction {dao.clearChildTrips(child.id);dao.clearChildLearning(child.id);dao.deleteChild(child)}
  names.forEach {deletePhotoIfUnused(it)}
 }

 suspend fun wardrobe(item:WardrobeItem) {require(item.name.isNotBlank());require(item.warmth in 0..3);GarmentGroup.valueOf(item.group);dao.wardrobe(item)}
 suspend fun choose(childId:String,slot:String,reference:String) {
  db.withTransaction {
   dao.choice(OutfitChoice(childId,slot,reference))
   if(slot=="ONE" && reference.isNotBlank()) {dao.choice(OutfitChoice(childId,"TOP",""));dao.choice(OutfitChoice(childId,"BOTTOM",""))}
   if(slot in listOf("TOP","BOTTOM") && reference.isNotBlank()) dao.choice(OutfitChoice(childId,"ONE",""))
  }
 }
 suspend fun deletePhotoIfUnused(name:String?) {if(name==null) return;val used=dao.wardrobe().first().any {it.photo==name} || dao.items().first().any {it.photo==name} || dao.stock().first().any {it.photo==name};if(!used) photos.file(name)?.delete()}
 suspend fun cleanupPhotos() {photos.cleanup((dao.wardrobe().first().mapNotNull {it.photo}+dao.items().first().mapNotNull {it.photo}+dao.stock().first().mapNotNull {it.photo}).toSet())}
 suspend fun clearAll() {
  db.withTransaction { dao.clearChoices();dao.clearWardrobe();dao.clearTrips(); dao.clearChildren(); dao.clearBags(); dao.clearLearning(); dao.clearWeather(); dao.clearReference() }
  preferences.clear();photos.cleanup(emptySet())
 }
}
