package ru.withbaby.app

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import ru.withbaby.data.*
import ru.withbaby.domain.*
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class,ExperimentalMaterial3Api::class)
@Composable fun ClothingScreen(state:AppState,vm:AppViewModel,onWardrobe:(String)->Unit,onWeather:()->Unit) {
 val now by produceState(System.currentTimeMillis()) {while(true) {delay(60_000);value=System.currentTimeMillis()}}
 val cache=state.weather?.takeIf {state.settings.weatherEnabled && it.matches(state.settings)}
 var manual by rememberSaveable {mutableStateOf(cache==null || cache.isStale() || cache.at(now)==null)}
 var childId by rememberSaveable {mutableStateOf(state.local.children.first().id)}
 val child=state.local.children.find {it.id==childId} ?: state.local.children.first()
 var modeName by rememberSaveable {mutableStateOf(WalkMode.STROLLER.name)}
 val mode=WalkMode.valueOf(modeName)
 var hoursAhead by rememberSaveable {mutableIntStateOf(0)}
 var temperature by rememberSaveable {mutableStateOf("")}
 var windy by rememberSaveable {mutableStateOf(false)}
 var rainy by rememberSaveable {mutableStateOf(false)}
 var sunny by rememberSaveable {mutableStateOf(false)}
 val target=now+hoursAhead*3600_000L
 val at=cache?.takeUnless {it.isStale()}?.at(target)
 val manualTemperature=temperature.replace(',','.').toDoubleOrNull()?.takeIf {it.isFinite() && it in -60.0..60.0}
 val weather=if(manual) manualTemperature?.let {ClothingWeather(it,wind=if(windy) 25.0 else 0.0,rain=if(rainy) 1.0 else 0.0,uv=if(sunny) 4.0 else 0.0)}
  else at?.let {ClothingWeather(it.temperature,it.feels,it.wind,it.rain,it.uv)}
 val outfit=weather?.let {ClothingRules.suggest(LocalDate.parse(child.birthday),it,mode)}
 val options=clothingOptions(child,weather,state.wardrobe)
 val automatic=weather?.let {automaticOutfit(child,it,mode,options)}.orEmpty()
 val selected=selectedOutfit(child,automatic,options,state.choices)
 var choosing by remember {mutableStateOf<GarmentGroup?>(null)}
 var addLayer by remember {mutableStateOf(false)}
 val context=LocalContext.current
 Page("Как одеть малыша",kicker="ОДЕЖДА ПО ПОГОДЕ") {
  if(state.local.children.size>1) Picker("Для кого",child,state.local.children,{it.name},{childId=it.id})
  OutlinedButton(onClick={onWardrobe(child.id)},modifier=Modifier.fillMaxWidth()) {Text("Гардероб ${child.name}")}
  Text("${child.name}: подберём удобные слои для прогулки.",style=MaterialTheme.typography.bodyLarge)
  FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
   WalkMode.entries.forEach {m->FilterChip(selected=mode==m,onClick={modeName=m.name},label={Text(m.title)})}
  }
  Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
   FilterChip(selected=!manual,onClick={manual=false},label={Text("По прогнозу")},leadingIcon={Glyph(R.drawable.ic_cloud_rain)})
   FilterChip(selected=manual,onClick={manual=true},label={Text("Вручную")},leadingIcon={Glyph(R.drawable.ic_pencil)})
  }
  if(manual) {
   SoftCard {
    Field(temperature,{temperature=it.filter {c->c.isDigit() || c in "-+.,"}.take(6)},"Температура на улице, °C",keyboard=KeyboardOptions(keyboardType=KeyboardType.Decimal))
    if(temperature.isNotBlank() && temperature !in listOf("-","+") && manualTemperature==null) Text("Укажите число от −60 до +60 °C",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.error)
    Toggle("Сильный ветер",windy,{windy=it})
    Toggle("Дождь или мокрый снег",rainy,{rainy=it})
    Toggle("Яркое солнце",sunny,{sunny=it})
   }
   Text("Используем указанные вами условия. Интернет не нужен.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
  } else {
   FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
    listOf(0 to "Сейчас",1 to "Через час",3 to "Через 3 часа").forEach {(hour,label)->FilterChip(selected=hoursAhead==hour,onClick={hoursAhead=hour},label={Text(label)})}
   }
   if(at!=null) AuroraCard {
    Text("${at.temperature.toInt()} °C · ощущается ${at.feels.toInt()} °C",style=MaterialTheme.typography.headlineSmall)
    Text(cache.city,style=MaterialTheme.typography.titleMedium)
    Text("${formatTime(at.time)} · ветер ${at.wind.toInt()} км/ч · осадки ${at.rain} мм",style=MaterialTheme.typography.bodySmall)
    Text("Прогноз сохранён ${formatTime(cache.fetched)}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
    TextButton(onClick=onWeather) {Text("Обновить или сменить город")}
   } else Empty(R.drawable.ic_cloud_rain,if(cache?.isStale()==true) "Прогноз устарел" else "Нужен свежий прогноз","Укажите условия вручную или выберите город и обновите погоду.","Настроить погоду",onWeather)
  }
  if(outfit!=null && weather!=null) {
   AuroraCard {
    Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(16.dp)) {
     Glyph(R.drawable.ic_shirt,modifier=Modifier.size(44.dp),tint=MaterialTheme.colorScheme.primary)
     Column(Modifier.weight(1f)) {Text("ГОТОВЫЙ КОМПЛЕКТ",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary);Text(outfit.title,style=MaterialTheme.typography.headlineSmall)}
    }
    Text("${child.name} · ${mode.title.lowercase()}",style=MaterialTheme.typography.bodyMedium)
   }
   val base=selected.filterKeys {it in listOf(GarmentGroup.TOP,GarmentGroup.BOTTOM,GarmentGroup.ONE)}
   VisualLayerCard("Основа",base.values.filterNotNull(),{choosing=base.keys.firstOrNull() ?: GarmentGroup.TOP})
   selected.filterKeys {it !in listOf(GarmentGroup.TOP,GarmentGroup.BOTTOM,GarmentGroup.ONE)}.forEach {(group,option)->
    VisualLayerCard(group.title,listOfNotNull(option),{choosing=group})
   }
   TextButton(onClick={addLayer=true}) {Glyph(R.drawable.ic_plus);Text("Добавить одежду или аксессуар")}
   if(state.choices.any {it.childId==child.id && it.reference.isNotBlank()}) TextButton(onClick={vm.resetOutfit(child.id)}) {Text("Вернуть автоматический подбор")}
   SoftCard {Text("Взять с собой",style=MaterialTheme.typography.titleMedium);outfit.takeAlong.forEach {Text("• $it",style=MaterialTheme.typography.bodyMedium)}}
   Text(outfit.note,style=MaterialTheme.typography.bodyMedium)
   outfit.caution?.let {SoftCard(tone=MaterialTheme.colorScheme.secondaryContainer) {Text(it,style=MaterialTheme.typography.bodyMedium)}}
   OutlinedButton(onClick={
    val text=buildString {
     append("Как одеть малыша · ${child.name}\n${mode.title}\n")
     if(manual) append("${weather.temperature} °C · условия указаны вручную\n") else append("${cache?.city} · ${formatTime(at!!.time)} · ${at.temperature.toInt()} °C, ощущается ${at.feels.toInt()} °C\nПрогноз сохранён ${formatTime(cache!!.fetched)}\n")
     append("\n${outfit.title}\n");selected.forEach {(g,o)->if(o!=null) append("${g.title}: ${o.name}\n")};append("\nВзять с собой:\n");outfit.takeAlong.forEach {append("• $it\n")};append("\n${outfit.note}\n");outfit.caution?.let {append("$it\n")};append("Ориентир по одежде, не медицинская рекомендация. Подберите слои по ситуации.")
    }
    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {type="text/plain";putExtra(Intent.EXTRA_TEXT,text)},"Поделиться комплектом"))
   },modifier=Modifier.fillMaxWidth()) {Glyph(R.drawable.ic_share_2);Spacer(Modifier.width(8.dp));Text("Поделиться комплектом")}
   Text("Это ориентир, а не строгая инструкция. Оценивайте самочувствие малыша и условия на месте; при особых потребностях следуйте советам вашего специалиста.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
  } else if(manual) Text("Введите температуру — комплект появится сразу.",style=MaterialTheme.typography.bodyMedium)
 }
 choosing?.let {group->ClothingCarousel(child,group,group in listOf(GarmentGroup.TOP,GarmentGroup.BOTTOM,GarmentGroup.ONE),options,selected,vm) {choosing=null}}
 if(addLayer) ModalBottomSheet(onDismissRequest={addLayer=false}) {
  Column(Modifier.heightIn(max=600.dp).verticalScroll(rememberScrollState()).padding(24.dp)) {Text("Что добавить?",style=MaterialTheme.typography.headlineSmall);GarmentGroup.entries.filter {it !in listOf(GarmentGroup.TOP,GarmentGroup.BOTTOM,GarmentGroup.ONE)}.forEach {g->ActionRow(R.drawable.ic_plus,g.title,onClick={addLayer=false;choosing=g})}}
 }

}

@Composable private fun VisualLayerCard(title:String,options:List<ClothingOption>,open:()->Unit) {
 SoftCard {
  Row(Modifier.fillMaxWidth().clickable(onClick=open),verticalAlignment=Alignment.CenterVertically) {
   Column(Modifier.weight(1f)) {Text(title,style=MaterialTheme.typography.titleLarge);Text("Нажмите, чтобы выбрать",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
   Glyph(R.drawable.ic_chevron_right)
  }
  if(options.isEmpty()) Text("Без этого слоя",style=MaterialTheme.typography.bodyMedium)
  options.forEach {option->
   Row(Modifier.fillMaxWidth().clickable(onClick=open),horizontalArrangement=Arrangement.spacedBy(14.dp),verticalAlignment=Alignment.CenterVertically) {
    ItemPicture(option.imageKey,option.photo,option.name,Modifier.size(94.dp))
    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)) {
     Text(option.name,style=MaterialTheme.typography.titleMedium)
     Text(if(option.owned) "Есть в гардеробе" else "Наличие не отмечено",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
     if(!option.suitable) Text("Ваш выбор: проверьте по погоде и возрасту",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
    }
   }
  }
 }
}
