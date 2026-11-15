package ru.withbaby.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.withbaby.data.*

@Composable fun BagScreen(state:AppState,vm:AppViewModel) {
 var selected by rememberSaveable {mutableStateOf("")};val bag=state.local.bags.find {it.id==selected} ?: state.local.bags.firstOrNull()
 var create by remember {mutableStateOf(false)};var rename by remember {mutableStateOf(false)};var deleting by remember {mutableStateOf(false)}
 var add by remember {mutableStateOf(false)};var edit by remember {mutableStateOf<Stock?>(null)}
 var search by rememberSaveable {mutableStateOf("")}
 Page("Сумка малыша",kicker="ПУСТЬ НУЖНОЕ БУДЕТ РЯДОМ",actions={IconButton(onClick={create=true}) {Glyph(R.drawable.ic_plus,"Новая сумка")}}) {
  if(bag==null) Empty(R.drawable.ic_backpack,"Ваша первая сумка","Сохраните постоянный набор и следите за запасами.","Создать сумку") {create=true}
  else {
   if(state.local.bags.size>1) Picker("Моя сумка",bag,state.local.bags,{it.name},{selected=it.id})
   else Text(bag.name,style=MaterialTheme.typography.titleLarge)
   val stock=state.local.stock.filter {it.bagId==bag.id};val low=stock.filter {it.count<it.minimum}
   SoftCard(tone=MaterialTheme.colorScheme.primaryContainer) {
    Row(verticalAlignment=Alignment.CenterVertically) {Column(Modifier.weight(1f)) {Text(if(low.isEmpty()) "Всё под рукой" else "Пора пополнить",style=MaterialTheme.typography.headlineSmall);Text(if(low.isEmpty()) "Отметьте вещи, которые уже лежат в сумке." else low.joinToString {it.name},style=MaterialTheme.typography.bodyMedium)};Glyph(R.drawable.ic_backpack,modifier=Modifier.size(48.dp))}
    Progress(stock.count {it.packed && it.count>0},stock.size)
   }
   Text("Галочка — вещь в сумке. «Использовали 1» уменьшает запас, «+1» пополняет его.",style=MaterialTheme.typography.bodyMedium)
   Field(search,{search=it},"Найти вещь в сумке")
   Button(onClick={add=true},modifier=Modifier.fillMaxWidth()) {Glyph(R.drawable.ic_plus);Spacer(Modifier.width(8.dp));Text("Добавить вещь")}
   val filtered=stock.filter {it.name.contains(search,true)}
   if(filtered.isEmpty()) Empty(R.drawable.ic_package,"Здесь пока пусто","Добавьте вещи или измените запрос поиска.")
   filtered.forEach {s->
    SoftCard {
     Row(verticalAlignment=Alignment.CenterVertically) {
      Checkbox(checked=s.packed,onCheckedChange={vm.stock(s.copy(packed=it))})
      Column(Modifier.weight(1f)) {Text(s.name,style=MaterialTheme.typography.titleMedium);Text("В наличии: ${s.count} · минимум ${s.minimum}",style=MaterialTheme.typography.bodySmall,color=if(s.count<s.minimum) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)}
      IconButton(onClick={edit=s}){Glyph(R.drawable.ic_pencil,"Изменить ${s.name}")}
     }
     Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
      OutlinedButton(modifier=Modifier.weight(1f),contentPadding=PaddingValues(horizontal=8.dp),enabled=s.count>0,onClick={vm.consume(s)}) {Text("Использовали 1")}
      FilledTonalButton(modifier=Modifier.width(48.dp),contentPadding=PaddingValues(0.dp),enabled=s.count<9999,onClick={vm.stock(s.copy(count=s.count+1))}) {Text("+1")}
     }
    }
   }
   ActionRow(R.drawable.ic_pencil,"Переименовать сумку",onClick={rename=true})
   TextButton(onClick={deleting=true}) {Text("Удалить сумку",color=MaterialTheme.colorScheme.error)}
  }
 }
 if(create) NameDialog("Новая сумка",dismiss={create=false}) {val b=Bag(newId(),it);vm.saveBag(b);selected=b.id}
 if(rename && bag!=null) NameDialog("Название сумки",bag.name,{rename=false}) {vm.saveBag(bag.copy(name=it))}
 if(deleting && bag!=null) Confirm("Удалить сумку?","Все запасы этого шаблона будут удалены.",onDismiss={deleting=false}) {vm.deleteBag(bag);deleting=false}
 if((add || edit!=null) && bag!=null) StockEditor(edit,bag.id,vm::stock,onDelete=vm::deleteStock) {add=false;edit=null}
}
