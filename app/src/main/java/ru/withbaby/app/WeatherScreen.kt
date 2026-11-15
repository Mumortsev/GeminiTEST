package ru.withbaby.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.withbaby.data.*
import java.util.Locale

@Composable fun WeatherScreen(state:AppState,vm:AppViewModel,back:()->Unit) {
 var query by rememberSaveable {mutableStateOf("")};var searched by remember {mutableStateOf(false)}
 val cities by vm.cities.collectAsStateWithLifecycle();val busy by vm.busy.collectAsStateWithLifecycle()
 val context=LocalContext.current
 val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {granted->
  if(granted) locate(context,{lat,lon-> vm.city(City("Моё местоположение",lat,lon)) },{vm.message(it)}) else vm.message("Геолокация не разрешена. Выберите город вручную")
 }
 Page("Погода для ваших планов",kicker="НЕОБЯЗАТЕЛЬНО, НО ПОЛЕЗНО",back=back) {
  Text("Город или приблизительные координаты отправляются в Open-Meteo. Имена, дата рождения и списки вещей в запросы не входят.",style=MaterialTheme.typography.bodyMedium)
  Field(query,{query=it.take(100);searched=false},"Название города")
  Button(enabled=query.trim().length>=2,onClick={searched=true;vm.searchCity(query)},modifier=Modifier.fillMaxWidth()) {Glyph(R.drawable.ic_search);Spacer(Modifier.width(8.dp));Text("Найти город")}
  if(searched) {
   if(cities.isEmpty()) Text("Если результатов нет, попробуйте другое название или проверьте подключение.",style=MaterialTheme.typography.bodySmall)
   cities.forEach {city->ActionRow(R.drawable.ic_map_pin,city.name,listOfNotNull(city.admin1,city.country).joinToString(", ")) {vm.city(city);searched=false}}
  }
  OutlinedButton(onClick={permission.launch(Manifest.permission.ACCESS_COARSE_LOCATION)},modifier=Modifier.fillMaxWidth()) {Glyph(R.drawable.ic_navigation);Spacer(Modifier.width(8.dp));Text("Определить местоположение")}
  val cache=state.weather
  if(cache!=null) {
   SoftCard(tone=MaterialTheme.colorScheme.primaryContainer) {
    Text(cache.city,style=MaterialTheme.typography.titleLarge)
    Text(if(!state.settings.weatherEnabled) "Погода отключена" else if(cache.isStale()) "Прогноз устарел" else "Сохранённый прогноз",style=MaterialTheme.typography.labelLarge)
    val current=cache.at(System.currentTimeMillis())
    if(current!=null) {
     Text("${current.temperature.toInt()}°",style=MaterialTheme.typography.displaySmall)
     Text("Ощущается как ${current.feels.toInt()} °C",style=MaterialTheme.typography.bodyLarge)
     Text("Ветер ${current.wind.toInt()} км/ч · осадки ${current.rain} мм · УФ ${current.uv.toInt()}",style=MaterialTheme.typography.bodyMedium)
    } else Text("Сохранённый прогноз не покрывает текущее время")
    Text("Получен ${formatTime(cache.fetched)}",style=MaterialTheme.typography.bodySmall)
   }
   Section("Ближайшие часы")
   for(offset in listOf(1,3,6,12,24)) {
    cache.at(System.currentTimeMillis()+offset*3600_000L)?.let {at->
     SoftCard {Text("${formatTime(at.time)} · ${at.temperature.toInt()} °C",style=MaterialTheme.typography.titleMedium);Text("Ощущается ${at.feels.toInt()} °C · ветер ${at.wind.toInt()} км/ч · осадки ${at.rain} мм",style=MaterialTheme.typography.bodySmall)}
    }
   }
  } else Empty(R.drawable.ic_cloud_rain,"Погода пока неизвестна","Выберите город при наличии интернета. Без прогноза приложение продолжит составлять базовые списки.")
  if(state.settings.city.isNotBlank()) {
   Button(enabled=!busy && state.settings.weatherEnabled,onClick=vm::refreshWeather,modifier=Modifier.fillMaxWidth()) {if(busy) CircularProgressIndicator(Modifier.size(18.dp),strokeWidth=2.dp) else Text("Обновить прогноз")}
   Toggle("Использовать погоду",state.settings.weatherEnabled,{vm.saveSettings(state.settings.copy(weatherEnabled=it))})
  }
  Text("Источник: Open-Meteo.com, данные CC BY 4.0. Бесплатный API — для некоммерческого использования. Прогноз может отличаться от условий на месте. Подбирайте одежду с учётом ситуации; приложение не даёт медицинских рекомендаций.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
 }
}
@SuppressLint("MissingPermission")
private fun locate(context:Context,success:(Double,Double)->Unit,error:(String)->Unit) {
 val manager=context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
 val provider=LocationManager.NETWORK_PROVIDER
 if(!manager.isProviderEnabled(provider)) {error("Сетевая геолокация недоступна. Выберите город вручную");return}
 val handler=Handler(Looper.getMainLooper())
 var done=false
 lateinit var listener:LocationListener
 val timeout=Runnable {if(!done) {done=true;manager.removeUpdates(listener);error("Не удалось определить место. Выберите город вручную")}}
 listener=object:LocationListener {
  override fun onLocationChanged(location:Location) {if(!done) {done=true;manager.removeUpdates(this);handler.removeCallbacks(timeout);success(location.latitude,location.longitude)}}
  override fun onProviderEnabled(provider:String) {}
  override fun onProviderDisabled(provider:String) {}
  @Deprecated("Deprecated in Java") override fun onStatusChanged(provider:String?,status:Int,extras:Bundle?) {}
 }
 try {manager.requestSingleUpdate(provider,listener,Looper.getMainLooper());handler.postDelayed(timeout,20000)} catch(e:Exception) {error("Геолокация недоступна. Выберите город вручную")}
}
