package ru.withbaby.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import ru.withbaby.domain.RuleConfig

object Background {
 fun schedule(context: Context) {
  val work = PeriodicWorkRequestBuilder<RefreshWorker>(6, TimeUnit.HOURS)
   .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build()
  WorkManager.getInstance(context).enqueueUniquePeriodicWork("reference-weather", ExistingPeriodicWorkPolicy.KEEP, work)
  WorkManager.getInstance(context).enqueueUniqueWork("refresh-on-launch", ExistingWorkPolicy.KEEP,
   OneTimeWorkRequestBuilder<RefreshWorker>().setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build())
 }
 fun remind(context: Context, trip: Trip, minutes: Long = 30) {
  val delay = (trip.departure - System.currentTimeMillis() - minutes * 60_000).coerceAtLeast(0)
  val work = OneTimeWorkRequestBuilder<ReminderWorker>().setInitialDelay(delay, TimeUnit.MILLISECONDS).setInputData(workDataOf("trip" to trip.id)).build()
  WorkManager.getInstance(context).enqueueUniqueWork("trip-${trip.id}",ExistingWorkPolicy.REPLACE,work)
 }
 fun cancel(context: Context, id: String) { WorkManager.getInstance(context).cancelUniqueWork("trip-$id") }
 fun notify(context: Context, id: Int, title: String, text: String) {
  if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
  val nm=context.getSystemService(NotificationManager::class.java)
  nm.createNotificationChannel(NotificationChannel("packing", "Подготовка и пополнение", NotificationManager.IMPORTANCE_DEFAULT))
  val intent = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return
  val pending=PendingIntent.getActivity(context,id,intent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
  nm.notify(id,NotificationCompat.Builder(context,"packing").setSmallIcon(R.drawable.ic_notification)
   .setContentTitle(title).setContentText(text).setStyle(NotificationCompat.BigTextStyle().bigText(text)).setContentIntent(pending).setAutoCancel(true).build())
 }
}
class ReminderWorker(context: Context, params: WorkerParameters): CoroutineWorker(context,params) {
 override suspend fun doWork(): Result {
  val repo=Repository(applicationContext)
  return try {
   if (!repo.preferences.settings.first().notifications) return Result.success()
   val trip=repo.dao.trips().first().find { it.id == inputData.getString("trip") && !it.completed } ?: return Result.success()
   Background.notify(applicationContext, trip.id.hashCode(), "Ничего не забыли?", "${trip.childName}: проверьте список перед выходом")
   Result.success()
  } finally { repo.db.close() }
 }
}
class RefreshWorker(context: Context, params: WorkerParameters): CoroutineWorker(context,params) {
 override suspend fun doWork(): Result {
  val repo=Repository(applicationContext)
  return try {
   val weather=runCatching { repo.refreshWeather() }
   val reference=runCatching { ReferenceUpdates.update(repo) }
   if (weather.isFailure || reference.isFailure) Result.retry() else Result.success()
  } finally { repo.db.close() }
 }
}
data class ReferenceManifest(val version: Int, val sha256: String)
object ReferenceUpdates {
 private const val BASE="https://raw.githubusercontent.com/Mumortsev/GeminiTEST/main/reference/"
 private fun read(name: String): ByteArray {
  val connection=URL(BASE+name).openConnection().apply { connectTimeout=15000; readTimeout=15000 }
  return connection.getInputStream().use { stream -> stream.readLimited(65537).also { require(it.size <= 65536) } }
 }
 suspend fun update(repo: Repository) = withContext(Dispatchers.IO) {
  val manifest=Gson().fromJson(String(read("manifest.json"), Charsets.UTF_8), ReferenceManifest::class.java)
  val local=repo.dao.reference().first()
  if (manifest.version <= (local?.version ?: 1)) return@withContext
  val bytes=read("rules-${manifest.version}.json")
  val hash=MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
  require(hash == manifest.sha256)
  val json=String(bytes,Charsets.UTF_8)
  val config=Gson().fromJson(json,RuleConfig::class.java)
  require(config.version == manifest.version && config.diaperHours in 1.0..6.0 && config.spareSetsHours in 2.0..24.0)
  repo.dao.reference(Reference(version=config.version,json=json,checked=System.currentTimeMillis()))
 }
}
