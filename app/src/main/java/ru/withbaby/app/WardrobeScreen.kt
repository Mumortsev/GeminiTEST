package ru.withbaby.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.withbaby.data.*
import ru.withbaby.domain.*

@OptIn(ExperimentalMaterial3Api::class,ExperimentalLayoutApi::class)
@Composable fun ClothingCarousel(child:Child,group:GarmentGroup,base:Boolean,options:List<ClothingOption>,selected:Map<GarmentGroup,ClothingOption?>,vm:AppViewModel,dismiss:()->Unit) {
 var tab by remember {mutableStateOf(group)}
 var onlyOwned by remember {mutableStateOf(false)}
 val filtered=options.filter {it.group==tab && (!onlyOwned || it.owned)}
 ModalBottomSheet(onDismissRequest=dismiss) {
  Column(Modifier.fillMaxWidth().heightIn(max=600.dp).verticalScroll(rememberScrollState()).padding(horizontal=24.dp).padding(bottom=24.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
   Text(if(base) "Выберите основу" else tab.title,style=MaterialTheme.typography.headlineSmall)
   if(base) FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
    listOf(GarmentGroup.TOP to "Верх",GarmentGroup.BOTTOM to "Низ",GarmentGroup.ONE to "Цельная").forEach {(g,label)->FilterChip(selected=tab==g,onClick={tab=g},label={Text(label)})}
   }
   FilterChip(selected=onlyOwned,onClick={onlyOwned=!onlyOwned},label={Text("Только из нашего гардероба")})
   key(tab,onlyOwned) {
    val index=filtered.indexOfFirst {it.reference==selected[tab]?.reference}.coerceAtLeast(0)
    val pager=rememberPagerState(initialPage=index,pageCount={filtered.size})
    if(filtered.isEmpty()) Text("Пока нет таких вещей. Отключите фильтр или добавьте одежду в гардероб.")
    else {
     HorizontalPager(state=pager,contentPadding=PaddingValues(horizontal=20.dp),pageSpacing=12.dp,modifier=Modifier.fillMaxWidth().height(260.dp)) {page->
      val option=filtered[page]
      Surface(shape=RoundedCornerShape(24.dp),color=MaterialTheme.colorScheme.surfaceContainerHigh,modifier=Modifier.fillMaxSize()) {
       Column(Modifier.padding(16.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(10.dp)) {
        ItemPicture(option.imageKey,option.photo,option.name,Modifier.size(160.dp))
        Text(option.name,style=MaterialTheme.typography.titleMedium)
        Text(if(option.owned) "Есть у вас" else "Наличие не отмечено",style=MaterialTheme.typography.bodySmall)
       }
      }
     }
     val current=filtered.getOrNull(pager.currentPage)
     Text("${pager.currentPage+1} / ${filtered.size} · листайте влево и вправо",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
     if(current!=null) {
      Text(current.detail,style=MaterialTheme.typography.bodySmall)
      if(!current.suitable) Text("Другой вариант: проверьте возраст, утепление и погоду. Это отличается от автоматического подбора.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
      Button(onClick={vm.choose(child.id,tab.name,current.reference);if(base && tab==GarmentGroup.TOP) tab=GarmentGroup.BOTTOM else dismiss()},modifier=Modifier.fillMaxWidth()) {Text(if(base && tab==GarmentGroup.TOP) "Выбрать верх и перейти к низу" else "Выбрать эту вещь")}
     }
    }
   }
   if(!base) TextButton(onClick={vm.choose(child.id,tab.name,"none");dismiss()}) {Text("Без этого слоя")}
  }
 }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun WardrobeScreen(initialChild:String,state:AppState,vm:AppViewModel,back:()->Unit) {
 var childId by rememberSaveable {mutableStateOf(initialChild)}
 val child=state.local.children.find {it.id==childId} ?: state.local.children.first()
 var query by rememberSaveable {mutableStateOf("")}
 var groupName by rememberSaveable {mutableStateOf("ALL")}
 var ownedOnly by rememberSaveable {mutableStateOf(false)}
 var adding by remember {mutableStateOf(false)}
 var edit by remember {mutableStateOf<WardrobeItem?>(null)}
 var preview by remember {mutableStateOf<GarmentType?>(null)}
 val wardrobe=state.wardrobe.filter {it.childId==child.id}
 val rows=buildList {
  GarmentCatalog.all.filter {(groupName=="ALL" || it.group.name==groupName) && (it.name+" "+wardrobe.find {w->w.catalogId==it.id}?.name.orEmpty()).contains(query,true)}.forEach {type->
   val existing=wardrobe.find {it.catalogId==type.id}
   if(!ownedOnly || existing!=null) add(type to existing)
  }
  wardrobe.filter {it.catalogId==null && (groupName=="ALL" || it.group==groupName) && it.name.contains(query,true)}.forEach {add(null to it)}
 }
 LazyVerticalGrid(columns=GridCells.Adaptive(140.dp),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp),horizontalArrangement=Arrangement.spacedBy(12.dp),modifier=Modifier.fillMaxSize()) {
  item(span={GridItemSpan(maxLineSpan)}) {Row(verticalAlignment=Alignment.CenterVertically) {IconButton(onClick=back) {Glyph(R.drawable.ic_arrow_left,"Назад")};Column {Text("ГАРДЕРОБ РЕБЁНКА",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary);Text("Одежда ${child.name}",style=MaterialTheme.typography.headlineMedium)}}}
  if(state.local.children.size>1) item(span={GridItemSpan(maxLineSpan)}) {Picker("Ребёнок",child,state.local.children,{it.name},{childId=it.id})}
  item(span={GridItemSpan(maxLineSpan)}) {Text("Отметьте, что уже есть. Нажмите на вещь, чтобы рассмотреть её или указать размер.",style=MaterialTheme.typography.bodyMedium)}
  item(span={GridItemSpan(maxLineSpan)}) {Button(onClick={adding=true},modifier=Modifier.fillMaxWidth()) {Glyph(R.drawable.ic_plus);Spacer(Modifier.width(8.dp));Text("Добавить свою одежду")}}
  item(span={GridItemSpan(maxLineSpan)}) {Field(query,{query=it},"Найти одежду")}
  item(span={GridItemSpan(maxLineSpan)}) {Picker("Раздел",groupName,listOf("ALL")+GarmentGroup.entries.map {it.name},{if(it=="ALL") "Весь гардероб" else GarmentGroup.valueOf(it).title},{groupName=it})}
  item(span={GridItemSpan(maxLineSpan)}) {FilterChip(selected=ownedOnly,onClick={ownedOnly=!ownedOnly},label={Text("Только то, что есть у нас · ${wardrobe.size}")})}
  items(rows,key={it.first?.id ?: it.second!!.id}) {(type,item)->
   val name=item?.name ?: type!!.name
   Surface(shape=RoundedCornerShape(20.dp),color=MaterialTheme.colorScheme.surfaceContainer,modifier=Modifier.fillMaxWidth()) {
    Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
     ItemPicture(item?.imageKey ?: type!!.id,item?.photo,name,Modifier.fillMaxWidth().height(125.dp).clickable {if(item!=null) edit=item else preview=type})
     Text(name,style=MaterialTheme.typography.titleSmall)
     if(item?.size?.isNotBlank()==true) Text("Размер ${item.size}",style=MaterialTheme.typography.bodySmall)
     Row(verticalAlignment=Alignment.CenterVertically) {
      Checkbox(checked=item!=null,onCheckedChange={enabled->if(type!=null) {if(enabled) vm.own(child.id,type,true) else item?.let(vm::removeWardrobe)} else if(!enabled) item?.let(vm::removeWardrobe)})
      Text("Есть у нас",style=MaterialTheme.typography.bodySmall)
     }
     if(item!=null) TextButton(onClick={edit=item}) {Text("Изменить")}
    }
   }
  }
  if(rows.isEmpty()) item(span={GridItemSpan(maxLineSpan)}) {Empty(R.drawable.ic_shirt,"Ничего не найдено","Измените запрос или добавьте свою вещь.")}
 }
 if(adding || edit!=null) WardrobeEditor(edit,child.id,vm) {adding=false;edit=null}
 preview?.let {type->AlertDialog(onDismissRequest={preview=null},title={Text(type.name)},text={Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {ItemPicture(type.id,null,type.name,Modifier.fillMaxWidth().height(220.dp));Text("${type.group.title} · ${type.warmthLabel}")}},confirmButton={TextButton(onClick={vm.own(child.id,type,true);preview=null}) {Text("Есть у нас")}},dismissButton={TextButton(onClick={preview=null}) {Text("Закрыть")}})}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun WardrobeEditor(item:WardrobeItem?,childId:String,vm:AppViewModel,dismiss:()->Unit) {
 var name by rememberSaveable {mutableStateOf(item?.name ?: "")}
 var group by rememberSaveable {mutableStateOf(item?.group ?: GarmentGroup.TOP.name)}
 var size by rememberSaveable {mutableStateOf(item?.size ?: "")}
 var season by rememberSaveable {mutableStateOf(item?.season ?: "Любой сезон")}
 var warmth by rememberSaveable {mutableIntStateOf(item?.warmth ?: 1)}
 var imageKey by rememberSaveable {mutableStateOf(item?.imageKey ?: "body_long")}
 var photo by rememberSaveable {mutableStateOf(item?.photo)}
 ModalBottomSheet(onDismissRequest=dismiss) {
  Box(Modifier.fillMaxHeight(.92f)) {
   Page(if(item==null) "Своя одежда" else "Изменить вещь",kicker="ГАРДЕРОБ",back=dismiss) {
    PictureField(imageKey,photo,name,{key,file->imageKey=key ?: "body_long";photo=file},vm::message)
    Field(name,{name=it.take(120)},"Название")
    Picker("Группа",GarmentGroup.valueOf(group),GarmentGroup.entries.toList(),{it.title},{group=it.name;if(photo==null) imageKey=GarmentCatalog.all.first {g->g.group==it}.id})
    Field(size,{size=it.take(40)},"Размер (необязательно)")
    Picker("Сезон",season,listOf("Любой сезон","Лето","Весна / осень","Зима","Для дома"),{it},{season=it})
    Picker("Утепление",warmth,listOf(0,1,2,3),{listOf("Лёгкая","Обычная","Тёплая","Утеплённая")[it]},{warmth=it})
    Button(enabled=name.isNotBlank(),onClick={vm.wardrobe(WardrobeItem(item?.id ?: newId(),childId,item?.catalogId?.takeIf {GarmentCatalog.find(it)?.group?.name==group},name.trim(),group,size.trim(),season,warmth,imageKey,photo));dismiss()},modifier=Modifier.fillMaxWidth()) {Text("Сохранить в гардероб")}
    if(item!=null) TextButton(onClick={vm.removeWardrobe(item);dismiss()}) {Text("Убрать из гардероба")}
   }
  }
 }
}
