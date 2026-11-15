package ru.withbaby.app

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ru.withbaby.data.*
import ru.withbaby.domain.*

@Composable fun HistoryScreen(state:AppState,vm:AppViewModel,open:(String)->Unit) {
 var search by rememberSaveable {mutableStateOf("")};var favorite by rememberSaveable {mutableStateOf(false)}
 Page("Ваши маленькие истории",kicker="ВЫХОДЫ И ПУТЕШЕСТВИЯ") {
  Field(search,{search=it},"Найти выход или ребёнка")
  FilterChip(selected=favorite,onClick={favorite=!favorite},label={Text("Только избранные")},leadingIcon={Glyph(R.drawable.ic_heart)})
  val trips=state.local.trips.filter {(!favorite || it.favorite) && (Scenario.valueOf(it.scenario).title+it.childName).contains(search,true)}
  if(trips.isEmpty()) Empty(R.drawable.ic_history,"Здесь будут ваши выходы","Сохраним списки и опыт, чтобы в следующий раз собраться ещё проще.")
  trips.forEach {TripCard(it,state.local.items) {open(it.id)}}
 }
}
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable fun TripScreen(id:String,state:AppState,vm:AppViewModel,back:()->Unit,open:(String)->Unit) {
 val trip=state.local.trips.find {it.id==id}
 if(trip==null) {Page("Выход не найден",back=back){Text("Возможно, он был удалён.")};return}
 val items=state.local.items.filter {it.tripId==id}
 var search by rememberSaveable(id) {mutableStateOf("")};var onlyUnpacked by rememberSaveable(id) {mutableStateOf(false)}
 var edit by remember {mutableStateOf<PackingItem?>(null)};var adding by remember {mutableStateOf(false)}
 var deleting by remember {mutableStateOf(false)};var quick by remember {mutableStateOf(false)}
 var bagSheet by remember {mutableStateOf(false)}
 val context=LocalContext.current
 Page(Scenario.valueOf(trip.scenario).title,kicker="${trip.childName.uppercase()} · ${trip.hours} ЧАСОВ",back=back,actions={
  IconButton(onClick={vm.trip(trip.copy(favorite=!trip.favorite))}) {Glyph(R.drawable.ic_heart,if(trip.favorite) "Убрать из избранного" else "В избранное",tint=if(trip.favorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)}
  IconButton(onClick={
   val text=buildString {append("${Scenario.valueOf(trip.scenario).title} · ${trip.childName}\n${formatTime(trip.departure)} · ${trip.hours} ч\n\n");items.forEach {append("${if(it.packed) "[✓]" else "[ ]"} ${it.name} — ${it.count}\n")}}
   context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {type="text/plain";putExtra(Intent.EXTRA_TEXT,text)},"Поделиться списком"))
  }) {Glyph(R.drawable.ic_share_2,"Поделиться списком")}
 }) {
  SoftCard(tone=MaterialTheme.colorScheme.primaryContainer) {
   Text(if(trip.completed) "Выход завершён" else "Собираемся без суеты",style=MaterialTheme.typography.titleLarge)
   Text("${formatTime(trip.departure)} · ${Transport.valueOf(trip.transport).title}",style=MaterialTheme.typography.bodySmall)
   Progress(items.count {it.packed},items.size)
   Button(onClick={quick=true},modifier=Modifier.fillMaxWidth()) {Glyph(R.drawable.ic_circle_check);Spacer(Modifier.width(10.dp));Text("Ничего не забыли?")}
  }
  Text(trip.weatherNote,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
  Field(search,{search=it},"Поиск вещей в списке")
  FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp),modifier=Modifier.fillMaxWidth()) {
   FilterChip(selected=onlyUnpacked,onClick={onlyUnpacked=!onlyUnpacked},label={Text("Осталось собрать")})
   TextButton(onClick={adding=true}) {Glyph(R.drawable.ic_plus);Text("Добавить")}
  }
  val filtered=items.filter {it.name.contains(search,true) && (!onlyUnpacked || !it.packed)}
  if(filtered.isEmpty()) Empty(R.drawable.ic_check,"Список просмотрен","Здесь нет вещей по выбранному фильтру.")
  filtered.groupBy {it.category}.forEach { (category,list) ->
   Text(category.uppercase(),style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.primary)
   list.forEach {item->
    SoftCard {
     Row(verticalAlignment=Alignment.CenterVertically) {
      Checkbox(checked=item.packed,onCheckedChange={vm.updateItem(item.copy(packed=it))})
      Column(Modifier.weight(1f).clickable {edit=item}.padding(horizontal=4.dp)) {Text("${item.name} · ${item.count}",style=MaterialTheme.typography.titleMedium);Text(item.reason,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
      IconButton(onClick={edit=item}) {Glyph(R.drawable.ic_pencil,"Изменить ${item.name}")}
     }
     if(trip.completed) {
      Text("Как пригодилась вещь?",style=MaterialTheme.typography.bodySmall)
      Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
       FilterChip(selected=item.feedback==1,onClick={vm.feedback(trip,item,if(item.feedback==1) 0 else 1)},label={Text("Не хватило")})
       FilterChip(selected=item.feedback == -1,onClick={vm.feedback(trip,item,if(item.feedback == -1) 0 else -1)},label={Text("Было лишним")})
      }
     }
     TextButton(onClick={vm.deleteItem(item)}) {Text("Удалить из списка")}
    }
   }
  }
  SoftCard {
   ActionRow(R.drawable.ic_backpack,"Сверить с сумкой","Отметить вещи из выбранной сумки") {bagSheet=true}
   ActionRow(R.drawable.ic_package,"Сохранить как шаблон сумки",onClick={vm.bagFromTrip(trip)})
   if(!trip.completed) ActionRow(R.drawable.ic_bell,"Напомнить перед выходом","Примерно за 30 минут") {vm.remind(trip)}
   ActionRow(R.drawable.ic_repeat_2,"Повторить выход","Новый список по текущим правилам") {vm.repeat(trip,open)}
  }
  if(!trip.completed) Button(onClick={vm.finish(trip)},modifier=Modifier.fillMaxWidth().heightIn(min=52.dp)) {Text("Завершить выход")}
  else Text("Отметьте выше, чего не хватило и что было лишним. Корректировки применятся к следующим спискам этого ребёнка. Расходники списываются вручную в разделе «Сумка».",style=MaterialTheme.typography.bodyMedium)
  TextButton(onClick={deleting=true}) {Text("Удалить выход",color=MaterialTheme.colorScheme.error)}
 }
 if(adding || edit!=null) ItemEditor(edit,id,vm::updateItem) {adding=false;edit=null}
 if(deleting) Confirm("Удалить выход?","Список и заметки об этом выходе будут удалены.",onDismiss={deleting=false}) {vm.deleteTrip(trip);back()}
 if(quick) {
  val missing=items.filter {!it.packed}
  AlertDialog(onDismissRequest={quick=false},icon={Glyph(if(missing.isEmpty()) R.drawable.ic_circle_check else R.drawable.ic_backpack)},title={Text(if(missing.isEmpty()) "Всё собрано" else "Осталось ${missing.size} вещей")},text={Text(if(missing.isEmpty()) "Проверьте ключи, телефон и документы. Хорошего вам выхода!" else missing.take(12).joinToString("\n") {"${it.name} · ${it.count}"}+if(missing.size>12) "\nИ ещё ${missing.size-12}…" else "")},confirmButton={TextButton(onClick={quick=false}){Text("Вернуться к списку")}})
 }
 if(bagSheet) ModalBottomSheet(onDismissRequest={bagSheet=false}) {
  Column(Modifier.padding(24.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
   Text("Какая сумка с вами?",style=MaterialTheme.typography.headlineSmall)
   Text("Сверим названия и количество собранных вещей. Списание запасов — вручную в сумке.",style=MaterialTheme.typography.bodyMedium)
   if(state.local.bags.isEmpty()) Text("Сначала создайте сумку в соответствующем разделе.")
   state.local.bags.forEach {bag->ActionRow(R.drawable.ic_backpack,bag.name,onClick={vm.packFromBag(trip,bag.id);bagSheet=false})}
  }
 }
}
