package ru.withbaby.app

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.withbaby.data.*
import ru.withbaby.domain.*
import java.time.*

@Composable fun ChildEditor(child:Child?,onboarding:Boolean,vm:AppViewModel,state:AppState,onClose:()->Unit) {
 var parent by rememberSaveable(child?.id) {mutableStateOf(state.settings.parent)}
 var name by rememberSaveable(child?.id) {mutableStateOf(child?.name ?: "")}
 var birthday by rememberSaveable(child?.id) {mutableStateOf(child?.birthday ?: "")}
 var feeding by rememberSaveable(child?.id) {mutableStateOf(child?.feeding ?: Feeding.BREAST.name)}
 var needs by rememberSaveable(child?.id) {mutableStateOf(child?.needs ?: "")}
 var error by remember {mutableStateOf<String?>(null)}
 val context=LocalContext.current
 val pickerTheme=if(MaterialTheme.colorScheme.background.luminance()<.5f) android.R.style.Theme_Material_Dialog_Alert else android.R.style.Theme_Material_Light_Dialog_Alert
 Page(if(onboarding) "Давайте знакомиться" else if(child==null) "Добавить малыша" else "Профиль малыша",kicker=if(onboarding) "ВАШ МАЛЕНЬКИЙ МИР" else "СЕМЬЯ",back=if(onboarding) null else onClose) {
  if(onboarding) {Text("Как к вам обращаться? Данные останутся на вашем устройстве.",style=MaterialTheme.typography.bodyLarge);Field(parent,{parent=it.take(80)},"Имя родителя")}
  Field(name,{name=it.take(80)},"Имя ребёнка")
  OutlinedButton(onClick={
   val date=runCatching {LocalDate.parse(birthday)}.getOrDefault(LocalDate.now())
   DatePickerDialog(context,pickerTheme,{_,y,m,d->birthday=LocalDate.of(y,m+1,d).toString()},date.year,date.monthValue-1,date.dayOfMonth).apply {datePicker.maxDate=System.currentTimeMillis()}.show()
  },modifier=Modifier.fillMaxWidth().heightIn(min=56.dp)) {Glyph(R.drawable.ic_calendar);Spacer(Modifier.width(12.dp));Text(if(birthday.isBlank()) "Дата рождения" else LocalDate.parse(birthday).format(java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy",java.util.Locale.forLanguageTag("ru"))))}
  Picker("Тип кормления",Feeding.valueOf(feeding),Feeding.entries.toList(),{it.title},{feeding=it.name})
  Field(needs,{needs=it.take(2000)},"Что особенно важно взять",singleLine=false)
  Text("Пишите отдельные вещи через точку с запятой: например, «очки; назначенные средства». Они попадут в личный раздел списка. Приложение не назначает лекарства и не определяет дозировки.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
  if(onboarding) PaletteChooser(state.settings.palette) {vm.setPalette(it)}
  if(error!=null) Text(error!!,color=MaterialTheme.colorScheme.error)
  Button(onClick={
   val date=runCatching {LocalDate.parse(birthday)}.getOrNull()
   if(name.isBlank() || onboarding && parent.isBlank() || date==null || date.isAfter(LocalDate.now())) error="Укажите имя и корректную дату рождения"
   else {vm.saveChild(Child(child?.id ?: newId(),name.trim(),birthday,feeding,needs.trim()),if(onboarding) parent else null);onClose()}
  },modifier=Modifier.fillMaxWidth().heightIn(min=54.dp)) {Text(if(onboarding) "Начать" else "Сохранить профиль")}
  if(onboarding) Text("Без аккаунта, пароля и обязательного интернета. Профили можно изменить или удалить в любой момент.",style=MaterialTheme.typography.bodySmall)
 }
}
@OptIn(ExperimentalLayoutApi::class)
@Composable fun CreateTripScreen(state:AppState,vm:AppViewModel,back:()->Unit,open:(String)->Unit) {
 var childId by rememberSaveable {mutableStateOf(state.local.children.first().id)}
 val child=state.local.children.find {it.id==childId} ?: state.local.children.first()
 var scenarioName by rememberSaveable {mutableStateOf(Scenario.WALK.name)}
 var transportName by rememberSaveable {mutableStateOf(Transport.STROLLER.name)}
 var hours by rememberSaveable {mutableStateOf("2")}
 var customHours by rememberSaveable {mutableStateOf(false)}
 var departure by rememberSaveable {mutableLongStateOf(System.currentTimeMillis()+3600_000)}
 var swim by rememberSaveable {mutableStateOf(false)};var sleep by rememberSaveable {mutableStateOf(false)}
 var details by rememberSaveable {mutableStateOf(false)}
 val creating by vm.creating.collectAsState()
 val context=LocalContext.current
 val pickerTheme=if(MaterialTheme.colorScheme.background.luminance()<.5f) android.R.style.Theme_Material_Dialog_Alert else android.R.style.Theme_Material_Light_Dialog_Alert
 Page("Собраться на выход",kicker="ЧТО ВОЗЬМЁМ С СОБОЙ",back=back) {
  if(state.local.children.size>1) Picker("С кем идём",child,state.local.children,{it.name},{childId=it.id})
  else Text("Список для ${child.name}",style=MaterialTheme.typography.titleMedium)
  Picker("Куда идём?",Scenario.valueOf(scenarioName),Scenario.entries.toList(),{it.title},{scenarioName=it.name})
  Text("Надолго?",style=MaterialTheme.typography.titleMedium)
  FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
   listOf(1,2,4,8).forEach {h->FilterChip(selected=!customHours && hours==h.toString(),onClick={hours=h.toString();customHours=false},label={Text("$h ч")})}
   FilterChip(selected=customHours,onClick={customHours=true},label={Text("Другое")})
  }
  if(customHours) Field(hours,{hours=it.filter(Char::isDigit).take(3)},"Количество часов (от 1 до 336)",keyboard=KeyboardOptions(keyboardType=KeyboardType.Number))
  SoftCard {
   Text("${Transport.valueOf(transportName).title} · ${formatTime(departure)}",style=MaterialTheme.typography.bodyMedium)
   TextButton(onClick={details=!details}) {Text(if(details) "Скрыть детали" else "Изменить время и детали")}
   androidx.compose.animation.AnimatedVisibility(details) {
    Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
     Picker("Как добираемся?",Transport.valueOf(transportName),Transport.entries.toList(),{it.title},{transportName=it.name})
     OutlinedButton(onClick={
      val at=Instant.ofEpochMilli(departure).atZone(ZoneId.systemDefault())
      DatePickerDialog(context,pickerTheme,{_,y,m,d->
       TimePickerDialog(context,pickerTheme,{_,hh,mm->departure=LocalDateTime.of(y,m+1,d,hh,mm).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()},at.hour,at.minute,true).show()
      },at.year,at.monthValue-1,at.dayOfMonth).apply {datePicker.minDate=System.currentTimeMillis()-60000;datePicker.maxDate=System.currentTimeMillis()+365L*86400_000}.show()
     },modifier=Modifier.fillMaxWidth()) {Glyph(R.drawable.ic_clock);Spacer(Modifier.width(8.dp));Text("Когда выходим?")}
     Toggle("Сон вне дома",sleep,{sleep=it});Toggle("Водные занятия",swim,{swim=it})
    }
   }
  }
  val forecast=state.weather?.takeIf {state.settings.weatherEnabled && it.matches(state.settings) && !it.isStale()}?.at(departure)
  Text(if(forecast!=null) "Учтём погоду: ${forecast.temperature.toInt()} °C, ветер и осадки." else "Погоду можно учесть самостоятельно — список работает и без прогноза.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
  Button(enabled=!creating && hours.toIntOrNull() in 1..336,onClick={vm.create(child,Scenario.valueOf(scenarioName),hours.toInt(),Transport.valueOf(transportName),departure,swim,sleep,open)},modifier=Modifier.fillMaxWidth().heightIn(min=54.dp)) {if(creating) CircularProgressIndicator(Modifier.size(20.dp),strokeWidth=2.dp) else Text("Готово — показать список")}
 }
}
@Composable fun ItemEditor(item:PackingItem?,tripId:String,onSave:(PackingItem)->Unit,onDelete:((PackingItem)->Unit)?=null,dismiss:()->Unit) {
 var name by remember {mutableStateOf(item?.name ?: "")};var count by remember {mutableStateOf(item?.count?.toString() ?: "1")}
 AlertDialog(onDismissRequest=dismiss,title={Text(if(item==null) "Добавить вещь" else "Изменить вещь")},text={Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {Field(name,{name=it.take(120)},"Название");Field(count,{count=it.filter(Char::isDigit).take(3)},"Количество",keyboard=KeyboardOptions(keyboardType=KeyboardType.Number));if(item!=null && onDelete!=null) TextButton(onClick={onDelete(item);dismiss()}) {Text("Удалить вещь",color=MaterialTheme.colorScheme.error)}}},confirmButton={TextButton(enabled=name.isNotBlank() && count.toIntOrNull() in 1..500,onClick={onSave(item?.copy(name=name.trim(),count=count.toInt()) ?: PackingItem(newId(),tripId,name.trim(),count.toInt(),"Моё","Добавлено вами"));dismiss()}){Text("Сохранить")}},dismissButton={TextButton(onClick=dismiss){Text("Отмена")}})
}
@Composable fun StockEditor(stock:Stock?,bagId:String,onSave:(Stock)->Unit,onDelete:((Stock)->Unit)?=null,dismiss:()->Unit) {
 var name by remember {mutableStateOf(stock?.name ?: "")};var count by remember {mutableStateOf(stock?.count?.toString() ?: "1")};var minimum by remember {mutableStateOf(stock?.minimum?.toString() ?: "1")}
 AlertDialog(onDismissRequest=dismiss,title={Text("Вещь в сумке")},text={Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
  Field(name,{name=it.take(120)},"Название");Field(count,{count=it.filter(Char::isDigit).take(4)},"В наличии",keyboard=KeyboardOptions(keyboardType=KeyboardType.Number));Field(minimum,{minimum=it.filter(Char::isDigit).take(4)},"Пополнить, если меньше",keyboard=KeyboardOptions(keyboardType=KeyboardType.Number))
  if(stock!=null && onDelete!=null) TextButton(onClick={onDelete(stock);dismiss()}) {Text("Удалить вещь",color=MaterialTheme.colorScheme.error)}
 }},confirmButton={TextButton(enabled=name.isNotBlank() && count.toIntOrNull() in 0..9999 && minimum.toIntOrNull() in 0..9999,onClick={onSave(stock?.copy(name=name.trim(),count=count.toInt(),minimum=minimum.toInt()) ?: Stock(newId(),bagId,name.trim(),count.toInt(),minimum.toInt()));dismiss()}){Text("Сохранить")}},dismissButton={TextButton(onClick=dismiss){Text("Отмена")}})
}
@Composable fun NameDialog(title:String,initial:String="",dismiss:()->Unit,save:(String)->Unit) {
 var name by remember {mutableStateOf(initial)}
 AlertDialog(onDismissRequest=dismiss,title={Text(title)},text={Field(name,{name=it.take(80)},"Имя / название")},confirmButton={TextButton(enabled=name.isNotBlank(),onClick={save(name.trim());dismiss()}) {Text("Сохранить")}},dismissButton={TextButton(onClick=dismiss){Text("Отмена")}})
}
