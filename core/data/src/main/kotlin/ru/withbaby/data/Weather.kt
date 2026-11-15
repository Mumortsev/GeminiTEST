package ru.withbaby.data

import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import ru.withbaby.domain.Conditions
import java.time.Instant
import kotlin.math.abs
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

data class Cities(val results: List<City>?)
data class City(val name: String, val latitude: Double, val longitude: Double, val country: String? = null, val admin1: String? = null) {
 val label: String get() = listOfNotNull(name, admin1, country).distinct().joinToString(", ")
}
data class Hourly(val time: List<Long>, val temperature_2m: List<Double>, val apparent_temperature: List<Double>, val precipitation: List<Double>, val wind_speed_10m: List<Double>, val uv_index: List<Double>)
data class Forecast(val hourly: Hourly)
data class WeatherAt(val temperature: Double, val feels: Double, val rain: Double, val wind: Double, val uv: Double, val time: Long) {
 fun conditions() = Conditions(temperature, rain, wind, uv)
}
interface WeatherApi {
 @GET("v1/forecast") suspend fun forecast(@Query("latitude") lat: Double, @Query("longitude") lon: Double,
  @Query("hourly") hourly: String = "temperature_2m,apparent_temperature,precipitation,wind_speed_10m,uv_index",
  @Query("timeformat") format: String = "unixtime", @Query("forecast_days") days: Int = 7): Forecast
}
interface GeocodingApi {
 @GET("v1/search") suspend fun search(@Query("name") name: String, @Query("count") count: Int = 8, @Query("language") language: String = "ru"): Cities
}
object Network {
 private val client = OkHttpClient.Builder().callTimeout(25, TimeUnit.SECONDS).build()
 private fun retrofit(url: String) = Retrofit.Builder().baseUrl(url).client(client).addConverterFactory(GsonConverterFactory.create()).build()
 val weather: WeatherApi = retrofit("https://api.open-meteo.com/").create(WeatherApi::class.java)
 val cities: GeocodingApi = retrofit("https://geocoding-api.open-meteo.com/").create(GeocodingApi::class.java)
}
fun WeatherCache.at(epochMillis: Long): WeatherAt? = runCatching {
 val h = Gson().fromJson(json, Forecast::class.java).hourly
 val i = h.time.indices.minByOrNull { abs(h.time[it] - epochMillis / 1000) } ?: return null
 if (abs(h.time[i] - epochMillis / 1000) > 3600) return null
 WeatherAt(h.temperature_2m[i], h.apparent_temperature[i], h.precipitation[i], h.wind_speed_10m[i], h.uv_index[i], h.time[i] * 1000)
}.getOrNull()
fun WeatherCache.isStale() = System.currentTimeMillis() - fetched > 6 * 3600_000L

fun WeatherCache.matches(settings: Settings) = lat == settings.lat && lon == settings.lon
