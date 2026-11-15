package ru.withbaby.app

import ru.withbaby.data.*
import ru.withbaby.domain.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class ClothingOption(val reference:String,val name:String,val group:GarmentGroup,val warmth:Int,val imageKey:String,val photo:String?,val owned:Boolean,val suitable:Boolean,val detail:String)
fun clothingOptions(child:Child,weather:ClothingWeather?,owned:List<WardrobeItem>):List<ClothingOption> {
 val months=ChronoUnit.MONTHS.between(LocalDate.parse(child.birthday),LocalDate.now())
 val t=weather?.let {it.feelsLike ?: it.temperature}
 fun appropriate(w:Int)=t==null || when(w) {0->t>=18;1->t in 5.0..26.0;2->t in -8.0..16.0;else->t<10}
 val inventory=owned.filter {it.childId==child.id}
 val personal=inventory.map {item->
  val type=GarmentCatalog.find(item.catalogId)
  val baseMatches=if(type!=null && type.warmth==item.warmth) t==null || type.matches(t,months) else appropriate(item.warmth)
  val matches=baseMatches && (t==null || when(item.season) {"Лето"->t>=18;"Зима"->t<10;"Весна / осень"->t in -2.0..22.0;"Для дома"->false;else->true})
  ClothingOption(item.catalogId?.let {"catalog:$it"} ?: "owned:${item.id}",item.name,GarmentGroup.valueOf(item.group),item.warmth,item.imageKey,item.photo,true,matches,listOf(item.size.takeIf {it.isNotBlank()}?.let {"Размер $it"},item.season,listOf("Лёгкая","Обычная","Тёплая","Утеплённая")[item.warmth]).filterNotNull().joinToString(" · "))
 }
 val basic=GarmentCatalog.all.map {type->ClothingOption("catalog:${type.id}",type.name,type.group,type.warmth,type.id,null,inventory.any {it.catalogId==type.id},t==null || type.matches(t,months),type.warmthLabel)}
 return (personal+basic.filterNot {b->personal.any {it.reference==b.reference}}).sortedWith(compareByDescending<ClothingOption> {it.suitable}.thenByDescending {it.owned}.thenBy {it.name})
}
fun automaticOutfit(child:Child,weather:ClothingWeather,mode:WalkMode,options:List<ClothingOption>):Map<GarmentGroup,ClothingOption> {
 val t=weather.feelsLike ?: weather.temperature
 val months=ChronoUnit.MONTHS.between(LocalDate.parse(child.birthday),LocalDate.now())
 val defaults=linkedMapOf<GarmentGroup,String>()
 if(months<6) defaults[GarmentGroup.ONE]=if(t>=23) "romper" else "sleepsuit"
 else {defaults[GarmentGroup.TOP]=if(t>=20) {if(months<12) "body_short" else "tshirt"} else {if(months<12) "body_long" else "longsleeve"};defaults[GarmentGroup.BOTTOM]=if(t>=23) "shorts" else "baby_pants"}
 if(t<20 && (t<10 || mode!=WalkMode.ACTIVE)) defaults[GarmentGroup.MID]=if(t<10) "fleece_jacket" else "cardigan"
 if(t<15 || weather.wind>=15 || weather.rain>.2) defaults[GarmentGroup.OUTER]=when {t<0->"winter_suit";t<10->"demi_suit";else->"windbreaker"}
 if(t<15 || t>=26 || weather.uv>=3) defaults[GarmentGroup.HEAD]=when {t<5->"warm_hat";t<15->"beanie";else->"sunhat"}
 if(t<26) defaults[GarmentGroup.SOCKS]=if(t<5) "warm_socks" else "socks"
 if(mode==WalkMode.ACTIVE && months>=9) defaults[GarmentGroup.SHOES]=when {t<5->"winter_boots";t<12->"demi_boots";t>=25->"sandals";else->"sneakers"}
 else if(t<15) defaults[GarmentGroup.SHOES]=if(t<5) "warm_booties" else "booties"
 if(t<5) defaults[GarmentGroup.HANDS]="mittens"
 // A footmuff is a substitute for part of insulation, not an extra mandatory layer.
 return defaults.mapValues {(group,id)-> options.firstOrNull {it.group==group && it.owned && it.suitable} ?: options.first {it.reference=="catalog:$id"}}
}
fun selectedOutfit(child:Child,automatic:Map<GarmentGroup,ClothingOption>,options:List<ClothingOption>,saved:List<OutfitChoice>):Map<GarmentGroup,ClothingOption?> {
 val choices=saved.filter {it.childId==child.id}.associate {it.slot to it.reference}
 val result=linkedMapOf<GarmentGroup,ClothingOption?>().apply {putAll(automatic)}
 val one=choices["ONE"]?.takeIf {it.isNotBlank() && it!="none"}?.let {r->options.find {it.reference==r}}
 val separates=listOf("TOP","BOTTOM").any {choices[it]?.let {r->r.isNotBlank() && r!="none"}==true}
 if(one!=null) {result.remove(GarmentGroup.TOP);result.remove(GarmentGroup.BOTTOM);result[GarmentGroup.ONE]=one}
 else if(separates) {
  result.remove(GarmentGroup.ONE)
  for(group in listOf(GarmentGroup.TOP,GarmentGroup.BOTTOM)) result[group]=options.firstOrNull {it.group==group && it.suitable} ?: options.first {it.group==group}
 }
 choices.forEach {(slot,ref)->
  val group=runCatching {GarmentGroup.valueOf(slot)}.getOrNull() ?: return@forEach
  if(one!=null && group in listOf(GarmentGroup.TOP,GarmentGroup.BOTTOM) || separates && group==GarmentGroup.ONE) return@forEach
  if(ref=="none") result[group]=null
  else options.find {it.reference==ref && it.group==group}?.let {result[group]=it}
 }
 val outer=result[GarmentGroup.OUTER]
 if(outer?.imageKey in listOf("winter_jacket","demi_jacket") && GarmentGroup.OUTER_BOTTOM !in result) {
  val pants=if(outer?.imageKey=="winter_jacket") "snow_pants" else "demi_pants"
  result[GarmentGroup.OUTER_BOTTOM]=options.firstOrNull {it.group==GarmentGroup.OUTER_BOTTOM && it.owned && it.suitable} ?: options.find {it.reference=="catalog:$pants"}
 }
 return result
}
