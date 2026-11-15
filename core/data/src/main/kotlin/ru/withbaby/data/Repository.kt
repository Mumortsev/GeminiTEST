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
 val db = Room.databaseBuilder(context.applicationContext, BabyDatabase::class.java, "with-baby.db").enableMultiInstanceInvalidation().build()
 val dao = db.dao()
 val preferences = Preferences(context.applicationContext)
 val gson = Gson()
 suspend fun createTrip(child: Child, scenario: Scenario, hours: Int, transport: Transport, departure: Long, swim: Boolean, sleep: Boolean): String {
  val cache = dao.weather().first()
  val settings = preferences.settings.first()
  val at = if (settings.weatherEnabled && cache != null && cache.matches(settings) && !cache.isStale()) cache.at(departure) else null
  val rule = dao.reference().first()?.let { runCatching { gson.fromJson(it.json, RuleConfig::class.java) }.getOrNull() } ?: RuleConfig()
  val trip = Trip(newId(), child.id, child.name, scenario.name, hours, transport.name, departure, swim=swim, sleep=sleep,
   weatherNote = if (at != null) "${cache?.city}: ${at.temperature.toInt()} °C, осадки ${at.rain} мм; сохранено ${java.time.Instant.ofEpochMilli(cache!!.fetched)}" else "Без свежего прогноза — проверьте погоду самостоятельно")
  val rules = PackingRules.generate(LocalDate.parse(child.birthday), Feeding.valueOf(child.feeding), hours, scenario, transport, child.needs, at?.conditions(), swim, sleep, dao.learning(child.id).associate { it.name to it.delta }, rule)
  db.withTransaction { dao.trip(trip); dao.items(rules.map { PackingItem(newId(),trip.id,it.name,it.count,it.group,it.reason) }) }
  return trip.id
 }
 suspend fun repeatTrip(trip: Trip): String {
  val child = dao.children().first().find { it.id == trip.childId } ?: error("Профиль ребёнка удалён")
  return createTrip(child, Scenario.valueOf(trip.scenario), trip.hours, Transport.valueOf(trip.transport), System.currentTimeMillis(), trip.swim, trip.sleep)
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
   dao.items().first().filter { it.tripId == trip.id }.forEach { dao.stock(Stock(newId(),bag.id,it.name,it.count,1,it.packed)) }
  }
 }
 suspend fun refreshWeather() {
  val s = preferences.settings.first()
  if (!s.weatherEnabled || s.city.isBlank()) return
  val forecast = Network.weather.forecast(s.lat, s.lon)
  require(forecast.hourly.time.isNotEmpty())
  dao.weather(WeatherCache(city=s.city,lat=s.lat,lon=s.lon,json=gson.toJson(forecast),fetched=System.currentTimeMillis()))
 }
 suspend fun deleteChild(child: Child) { db.withTransaction { dao.clearChildTrips(child.id); dao.clearChildLearning(child.id); dao.deleteChild(child) } }
 suspend fun clearAll() {
  db.withTransaction { dao.clearTrips(); dao.clearChildren(); dao.clearBags(); dao.clearLearning(); dao.clearWeather(); dao.clearReference() }
  preferences.clear()
 }
}
