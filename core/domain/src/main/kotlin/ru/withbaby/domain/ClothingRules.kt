package ru.withbaby.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class WalkMode(val title:String) { STROLLER("В коляске"), ACTIVE("Идёт / играет"), CARRIER("В переноске") }
data class ClothingWeather(val temperature:Double,val feelsLike:Double?=null,val wind:Double=0.0,val rain:Double=0.0,val uv:Double=0.0)
data class ClothingPiece(val area:String,val name:String,val detail:String)
data class Outfit(val title:String,val pieces:List<ClothingPiece>,val takeAlong:List<String>,val note:String,val caution:String?)

/** Local everyday clothing suggestions, not medical guidance or a safety threshold. */
object ClothingRules {
 fun suggest(birthday:LocalDate,weather:ClothingWeather,mode:WalkMode,today:LocalDate=LocalDate.now()):Outfit {
  require(!birthday.isAfter(today))
  require(weather.temperature.isFinite() && weather.temperature in -60.0..60.0)
  require(weather.feelsLike==null || weather.feelsLike.isFinite() && weather.feelsLike in -80.0..80.0)
  require(weather.wind.isFinite() && weather.wind>=0 && weather.rain.isFinite() && weather.rain>=0 && weather.uv.isFinite() && weather.uv>=0)
  val months=ChronoUnit.MONTHS.between(birthday,today)
  val t=weather.feelsLike ?: weather.temperature
  val pieces=mutableListOf<ClothingPiece>()
  val extra=mutableListOf<String>()
  val active=mode==WalkMode.ACTIVE
  fun add(area:String,name:String,detail:String) {pieces+=ClothingPiece(area,name,detail)}
  when {
   t>=26 -> {
    add("Основа",if(months<12) "Лёгкое хлопковое боди" else "Лёгкая футболка и шорты","Тонкая дышащая ткань, свободный крой")
    add("На голову","Лёгкая панама","На солнце; выбирайте тень")
    add("На ноги",if(active) "Лёгкая удобная обувь" else "Без лишнего утепления","Смотрите по условиям и ощущениям малыша")
   }
   t>=20 -> {
    add("Основа",if(months<12) "Боди и лёгкие штанишки" else "Футболка и лёгкие брюки","Хлопок или другая дышащая ткань")
    add("На ноги",if(active) "Носки и лёгкая обувь" else "Тонкие носочки","Без плотного утепления")
    if(weather.uv>=3) add("На голову","Панама","Для участков на солнце")
    if(!active) extra+="Лёгкая кофточка: пригодится, если станет прохладнее"
   }
   t>=15 -> {
    add("Основа",if(months<12) "Боди с длинным рукавом и штанишки" else "Лонгслив и брюки","Мягкая ткань без плотного утепления")
    add("Поверх",if(active) "Лёгкая кофта" else "Лёгкая кофта или кардиган","Съёмный слой: удобно снять при потеплении")
    add("На ноги",if(active) "Носки и закрытая обувь" else "Носочки или мягкие пинетки","По размеру, без тесной резинки")
   }
   t>=10 -> {
    add("Основа",if(months<12) "Боди с длинным рукавом и штанишки" else "Лонгслив и брюки","Комфортный нижний слой")
    if(!active) add("Средний слой","Кофта или тонкий флис","Съёмный слой для спокойной прогулки")
    add("Снаружи","Лёгкий демисезонный комбинезон или куртка","Толщину выбирайте по условиям и одежде под ним")
    add("На голову","Лёгкая шапка","По ветру и ощущениям ребёнка")
    add("На ноги",if(active) "Носки и закрытая обувь" else "Носочки и пинетки","Без сдавливания")
   }
   t>=5 -> {
    add("Основа","Боди или лонгслив и штанишки","Мягкий нижний слой")
    add("Средний слой",if(active) "Лёгкая кофта" else "Кофта или флис","Слой, который можно снять")
    add("Снаружи","Демисезонный комбинезон или комплект","С защитой от ветра")
    add("На голову","Шапка по погоде","Прикрывает уши, не давит")
    add("На ноги",if(active) "Носки и демисезонная обувь" else "Тёплые пинетки","Оставьте место для движения пальцев")
   }
   else -> {
    add("Основа","Боди или лонгслив и штанишки","Сухой мягкий нижний слой")
    add("Средний слой",if(active) "Кофта по погоде" else "Флисовый или шерстяной слой","Толщина зависит от верхней одежды")
    add("Снаружи",if(t<0) "Утеплённый комбинезон или зимний комплект" else "Утеплённый демисезонный комплект","Сверьтесь с рекомендациями производителя одежды")
    add("На голову и руки","Тёплая шапка и варежки","Без тугих завязок и длинных свободных шарфов")
    add("На ноги",if(active) "Тёплые носки и обувь по сезону" else "Тёплые носки и утеплённые пинетки","Обувь и слои не должны сдавливать стопу")
   }
  }
  if(weather.wind>=15 || weather.rain>0.2) {
   if(t>=15) add("От ветра и осадков","Лёгкая ветровка или непромокаемый верх","Подберите по температуре; снимайте в помещении")
   if(weather.rain>0.2) extra+=if(mode==WalkMode.STROLLER) "Дождевик при осадках — с вентиляцией, не закрывая коляску наглухо" else "Защита от дождя по погоде"
  }
  if(weather.uv>=3 && t<26) extra+="Тень или козырёк на участках с ярким солнцем"
  if(mode==WalkMode.STROLLER && t<10) extra+="Конверт для коляски по погоде: учитывайте его утепление, не добавляйте все слои сразу"
  extra+="Сухой запасной комплект"
  val note=when(mode) {
   WalkMode.STROLLER -> "В коляске малыш двигается меньше. Начните с этого набора и проверяйте, комфортно ли ему; конверт тоже сохраняет тепло."
   WalkMode.ACTIVE -> "Для подвижной прогулки удобны съёмные слои. Если ребёнок вспотел, пересмотрите утепление; на остановке может понадобиться кофта."
   WalkMode.CARRIER -> "В переноске учитывайте тепло взрослого и ткань переноски. Не добавляйте утепление автоматически; лицо и дыхательные пути должны оставаться открытыми."
  } + if(months<6) " Для самых маленьких особенно важны мягкая одежда и частая проверка комфорта." else ""
  val caution=when {
   t<=-15 -> "В сильный мороз универсального набора недостаточно. Оцените условия и индивидуальные потребности ребёнка; приложение не определяет безопасную длительность прогулки."
   t>=30 -> "В жару одежда не заменяет тень и прохладное место. Избегайте лишних слоёв и не накрывайте коляску тканью."
   else -> null
  }
  return Outfit(when {t>=26->"Легко и свободно";t>=20->"Лёгкий комплект";t>=15->"Съёмные лёгкие слои";t>=5->"Для прохладной прогулки";else->"Тёплые слои по погоде"},pieces,extra.distinct(),note,caution)
 }
}
