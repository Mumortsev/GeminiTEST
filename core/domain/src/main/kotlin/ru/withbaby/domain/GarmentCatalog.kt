package ru.withbaby.domain

// Original local catalog. Temperatures are editable everyday guides, not medical limits.
enum class GarmentGroup(val title:String) { TOP("Верх основы"), BOTTOM("Низ основы"), ONE("Цельная одежда"), MID("Средний слой"), OUTER("Верхняя одежда"), OUTER_BOTTOM("Низ верхней одежды"), HEAD("Головные уборы"), SOCKS("Носки"), SHOES("Обувь"), HANDS("Рукавички"), STROLLER("Для коляски") }
data class GarmentType(val id:String,val name:String,val group:GarmentGroup,val warmth:Int,val minC:Double,val maxC:Double,val minMonths:Int=0) {
 fun matches(t:Double,months:Long)=t in minC..maxC && months>=minMonths
 val warmthLabel:String get()=listOf("Лёгкая","Обычная","Тёплая","Утеплённая")[warmth.coerceIn(0,3)]
}
object GarmentCatalog {
 val all=listOf(
  GarmentType("body_short","Боди с коротким рукавом",GarmentGroup.TOP,0,20.0,60.0,0),
  GarmentType("body_long","Боди с длинным рукавом",GarmentGroup.TOP,1,-60.0,22.0,0),
  GarmentType("body_wrap","Боди на запах",GarmentGroup.TOP,1,-60.0,24.0,0),
  GarmentType("body_sleeveless","Боди без рукавов",GarmentGroup.TOP,0,24.0,60.0,0),
  GarmentType("undershirt","Распашонка",GarmentGroup.TOP,1,-60.0,25.0,0),
  GarmentType("tshirt","Футболка",GarmentGroup.TOP,0,20.0,60.0,3),
  GarmentType("longsleeve","Лонгслив",GarmentGroup.TOP,1,-60.0,22.0,3),
  GarmentType("tank","Майка",GarmentGroup.TOP,0,25.0,60.0,6),
  GarmentType("shirt","Рубашка",GarmentGroup.TOP,1,15.0,26.0,6),
  GarmentType("thermal_top","Термолонгслив",GarmentGroup.TOP,2,-60.0,8.0,6),
  GarmentType("sliders","Ползунки с закрытой стопой",GarmentGroup.BOTTOM,1,-60.0,23.0,0),
  GarmentType("baby_pants","Мягкие штанишки",GarmentGroup.BOTTOM,1,-60.0,25.0,0),
  GarmentType("leggings","Легинсы",GarmentGroup.BOTTOM,1,-60.0,24.0,3),
  GarmentType("trousers","Брюки",GarmentGroup.BOTTOM,1,5.0,25.0,6),
  GarmentType("joggers","Джоггеры",GarmentGroup.BOTTOM,1,-5.0,22.0,6),
  GarmentType("shorts","Шорты",GarmentGroup.BOTTOM,0,22.0,60.0,6),
  GarmentType("bloomers","Трусики-блумеры",GarmentGroup.BOTTOM,0,24.0,60.0,0),
  GarmentType("thermal_bottom","Термолегинсы",GarmentGroup.BOTTOM,2,-60.0,8.0,6),
  GarmentType("tights","Колготки",GarmentGroup.BOTTOM,1,-60.0,18.0,3),
  GarmentType("skirt","Юбка",GarmentGroup.BOTTOM,0,18.0,30.0,6),
  GarmentType("sleepsuit","Хлопковый слип",GarmentGroup.ONE,1,-60.0,23.0,0),
  GarmentType("romper","Песочник",GarmentGroup.ONE,0,22.0,60.0,0),
  GarmentType("cotton_overall","Хлопковый комбинезон",GarmentGroup.ONE,1,-60.0,23.0,0),
  GarmentType("dress","Платье",GarmentGroup.ONE,0,20.0,35.0,6),
  GarmentType("dungarees","Полукомбинезон",GarmentGroup.ONE,1,10.0,25.0,6),
  GarmentType("pyjamas","Пижама",GarmentGroup.ONE,1,10.0,25.0,3),
  GarmentType("cardigan","Кардиган",GarmentGroup.MID,1,12.0,23.0,0),
  GarmentType("sweatshirt","Свитшот",GarmentGroup.MID,1,10.0,22.0,3),
  GarmentType("hoodie","Толстовка с капюшоном",GarmentGroup.MID,1,8.0,20.0,3),
  GarmentType("fleece_jacket","Флисовая кофта",GarmentGroup.MID,2,-60.0,15.0,0),
  GarmentType("sweater","Вязаный джемпер",GarmentGroup.MID,2,-60.0,15.0,3),
  GarmentType("vest","Жилет",GarmentGroup.MID,1,5.0,20.0,3),
  GarmentType("fleece_suit","Флисовый комбинезон",GarmentGroup.MID,2,-60.0,12.0,0),
  GarmentType("wool_suit","Шерстяная поддёва",GarmentGroup.MID,2,-60.0,10.0,0),
  GarmentType("windbreaker","Ветровка",GarmentGroup.OUTER,0,12.0,24.0,3),
  GarmentType("raincoat","Дождевик",GarmentGroup.OUTER,0,10.0,26.0,6),
  GarmentType("rain_suit","Непромокаемый комбинезон",GarmentGroup.OUTER,1,5.0,20.0,0),
  GarmentType("demi_suit","Демисезонный комбинезон",GarmentGroup.OUTER,2,-2.0,14.0,0),
  GarmentType("demi_jacket","Демисезонная куртка",GarmentGroup.OUTER,2,0.0,15.0,6),
  GarmentType("winter_suit","Зимний комбинезон",GarmentGroup.OUTER,3,-60.0,4.0,0),
  GarmentType("winter_jacket","Зимняя куртка",GarmentGroup.OUTER,3,-60.0,4.0,6),
  GarmentType("snow_pants","Утеплённый полукомбинезон",GarmentGroup.OUTER_BOTTOM,3,-60.0,5.0,6),
  GarmentType("demi_pants","Демисезонные непромокаемые брюки",GarmentGroup.OUTER_BOTTOM,2,0.0,15.0,6),
  GarmentType("rain_pants","Дождевые брюки",GarmentGroup.OUTER_BOTTOM,0,10.0,25.0,9),
  GarmentType("bonnet","Хлопковый чепчик",GarmentGroup.HEAD,1,10.0,20.0,0),
  GarmentType("beanie","Тонкая шапка",GarmentGroup.HEAD,1,5.0,18.0,0),
  GarmentType("warm_hat","Тёплая шапка",GarmentGroup.HEAD,3,-60.0,8.0,0),
  GarmentType("balaclava","Шапка-шлем",GarmentGroup.HEAD,3,-60.0,5.0,3),
  GarmentType("sunhat","Панама",GarmentGroup.HEAD,0,18.0,60.0,0),
  GarmentType("cap","Кепка",GarmentGroup.HEAD,0,18.0,60.0,9),
  GarmentType("socks","Тонкие носки",GarmentGroup.SOCKS,1,0.0,28.0,0),
  GarmentType("warm_socks","Тёплые носки",GarmentGroup.SOCKS,3,-60.0,12.0,0),
  GarmentType("booties","Мягкие пинетки",GarmentGroup.SHOES,1,10.0,25.0,0),
  GarmentType("warm_booties","Утеплённые пинетки",GarmentGroup.SHOES,3,-60.0,10.0,0),
  GarmentType("sandals","Сандалии",GarmentGroup.SHOES,0,22.0,60.0,9),
  GarmentType("sneakers","Кроссовки",GarmentGroup.SHOES,1,10.0,26.0,9),
  GarmentType("demi_boots","Демисезонные ботинки",GarmentGroup.SHOES,2,0.0,15.0,9),
  GarmentType("winter_boots","Зимние ботинки",GarmentGroup.SHOES,3,-60.0,5.0,9),
  GarmentType("rain_boots","Резиновые сапоги",GarmentGroup.SHOES,1,8.0,22.0,12),
  GarmentType("mittens","Тёплые варежки",GarmentGroup.HANDS,3,-60.0,5.0,0),
  GarmentType("gloves","Лёгкие перчатки",GarmentGroup.HANDS,1,0.0,12.0,18),
  GarmentType("scratch_mitts","Хлопковые рукавички",GarmentGroup.HANDS,0,10.0,25.0,0),
  GarmentType("footmuff","Конверт для коляски",GarmentGroup.STROLLER,3,-60.0,8.0,0),
  GarmentType("blanket","Лёгкий плед",GarmentGroup.STROLLER,1,8.0,20.0,0),
  GarmentType("stroller_rain","Дождевик для коляски",GarmentGroup.STROLLER,0,-5.0,25.0,0),
  GarmentType("sunshade","Козырёк от солнца",GarmentGroup.STROLLER,0,15.0,60.0,0),
 )
 fun find(id:String?)=all.find {it.id==id}
}
data class IllustrationType(val id:String,val name:String)
object BagIllustrations {
 val all=listOf(
  IllustrationType("bag_diaper","Подгузник"),
  IllustrationType("bag_wipes","Влажные салфетки"),
  IllustrationType("bag_bags","Пакеты для использованных вещей"),
  IllustrationType("bag_swaddle","Пелёнка"),
  IllustrationType("bag_bottle","Бутылочка"),
  IllustrationType("bag_formula","Смесь"),
  IllustrationType("bag_water","Вода"),
  IllustrationType("bag_snack","Перекус"),
  IllustrationType("bag_cup","Поильник"),
  IllustrationType("bag_toy","Игрушка"),
  IllustrationType("bag_documents","Документы"),
  IllustrationType("bag_medical_notes","Медицинские записи"),
  IllustrationType("bag_bib","Нагрудник"),
  IllustrationType("bag_towel","Полотенце"),
  IllustrationType("bag_swim","Купальные принадлежности"),
  IllustrationType("bag_nursing_pads","Вкладыши для кормления"),
  IllustrationType("bag_pacifier","Пустышка"),
  IllustrationType("bag_cream","Крем"),
  IllustrationType("bag_thermos","Термос"),
  IllustrationType("bag_changing_mat","Коврик для переодевания"),
  IllustrationType("bag_stroller","Коляска"),
  IllustrationType("bag_carrier","Переноска"),
  IllustrationType("bag_carseat","Автокресло"),
  IllustrationType("bag_medicine","Назначенные средства"),
  IllustrationType("bag_phone","Телефон"),
  IllustrationType("bag_keys","Ключи"),
  IllustrationType("bag_container","Контейнер"),
  IllustrationType("bag_bag","Своя вещь")
 )
 fun infer(name:String):String {
  val n=name.lowercase()
  return when {
   "подгуз" in n->"bag_diaper";"салфет" in n->"bag_wipes";"бутыл" in n->"bag_bottle";"смеси" in n || "смесь" in n->"bag_formula"
   "дождевик" in n->"stroller_rain";"панам" in n->"sunhat";"козыр" in n->"sunshade";"пижам" in n->"pyjamas";"одежд" in n->"sleepsuit"
   "вкладыш" in n->"bag_nursing_pads";"перекус" in n->"bag_snack";"поильник" in n->"bag_cup";"вод" in n->"bag_water";"игруш" in n->"bag_toy"
   "полотен" in n->"bag_towel";"пелён" in n || "пелен" in n->"bag_swaddle";"пакет" in n->"bag_bags";"документ" in n->"bag_documents"
   "медицин" in n->"bag_medical_notes";"нагруд" in n->"bag_bib";"купаль" in n->"bag_swim";"коляск" in n->"bag_stroller";"переноск" in n->"bag_carrier"
   "автокрес" in n->"bag_carseat";"слой" in n->"cardigan";"сна" in n->"pyjamas";"пустыш" in n->"bag_pacifier";"крем" in n->"bag_cream"
   "назначен" in n->"bag_medicine";"термос" in n->"bag_thermos";"телефон" in n->"bag_phone";"ключ" in n->"bag_keys";else->"bag_bag"
  }
 }
}
