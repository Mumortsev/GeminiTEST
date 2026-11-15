package ru.withbaby.app

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import ru.withbaby.data.*
import ru.withbaby.domain.*

class BabyApp: Application() {
 val repository by lazy { Repository(this) }
 override fun onCreate() { super.onCreate(); Background.schedule(this) }
}
data class LocalData(val children: List<Child> = emptyList(), val trips: List<Trip> = emptyList(), val items: List<PackingItem> = emptyList(), val bags: List<Bag> = emptyList(), val stock: List<Stock> = emptyList())
data class AppState(val local: LocalData = LocalData(), val settings: Settings = Settings(), val weather: WeatherCache? = null, val reference: Reference? = null, val ready: Boolean = false)
class AppViewModel(app: Application): AndroidViewModel(app) {
 val repo=(app as BabyApp).repository
 private val local=combine(repo.dao.children(),repo.dao.trips(),repo.dao.items(),repo.dao.bags(),repo.dao.stock()) { c,t,i,b,s -> LocalData(c,t,i,b,s) }
 val state=combine(local,repo.preferences.settings,repo.dao.weather(),repo.dao.reference()) { l,s,w,r -> AppState(l,s,w,r,true) }
  .stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),AppState())
 val messages=Channel<String>(Channel.BUFFERED)
 val busy=MutableStateFlow(false)
 val creating=MutableStateFlow(false)
 val cities=MutableStateFlow<List<City>>(emptyList())
 init { action { repo.initializeBags() } }
 fun action(block: suspend () -> Unit) { viewModelScope.launch { try { block() } catch (e: CancellationException) { throw e } catch (e: Exception) { messages.send(if(e is javax.crypto.AEADBadTagException) "Неверный пароль или повреждённая копия" else "Не удалось выполнить действие. Проверьте данные и повторите попытку") } } }
 fun message(text: String) { action { messages.send(text) } }
 fun saveChild(child: Child,parent: String?=null) { action { repo.dao.child(child); if(parent!=null) repo.preferences.save(repo.preferences.settings.first().copy(parent=parent.trim())) } }
 fun saveSettings(s: Settings) { action { repo.preferences.save(s) } }
 fun updateItem(i: PackingItem) { action { repo.dao.items(listOf(i)) } }
 fun deleteItem(i: PackingItem) { action { repo.dao.deleteItem(i) } }
 fun deleteChild(c: Child) { action { state.value.local.trips.filter { it.childId==c.id }.forEach { Background.cancel(getApplication(),it.id) };repo.deleteChild(c) } }
 fun trip(t: Trip) { action { repo.dao.trip(t) } }
 fun deleteTrip(t: Trip) { action { Background.cancel(getApplication(),t.id); repo.dao.deleteTrip(t) } }
 fun repeat(t: Trip,open:(String)->Unit) { action { open(repo.repeatTrip(t)) } }
 fun create(c: Child,s: Scenario,h: Int,t: Transport,d: Long,swim:Boolean,sleep:Boolean,open:(String)->Unit) { if(creating.value) return;creating.value=true;action {try {open(repo.createTrip(c,s,h,t,d,swim,sleep))} finally {creating.value=false}} }
 fun finish(t: Trip) { action { repo.finish(t); Background.cancel(getApplication(),t.id); messages.send("Поездка завершена. Отметьте, чего не хватило и что было лишним") } }
 fun feedback(t: Trip,i: PackingItem,v: Int) { action { repo.feedback(t,i,v) } }
 fun saveBag(b: Bag) { action { require(b.name.isNotBlank());repo.dao.bag(b) } }
 fun deleteBag(b: Bag) { action { repo.dao.deleteBag(b) } }
 fun stock(s: Stock) { action { repo.dao.stock(s); if(s.count < s.minimum && state.value.settings.notifications) Background.notify(getApplication(),s.id.hashCode(),"Пора пополнить сумку", "${s.name}: осталось ${s.count}, минимум ${s.minimum}") } }
 fun deleteStock(s: Stock) { action { repo.dao.deleteStock(s) } }
 fun bagFromTrip(t: Trip) { action { repo.bagFromTrip(t);messages.send("Шаблон сумки сохранён") } }
 fun packFromBag(t: Trip,bagId:String) { action {
  val stock=state.value.local.stock.filter { it.bagId==bagId && it.packed && it.count>0 }
  val items=state.value.local.items.filter { it.tripId==t.id }
  repo.dao.items(items.map { i -> i.copy(packed=stock.any { it.name.equals(i.name,true) && it.count>=i.count } || i.packed) })
  messages.send("Отмечены вещи, которых достаточно в выбранной сумке. Запасы не списаны")
 } }
 fun consume(s: Stock) { stock(s.copy(count=(s.count-1).coerceAtLeast(0))) }
 private var cityJob: Job?=null
 fun searchCity(query:String) {
  cityJob?.cancel(); cityJob=viewModelScope.launch { cities.value=emptyList(); if(query.trim().length<2) return@launch
   delay(400);try { cities.value=Network.cities.search(query.trim()).results.orEmpty() } catch(e:CancellationException) {throw e} catch(e:Exception) { messages.send("Города недоступны. Проверьте подключение") }
  }
 }
 fun city(c:City) { action { repo.preferences.save(repo.preferences.settings.first().copy(city=c.label,lat=c.latitude,lon=c.longitude,weatherEnabled=true)); refreshWeather() } }
 fun refreshWeather() { action {
  busy.value=true
  try { withContext(Dispatchers.IO) { repo.refreshWeather() };messages.send("Прогноз обновлён") }
  catch(e:Exception) { messages.send("Не удалось обновить прогноз. Сохранённые данные остаются доступны") }
  finally { busy.value=false }
 } }
 fun remind(t:Trip) { action {
  if (!repo.preferences.settings.first().notifications) { messages.send("Сначала включите уведомления в профиле");return@action }
  Background.remind(getApplication(),t);messages.send("Напомним за 30 минут до выхода. Android может задержать уведомление")
 } }
 fun backup(uri:Uri,password:String) { action {
  withContext(Dispatchers.IO) { val bytes=Backup.export(repo,password.toCharArray());getApplication<Application>().contentResolver.openOutputStream(uri,"wt")?.use { it.write(bytes) } ?: error("Файл недоступен") }
  messages.send("Зашифрованная копия сохранена")
 } }
 fun restore(uri:Uri,password:String) { action {
  withContext(Dispatchers.IO) {
   val bytes=getApplication<Application>().contentResolver.openInputStream(uri)?.use { it.readLimited(10_000_001) } ?: error("Файл недоступен")
   // Cancel pending reminders only after successful validation and restoration.
   Backup.restore(repo,bytes,password.toCharArray());WorkManager.getInstance(getApplication()).cancelAllWork();Background.schedule(getApplication())
  }; messages.send("Данные восстановлены. При необходимости включите уведомления снова")
 } }
 fun clear() { action { WorkManager.getInstance(getApplication()).cancelAllWork();repo.clearAll();Background.schedule(getApplication());repo.initializeBags();messages.send("Личные данные удалены") } }
}
