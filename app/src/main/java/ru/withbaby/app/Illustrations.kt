package ru.withbaby.app

import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.graphics.BitmapRegionDecoder
import android.graphics.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.withbaby.data.*
import ru.withbaby.domain.*

private val pictureCache=object:LruCache<String,ImageBitmap>(16*1024*1024) {override fun sizeOf(key:String,value:ImageBitmap)=value.width*value.height*4}
@Composable fun ItemPicture(key:String?,photo:String?,name:String,modifier:Modifier=Modifier) {
 val context=LocalContext.current
 Surface(modifier.clip(RoundedCornerShape(18.dp)),color=Color(0xFFF8F5F0),shape=RoundedCornerShape(18.dp)) {
  BoxWithConstraints(Modifier.fillMaxSize(),contentAlignment=Alignment.Center) {
   val density=LocalDensity.current
   val requested=with(density) {maxOf(maxWidth,maxHeight).toPx()}
   val side=when {requested<=192->192;requested<=384->384;requested<=512->512;else->768}
   val stableKey=key?.takeIf {it in ArtResources.images} ?: "bag_bag"
   val asset=ArtResources.images.getValue(stableKey)
   val cacheKey=if(photo!=null) "photo:$photo:$side" else "catalog:$stableKey:$side"
   val bitmap by produceState<ImageBitmap?>(pictureCache.get(cacheKey),cacheKey) {
    val cached=pictureCache.get(cacheKey)
    value=cached
    if(cached==null) value=withContext(Dispatchers.IO) {
     fun decodeFile():Bitmap? {
      val file=PhotoStore(context).file(photo)?.takeIf {it.isFile} ?: return null
      val bounds=BitmapFactory.Options().apply {inJustDecodeBounds=true};BitmapFactory.decodeFile(file.absolutePath,bounds)
      var sample=1;while(maxOf(bounds.outWidth,bounds.outHeight)/(sample*2)>=side) sample*=2
      return BitmapFactory.decodeFile(file.absolutePath,BitmapFactory.Options().apply {inSampleSize=sample})
     }
     @Suppress("DEPRECATION")
     fun decodeCatalog():Bitmap?=context.resources.openRawResource(asset.drawable).use {input->
      val decoder=BitmapRegionDecoder.newInstance(input,false) ?: return@use null
      try {
       val column=asset.index%asset.columns;val row=asset.index/asset.columns
       val rect=Rect(column*decoder.width/asset.columns,row*decoder.height/asset.rows,(column+1)*decoder.width/asset.columns,(row+1)*decoder.height/asset.rows)
       var sample=1;while(maxOf(rect.width(),rect.height())/(sample*2)>=side) sample*=2
       decoder.decodeRegion(rect,BitmapFactory.Options().apply {inSampleSize=sample})
      } finally {decoder.recycle()}
     }
     val loaded=runCatching {if(photo!=null) decodeFile() ?: decodeCatalog() else decodeCatalog()}.getOrNull()
     val result=loaded?.let {source->
      val scale=minOf(1.0,side.toDouble()/maxOf(source.width,source.height))
      val fitted=if(scale<1) Bitmap.createScaledBitmap(source,(source.width*scale).toInt().coerceAtLeast(1),(source.height*scale).toInt().coerceAtLeast(1),true) else source
      if(fitted!==source) source.recycle()
      fitted.asImageBitmap()
     }
     if(result!=null) pictureCache.put(cacheKey,result)
     result
    }
   }
   val image=bitmap
   if(image!=null) Image(image,name,Modifier.fillMaxSize(),contentScale=ContentScale.Fit)
   else CircularProgressIndicator(Modifier.size(20.dp),strokeWidth=2.dp,color=Color(0xFF6D7081))
  }
 }
}
@Composable fun PicturePreview(name:String,key:String?,photo:String?,detail:String?=null,dismiss:()->Unit) {
 AlertDialog(onDismissRequest=dismiss,title={Text(name)},text={Column(verticalArrangement=Arrangement.spacedBy(16.dp)) {ItemPicture(key,photo,name,Modifier.fillMaxWidth().height(220.dp));if(detail!=null) Text(detail)}},confirmButton={TextButton(onClick=dismiss) {Text("Понятно")}})
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun PictureField(imageKey:String?,photo:String?,name:String,onChange:(String?,String?)->Unit,onError:(String)->Unit) {
 var choosing by remember {mutableStateOf(false)}
 val context=LocalContext.current;val scope=rememberCoroutineScope();var importing by remember {mutableStateOf(false)}
 val launcher=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {uri->if(uri!=null) scope.launch {
  importing=true
  try {val id=withContext(Dispatchers.IO) {PhotoStore(context).import(uri)};onChange(imageKey,id)} catch(e:Exception) {onError("Не удалось открыть фото. Выберите изображение до 20 МБ")} finally {importing=false}
 }}
 Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
  ItemPicture(imageKey ?: BagIllustrations.infer(name),photo,name.ifBlank {"Изображение вещи"},Modifier.size(76.dp))
  Column {TextButton(onClick={choosing=true}) {Text("Из каталога")};TextButton(enabled=!importing,onClick={launcher.launch(arrayOf("image/*"))}) {Text(if(importing) "Сохраняем фото…" else "Своё фото")}}
 }
 if(photo!=null) TextButton(onClick={onChange(imageKey,null)}) {Text("Убрать фото")}
 if(choosing) ModalBottomSheet(onDismissRequest={choosing=false}) {
  var query by remember {mutableStateOf("")}
  val options=BagIllustrations.all+GarmentCatalog.all.map {IllustrationType(it.id,it.name)}
  Column(Modifier.padding(24.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
   Text("Изображение для вещи",style=MaterialTheme.typography.headlineSmall)
   Field(query,{query=it},"Поиск изображения")
   LazyRow(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
    items(options.filter {it.name.contains(query,true)},key={it.id}) {option->
     Column(Modifier.width(150.dp).clickable {onChange(option.id,null);choosing=false},verticalArrangement=Arrangement.spacedBy(8.dp)) {
      ItemPicture(option.id,null,option.name,Modifier.size(150.dp));Text(option.name,style=MaterialTheme.typography.titleSmall)
     }
    }
   }
   Text("Листайте изображения и нажмите на подходящее.",style=MaterialTheme.typography.bodySmall)
  }
 }
}
