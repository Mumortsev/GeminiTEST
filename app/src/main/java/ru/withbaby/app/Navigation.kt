package ru.withbaby.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import ru.withbaby.data.*
import androidx.navigation.NavGraph.Companion.findStartDestination

private data class Destination(val route:String,val title:String,val icon:Int)
private val destinations=listOf(Destination("home","Главная",R.drawable.ic_house),Destination("clothing","Одежда",R.drawable.ic_shirt),Destination("bags","Сумка",R.drawable.ic_backpack),Destination("history","История",R.drawable.ic_history),Destination("profile","Профиль",R.drawable.ic_user_round))
@Composable fun BabyRoot(vm:AppViewModel,state:AppState) {
 val snackbar=remember { SnackbarHostState() }
 LaunchedEffect(vm) { for(message in vm.messages) snackbar.showSnackbar(message) }
 val nav=rememberNavController()
 val entry by nav.currentBackStackEntryAsState();val route=entry?.destination?.route
 val main=destinations.any { it.route==route }
 BoxWithConstraints(Modifier.fillMaxSize()) {
  val wide=maxWidth>=700.dp
  Scaffold(modifier=Modifier.auroraGlow(),containerColor=androidx.compose.ui.graphics.Color.Transparent,snackbarHost={SnackbarHost(snackbar)},bottomBar={
   if(state.ready && state.settings.parent.isNotBlank() && state.local.children.isNotEmpty() && main && !wide) NavigationBar(containerColor=MaterialTheme.colorScheme.background.copy(alpha=.94f)) {
    destinations.forEach { d -> NavigationBarItem(selected=route==d.route,onClick={ nav.navigate(d.route) { popUpTo(nav.graph.findStartDestination().id) { saveState=true };launchSingleTop=true;restoreState=true } },icon={Glyph(d.icon)},label={Text(d.title)}) }
   }
  }) { padding ->
   Row(Modifier.fillMaxSize().padding(padding)) {
    if(wide && main && state.ready && state.local.children.isNotEmpty()) NavigationRail {
     Spacer(Modifier.height(24.dp));destinations.forEach {d->NavigationRailItem(selected=route==d.route,onClick={nav.navigate(d.route) {launchSingleTop=true}},icon={Glyph(d.icon)},label={Text(d.title)})}
    }
    Box(Modifier.weight(1f),contentAlignment=Alignment.TopCenter) {
     Box(Modifier.widthIn(max=840.dp).fillMaxSize()) {
      if(!state.ready) { Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){CircularProgressIndicator()} }
      else if(state.settings.parent.isBlank() || state.local.children.isEmpty()) { ChildEditor(null,true,vm,state,onClose={}) }
      else NavHost(navController=nav,startDestination="home") {
       composable("home") { HomeScreen(state,vm,onCreate={nav.navigate("create")},onWeather={nav.navigate("weather")},onClothing={nav.navigate("clothing")},onBags={nav.navigate("bags")},onTrip={nav.navigate("trip/$it")}) }
       composable("create") { CreateTripScreen(state,vm,{nav.popBackStack()}) {id -> nav.navigate("trip/$id") {popUpTo("create"){inclusive=true}}} }
       composable("trip/{id}") {b-> TripScreen(b.arguments?.getString("id").orEmpty(),state,vm,{nav.popBackStack()}) {id->nav.navigate("trip/$id")} }
       composable("clothing") { ClothingScreen(state) {nav.navigate("weather")} }
       composable("bags") { BagScreen(state,vm) }
       composable("history") { HistoryScreen(state,vm) {nav.navigate("trip/$it")} }
       composable("profile") { ProfileScreen(state,vm,{nav.navigate("weather")},{nav.navigate("privacy")},{nav.navigate("about")}) }
       composable("weather") { WeatherScreen(state,vm) {nav.popBackStack()} }
       composable("privacy") { DocumentScreen("Политика конфиденциальности",PrivacyText) {nav.popBackStack()} }
       composable("about") { DocumentScreen("О приложении",AboutText) {nav.popBackStack()} }
      }
     }
    }
   }
  }
 }
}
@Composable fun HomeScreen(state:AppState,vm:AppViewModel,onCreate:()->Unit,onWeather:()->Unit,onClothing:()->Unit,onBags:()->Unit,onTrip:(String)->Unit) {
 Page("Пойдём гулять?",kicker="ПРИВЕТ, ${state.settings.parent.uppercase()}") {
  AuroraCard {
   Text("Всё готово.\nМожно просто быть рядом.",style=MaterialTheme.typography.headlineMedium)
   Row(verticalAlignment=Alignment.CenterVertically) {
    Text("Подскажем, что взять с собой и как одеть малыша.",style=MaterialTheme.typography.bodyMedium,modifier=Modifier.weight(1f))
    BagArt(Modifier.widthIn(max=100.dp))
   }
   Button(onClick=onCreate,modifier=Modifier.fillMaxWidth().heightIn(min=54.dp)) {Glyph(R.drawable.ic_plus);Spacer(Modifier.width(8.dp));Text("Собраться на выход")}
  }
  SoftCard {
   ActionRow(R.drawable.ic_shirt,"Как одеть малыша","Готовый комплект по погоде",onClothing)
   HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant.copy(alpha=.5f))
   ActionRow(R.drawable.ic_backpack,"Моя сумка","Что уже собрано и что пополнить",onBags)
  }
  val active=state.local.trips.filter { !it.completed }
  if(active.isNotEmpty()) {Section("Ваши планы");active.take(3).forEach {TripCard(it,state.local.items) {onTrip(it.id)}}}
  else Empty(R.drawable.ic_navigation,"Пока без планов","Выберите, куда идёте. Список нужных вещей составим за вас.","Создать выход",onCreate)
  val forecast=state.weather?.takeIf {state.settings.weatherEnabled && it.matches(state.settings)}
  ActionRow(R.drawable.ic_cloud_rain,if(state.settings.weatherEnabled && state.settings.city.isNotBlank()) "Погода · ${state.settings.city.substringBefore(',')}" else "Посмотреть погоду",
   forecast?.let { "${it.at(System.currentTimeMillis())?.temperature?.toInt()?.toString() ?: "—"} °C · ${if(it.isStale()) "прогноз устарел" else "сохранено ${formatTime(it.fetched)}"}" } ?: "Можно выбрать город или обойтись без прогноза",onWeather)
 }
}
