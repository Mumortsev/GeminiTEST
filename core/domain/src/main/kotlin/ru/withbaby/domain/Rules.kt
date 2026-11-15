package ru.withbaby.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.ceil

enum class Feeding(val title: String) { BREAST("Грудное"), FORMULA("Смесь"), MIXED("Смешанное"), SOLIDS("Общий стол") }
enum class Scenario(val title: String) {
 WALK("Прогулка"), SHOP("Магазин"), DOCTOR("Поликлиника"), VISIT("В гости"), CAFE("Кафе"), PARK("Парк"), CAR("Поездка на автомобиле"), TRANSIT("Общественный транспорт"), TRAVEL("Путешествие"), NIGHT("Ночёвка вне дома")
}
enum class Transport(val title: String) { STROLLER("Коляска"), CARRIER("Слинг / переноска"), CAR("Автомобиль"), TRANSIT("Общественный транспорт"), WALK("Пешком") }
data class Need(val name: String, val count: Int, val group: String, val reason: String)
data class Conditions(val temperature: Double, val rain: Double, val wind: Double, val uv: Double)
data class RuleConfig(val version: Int = 1, val diaperHours: Double = 3.0, val spareSetsHours: Double = 6.0)

/** Pure deterministic rules. Quantity estimates are editable, never medical advice. */
object PackingRules {
 fun generate(birthday: LocalDate, feeding: Feeding, hours: Int, scenario: Scenario, transport: Transport,
   needs: String, conditions: Conditions?, swimming: Boolean, sleep: Boolean, adjustments: Map<String, Int>,
   config: RuleConfig = RuleConfig(), today: LocalDate = LocalDate.now()): List<Need> {
  require(hours in 1..336)
  val months = ChronoUnit.MONTHS.between(birthday, today).coerceAtLeast(0)
  val result = linkedMapOf<String, Need>()
  fun add(n: String, q: Int, g: String, r: String) { result[n] = Need(n, q.coerceIn(1, 500), g, r) }
  add("Подгузники", ceil(hours / config.diaperHours).toInt() + 1, "Гигиена", "Примерно один на ${config.diaperHours} ч и один запасной")
  add("Влажные салфетки", ceil(hours / 24.0).toInt(), "Гигиена", "Упаковка для переодевания")
  add("Пакеты для использованных вещей", ceil(hours / 6.0).toInt(), "Гигиена", "Отдельное хранение")
  add("Сменная одежда", ceil(hours / config.spareSetsHours).toInt(), "Одежда", "Комплект на случай переодевания")
  add("Пелёнка", if (months < 6) 2 else 1, "Гигиена", "Чистая поверхность")
  if (feeding == Feeding.BREAST || feeding == Feeding.MIXED) add("Вкладыши для кормления", ceil(hours / 4.0).toInt(), "Питание", "По привычному режиму родителя")
  if (feeding == Feeding.FORMULA || feeding == Feeding.MIXED) {
   add("Порции смеси", ceil(hours / 3.0).toInt(), "Питание", "Оценка; уточните по привычному режиму")
   add("Бутылочки", ceil(hours / 3.0).toInt().coerceAtMost(6), "Питание", "Чистые бутылочки")
   add("Вода для приготовления смеси", ceil(hours / 6.0).toInt(), "Питание", "Готовьте по инструкции производителя")
  }
  if (months >= 6) { add("Привычный перекус", ceil(hours / 4.0).toInt(), "Питание", "С учётом введённых продуктов"); add("Поильник / вода", 1, "Питание", "По привычным потребностям") }
  add("Любимая игрушка", 1, "Комфорт", "Знакомая вещь вне дома")
  if (sleep || scenario == Scenario.NIGHT || hours >= 8) { add("Вещи для сна", 1, "Комфорт", "Привычный ритуал сна"); add("Пижама", ceil(hours / 24.0).toInt(), "Одежда", "Для ночёвки") }
  if (transport == Transport.CAR || scenario == Scenario.CAR || scenario == Scenario.TRAVEL) add("Автокресло по возрасту", 1, "В дороге", "Проверьте установку перед выездом")
  if (transport == Transport.STROLLER) add("Коляска", 1, "В дороге", "Выбранный способ передвижения")
  if (transport == Transport.CARRIER) add("Переноска", 1, "В дороге", "Выбранный способ передвижения")
  if (scenario == Scenario.DOCTOR || scenario == Scenario.TRAVEL) { add("Документы ребёнка", 1, "Важное", "Полис и документы по месту назначения"); add("Медицинские записи", 1, "Важное", "При необходимости") }
  if (scenario == Scenario.CAFE || scenario == Scenario.VISIT) add("Нагрудник", 1, "Питание", "Для еды вне дома")
  if (swimming) { add("Полотенце", 1, "Одежда", "Выбраны водные занятия"); add("Купальные принадлежности", 1, "Одежда", "По правилам места") }
  conditions?.let {
   if (it.rain > 0.2) add("Дождевик", 1, "Погода", "В прогнозе осадки")
   if (it.temperature < 12 || it.wind > 25) add("Дополнительный слой одежды", 1, "Погода", "Прохладно или ветрено; подберите по ощущениям")
   if (it.temperature > 22 || it.uv >= 3) { add("Панама", 1, "Погода", "Защита от солнца"); add("Солнцезащитный козырёк", 1, "Погода", "Продумайте тень и перерывы") }
  }
  needs.split(';', '\n').map { it.trim() }.filter { it.isNotBlank() }.forEach { add(it, 1, "Личное", "Из профиля ребёнка") }
  adjustments.forEach { (name, delta) -> result[name]?.let { result[name] = it.copy(count = (it.count + delta).coerceAtLeast(1), reason = it.reason + "; учтён прошлый опыт") } }
  return result.values.toList()
 }
}

/** Future remote identity/sync adapters implement these ports; no network identity in v1. */
interface IdentityProvider { suspend fun localIdentity(): String }
interface SyncGateway { suspend fun sync() }
