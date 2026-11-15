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
private val destinations=listOf(Destination("home","Главная",R.drawable.ic_house),Destination("bags","Сумка",R.drawable.ic_backpack),Destination("history","История",R.drawable.ic_history),Destination("profile","Профиль",R.drawable.ic_user_round))
@Composable fun BabyRoot(vm:AppViewModel,state:AppState) {
 val snackbar=remember { SnackbarHostState() }
 LaunchedEffect(vm) { for(message in vm.messages) snackbar.showSnackbar(message) }
 val nav=rememberNavController()
 val entry by nav.currentBackStackEntryAsState();val route=entry?.destination?.route
 val main=destinations.any { it.route==route }
 BoxWithConstraints(Modifier.fillMaxSize()) {
  val wide=maxWidth>=700.dp
  Scaffold(snackbarHost={SnackbarHost(snackbar)},bottomBar={
   if(state.ready && state.settings.parent.isNotBlank() && state.local.children.isNotEmpty() && main && !wide) NavigationBar(containerColor=MaterialTheme.colorScheme.background) {
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
       composable("home") { HomeScreen(state,vm,onCreate={nav.navigate("create")},onWeather={nav.navigate("weather")},onTrip={nav.navigate("trip/$it")}) }
       composable("create") { CreateTripScreen(state,vm,{nav.popBackStack()}) {id -> nav.navigate("trip/$id") {popUpTo("create"){inclusive=true}}} }
       composable("trip/{id}") {b-> TripScreen(b.arguments?.getString("id").orEmpty(),state,vm,{nav.popBackStack()}) {id->nav.navigate("trip/$id")} }
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
@Composable fun HomeScreen(state:AppState,vm:AppViewModel,onCreate:()->Unit,onWeather:()->Unit,onTrip:(String)->Unit) {
 Page("Пойдём вместе",kicker="ДОБРЫЙ ДЕНЬ, ${state.settings.parent.uppercase()}") {
  Surface(onClick=onCreate,color=MaterialTheme.colorScheme.primaryContainer,shape=androidx.compose.foundation.shape.RoundedCornerShape(32.dp)) {
   Column(Modifier.padding(24.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
    Text("Маленький выход.\nБольшое спокойствие.",style=MaterialTheme.typography.headlineMedium)
    Row(verticalAlignment=Alignment.CenterVertically) {
     Text("Всё нужное для малыша — в одном продуманном списке.",style=MaterialTheme.typography.bodyMedium,modifier=Modifier.weight(1f))
     BagArt(Modifier.widthIn(max=112.dp))
    }
    Button(onClick=onCreate,modifier=Modifier.fillMaxWidth().heightIn(min=52.dp)) {Glyph(R.drawable.ic_plus);Spacer(Modifier.width(8.dp));Text("Подготовиться к выходу")}
   }
  }
  SoftCard {
   ActionRow(R.drawable.ic_cloud_rain,if(state.settings.weatherEnabled) state.settings.city.substringBefore(',') else "Погода по вашему желанию",state.weather?.takeIf {it.matches(state.settings)}?.let { "${it.at(System.currentTimeMillis())?.temperature?.toInt()?.toString() ?: "—"} °C · ${if(it.isStale()) "Устаревший прогноз" else "Сохранённый прогноз"}" } ?: "Без неё списки тоже работают",onWeather)
  }
  val active=state.local.trips.filter { !it.completed }
  Section("Ближайшие выходы")
  if(active.isEmpty()) Empty(R.drawable.ic_navigation,"Впереди что-то хорошее","Прогулка в парке или первая поездка? Соберём список под ваши планы.","Создать выход",onCreate)
  else active.take(3).forEach {TripCard(it,state.local.items) {onTrip(it.id)}}
  val favorites=state.local.trips.filter { it.favorite }
  if(favorites.isNotEmpty()) {Section("Любимые маршруты");favorites.take(3).forEach {t->SoftCard {ActionRow(R.drawable.ic_repeat_2,ru.withbaby.domain.Scenario.valueOf(t.scenario).title,"Повторить для ${t.childName}") {vm.repeat(t,onTrip)}}} }
  SoftCard(tone=MaterialTheme.colorScheme.secondaryContainer) { Text("Чуть меньше забот",style=MaterialTheme.typography.titleMedium);Text("Соберите постоянную сумку один раз. После выхода отмечайте использованное — так проще вовремя пополнить запасы.",style=MaterialTheme.typography.bodyMedium) }
 }
}
