package ru.withbaby.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayInputStream
import java.io.File
import java.util.UUID

/** App-private, bounded WebP photos. Original metadata/EXIF are not copied. */
class PhotoStore(private val context:Context) {
 val directory:File get()=File(context.filesDir,"pictures").apply {mkdirs()}
 fun file(name:String?):File?=name?.takeIf {it.matches(Regex("[a-f0-9-]{36}\\.webp"))}?.let {File(directory,it)}
 fun import(uri:Uri):String {
  val bytes=context.contentResolver.openInputStream(uri)?.use {it.readLimited(20_000_001)} ?: error("Файл недоступен")
  require(bytes.size<=20_000_000) {"Фото больше 20 МБ"}
  val bounds=BitmapFactory.Options().apply {inJustDecodeBounds=true};BitmapFactory.decodeByteArray(bytes,0,bytes.size,bounds)
  require(bounds.outWidth>0 && bounds.outHeight>0) {"Не удалось прочитать изображение"}
  var sample=1;while(bounds.outWidth/sample>1536 || bounds.outHeight/sample>1536) sample*=2
  var bitmap=BitmapFactory.decodeByteArray(bytes,0,bytes.size,BitmapFactory.Options().apply {inSampleSize=sample}) ?: error("Не удалось прочитать фото")
  val orientation=runCatching {ExifInterface(ByteArrayInputStream(bytes)).getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL)}.getOrDefault(1)
  val matrix=Matrix().apply {when(orientation) {2->setScale(-1f,1f);3->setRotate(180f);4->setScale(1f,-1f);5->{setRotate(90f);postScale(-1f,1f)};6->setRotate(90f);7->{setRotate(-90f);postScale(-1f,1f)};8->setRotate(-90f)}}
  if(!matrix.isIdentity) {val rotated=Bitmap.createBitmap(bitmap,0,0,bitmap.width,bitmap.height,matrix,true);if(rotated!==bitmap) bitmap.recycle();bitmap=rotated}
  val scale=minOf(1.0,1024.0/maxOf(bitmap.width,bitmap.height))
  if(scale<1) {val small=Bitmap.createScaledBitmap(bitmap,(bitmap.width*scale).toInt().coerceAtLeast(1),(bitmap.height*scale).toInt().coerceAtLeast(1),true);if(small!==bitmap) bitmap.recycle();bitmap=small}
  val name=UUID.randomUUID().toString()+".webp"
  try {File(directory,name).outputStream().use {out->@Suppress("DEPRECATION") val ok=bitmap.compress(Bitmap.CompressFormat.WEBP,92,out);require(ok)}} finally {bitmap.recycle()}
  return name
 }
 fun validate(bytes:ByteArray) {
  require(bytes.size<=2_000_000)
  val bounds=BitmapFactory.Options().apply {inJustDecodeBounds=true};BitmapFactory.decodeByteArray(bytes,0,bytes.size,bounds)
  require(bounds.outMimeType=="image/webp" && bounds.outWidth in 1..1024 && bounds.outHeight in 1..1024)
 }
 fun put(bytes:ByteArray):String {validate(bytes);val name=UUID.randomUUID().toString()+".webp";File(directory,name).writeBytes(bytes);return name}
 fun cleanup(used:Set<String>) {directory.listFiles()?.filter {it.name !in used}?.forEach {it.delete()}}
}
