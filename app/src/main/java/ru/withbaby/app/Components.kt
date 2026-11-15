package ru.withbaby.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import ru.withbaby.data.*
import ru.withbaby.domain.*

@Composable fun Glyph(id:Int,description:String?=null,modifier:Modifier=Modifier,tint:Color=LocalContentColor.current) {
 Icon(painterResource(id),description,modifier.size(24.dp),tint=tint)
}
@Composable fun Page(title:String,kicker:String="С МАЛЫШОМ КУДА УГОДНО",back:(()->Unit)?=null,actions:@Composable RowScope.()->Unit={},content:@Composable ColumnScope.()->Unit) {
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=24.dp).padding(top=16.dp,bottom=32.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
  Row(verticalAlignment=Alignment.CenterVertically) {
   if(back!=null) IconButton(onClick=back) { Glyph(R.drawable.ic_arrow_left,"Назад") }
   Column(Modifier.weight(1f)) { Text(kicker,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary,letterSpacing=1.5.sp);Text(title,style=MaterialTheme.typography.headlineLarge) }
   actions()
  }
  content()
 }
}
@Composable fun SoftCard(modifier:Modifier=Modifier,tone:Color=MaterialTheme.colorScheme.surfaceVariant,content:@Composable ColumnScope.()->Unit) {
 Surface(modifier.fillMaxWidth(),shape=RoundedCornerShape(28.dp),color=tone,contentColor=if(tone==MaterialTheme.colorScheme.primaryContainer) MaterialTheme.colorScheme.onPrimaryContainer else if(tone==MaterialTheme.colorScheme.secondaryContainer) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface) { Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp),content=content) }
}
@Composable fun Empty(icon:Int,title:String,detail:String,action:String?=null,onAction:()->Unit={}) {
 SoftCard {
  Surface(color=MaterialTheme.colorScheme.primaryContainer,shape=RoundedCornerShape(20.dp)) { Glyph(icon,modifier=Modifier.padding(16.dp).size(28.dp),tint=MaterialTheme.colorScheme.primary) }
  Text(title,style=MaterialTheme.typography.titleLarge);Text(detail,style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
  if(action!=null) FilledTonalButton(onClick=onAction) { Text(action) }
 }
}
@Composable fun Section(title:String,action:String?=null,onAction:()->Unit={}) {
 Row(verticalAlignment=Alignment.CenterVertically,modifier=Modifier.fillMaxWidth()) { Text(title,style=MaterialTheme.typography.titleLarge,modifier=Modifier.weight(1f));if(action!=null) TextButton(onClick=onAction) { Text(action) } }
}
@Composable fun ActionRow(icon:Int,title:String,subtitle:String?=null,onClick:()->Unit) {
 Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick=onClick).padding(vertical=14.dp,horizontal=4.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)) {
  Glyph(icon,tint=MaterialTheme.colorScheme.primary)
  Column(Modifier.weight(1f)) { Text(title,style=MaterialTheme.typography.titleMedium);if(subtitle!=null) Text(subtitle,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant) }
  Glyph(R.drawable.ic_chevron_right,tint=MaterialTheme.colorScheme.onSurfaceVariant)
 }
}
@Composable fun Progress(packed:Int,total:Int) {
 val value by animateFloatAsState(if(total==0) 0f else packed.toFloat()/total,label="Сборы")
 LinearProgressIndicator(progress={value},modifier=Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),color=MaterialTheme.colorScheme.primary,trackColor=MaterialTheme.colorScheme.outlineVariant)
 AnimatedContent(targetState=packed,label="Собранные вещи") { count -> Text("Собрано $count из $total",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant) }
}
@Composable fun TripCard(trip:Trip,items:List<PackingItem>,open:()->Unit) {
 val own=items.filter { it.tripId==trip.id }
 Surface(onClick=open,color=MaterialTheme.colorScheme.surfaceVariant,shape=RoundedCornerShape(24.dp)) {
  Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
   Row(verticalAlignment=Alignment.CenterVertically) {
    Column(Modifier.weight(1f)) { Text(Scenario.valueOf(trip.scenario).title,style=MaterialTheme.typography.titleLarge);Text("${trip.childName} · ${formatDuration(trip.hours)} · ${if(trip.completed) "Завершено" else formatTime(trip.departure)}",style=MaterialTheme.typography.bodySmall) }
    Glyph(if(trip.favorite) R.drawable.ic_heart else R.drawable.ic_chevron_right,tint=MaterialTheme.colorScheme.primary)
   }; Progress(own.count { it.packed },own.size)
  }
 }
}
fun formatTime(time:Long):String=DateTimeFormatter.ofPattern("d MMM, HH:mm",java.util.Locale.forLanguageTag("ru")).format(Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()))
@Composable fun Field(value:String,onValue:(String)->Unit,label:String,modifier:Modifier=Modifier,singleLine:Boolean=true,keyboard:androidx.compose.foundation.text.KeyboardOptions=androidx.compose.foundation.text.KeyboardOptions.Default) {
 OutlinedTextField(value=value,onValueChange=onValue,label={Text(label)},modifier=modifier.fillMaxWidth(),singleLine=singleLine,shape=RoundedCornerShape(16.dp),keyboardOptions=keyboard)
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun <T> Picker(label:String,value:T,options:List<T>,text:(T)->String,onValue:(T)->Unit) {
 var expanded by remember { mutableStateOf(false) }
 ExposedDropdownMenuBox(expanded=expanded,onExpandedChange={expanded=it}) {
  OutlinedTextField(value=text(value),onValueChange={},readOnly=true,label={Text(label)},trailingIcon={ExposedDropdownMenuDefaults.TrailingIcon(expanded)},modifier=Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),shape=RoundedCornerShape(16.dp))
  ExposedDropdownMenu(expanded=expanded,onDismissRequest={expanded=false}) {options.forEach { option -> DropdownMenuItem(text={Text(text(option))},onClick={onValue(option);expanded=false}) } }
 }
}
@Composable fun Toggle(title:String,checked:Boolean,onChange:(Boolean)->Unit,subtitle:String?=null) {
 Row(Modifier.fillMaxWidth().padding(vertical=4.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
  Column(Modifier.weight(1f)) {Text(title,style=MaterialTheme.typography.bodyLarge);if(subtitle!=null) Text(subtitle,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
  Switch(checked=checked,onCheckedChange=onChange)
 }
}
@Composable fun Confirm(title:String,text:String,confirm:String="Удалить",onDismiss:()->Unit,onConfirm:()->Unit) {
 AlertDialog(onDismissRequest=onDismiss,title={Text(title)},text={Text(text)},confirmButton={TextButton(onClick=onConfirm) {Text(confirm)}},dismissButton={TextButton(onClick=onDismiss){Text("Отмена")}})
}

@Composable fun AuroraCard(content:@Composable ColumnScope.()->Unit) {
 Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).auroraGlow(MaterialTheme.colorScheme.primaryContainer,card=true)) {
  CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onPrimaryContainer) {Column(Modifier.padding(24.dp),verticalArrangement=Arrangement.spacedBy(12.dp),content=content)}
 }
}

fun russianCount(n:Int,one:String,few:String,many:String):String=if(n%100 in 11..14) many else when(n%10) {1->one;2,3,4->few;else->many}
fun formatDuration(hours:Int):String {
 val days=hours/24;val remainder=hours%24
 return if(days==0) "$hours ч" else "$days ${russianCount(days,"сутки","суток","суток")}"+(if(remainder>0) " $remainder ч" else "")
}
