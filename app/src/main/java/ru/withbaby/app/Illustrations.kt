package ru.withbaby.app

import android.graphics.BitmapFactory
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.withbaby.data.*
import ru.withbaby.domain.*

private val pictureCache=object:LruCache<String,ImageBitmap>(8*1024*1024) {override fun sizeOf(key:String,value:ImageBitmap)=value.width*value.height*4}
@Composable fun ItemPicture(key:String?,photo:String?,name:String,modifier:Modifier=Modifier) {
 val context=LocalContext.current
 val bitmap by produceState<ImageBitmap?>(null,photo) {
  value=photo?.let {id->pictureCache.get(id) ?: withContext(Dispatchers.IO) {
   val file=PhotoStore(context).file(id)
   val bitmap=file?.takeIf {it.isFile}?.let {BitmapFactory.decodeFile(it.absolutePath,BitmapFactory.Options().apply {inSampleSize=2})}?.asImageBitmap()
   if(bitmap!=null) pictureCache.put(id,bitmap)
   bitmap
  }}
 }
 Surface(modifier.clip(RoundedCornerShape(18.dp)),color=Color(0xFFF4F2EE),shape=RoundedCornerShape(18.dp)) {
  val image=bitmap
  if(image!=null) Image(image,name,Modifier.fillMaxSize(),contentScale=ContentScale.Fit)
  else Image(painterResource(ArtResources.ids[key] ?: R.drawable.art_bag_bag),name,Modifier.fillMaxSize().padding(6.dp),contentScale=ContentScale.Fit)
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
  Column {TextButton(onClick={choosing=true}) {Text("Выбрать рисунок")};TextButton(enabled=!importing,onClick={launcher.launch(arrayOf("image/*"))}) {Text(if(importing) "Сохраняем фото…" else "Своё фото")}}
 }
 if(photo!=null) TextButton(onClick={onChange(imageKey,null)}) {Text("Убрать фото")}
 if(choosing) ModalBottomSheet(onDismissRequest={choosing=false}) {
  var query by remember {mutableStateOf("")}
  val options=BagIllustrations.all+GarmentCatalog.all.map {IllustrationType(it.id,it.name)}
  Column(Modifier.padding(24.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
   Text("Рисунок для вещи",style=MaterialTheme.typography.headlineSmall)
   Field(query,{query=it},"Поиск изображения")
   LazyRow(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
    items(options.filter {it.name.contains(query,true)},key={it.id}) {option->
     Column(Modifier.width(150.dp).clickable {onChange(option.id,null);choosing=false},verticalArrangement=Arrangement.spacedBy(8.dp)) {
      ItemPicture(option.id,null,option.name,Modifier.size(150.dp));Text(option.name,style=MaterialTheme.typography.titleSmall)
     }
    }
   }
   Text("Листайте рисунки влево и вправо. Нажмите на подходящий.",style=MaterialTheme.typography.bodySmall)
  }
 }
}
