package ru.withbaby.app

import android.Manifest
import android.os.Build
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.withbaby.data.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun ProfileScreen(state:AppState,vm:AppViewModel,weather:()->Unit,privacy:()->Unit,about:()->Unit) {
 var child by remember {mutableStateOf<Child?>(null)};var editChild by remember {mutableStateOf(false)}
 var deleteChild by remember {mutableStateOf<Child?>(null)};var parent by remember {mutableStateOf(false)};var deleting by remember {mutableStateOf(false)}
 var dataExpanded by rememberSaveable {mutableStateOf(false)}
 var backupMode by remember {mutableStateOf<String?>(null)};var pendingPassword by remember {mutableStateOf("")}
 val context=LocalContext.current
 val export=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) {uri->if(uri!=null && pendingPassword.length>=8) vm.backup(uri,pendingPassword);pendingPassword=""}
 val restore=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {uri->if(uri!=null && pendingPassword.length>=8) vm.restore(uri,pendingPassword);pendingPassword=""}
 val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {granted-> vm.saveSettings(state.settings.copy(notifications=granted));if(!granted) vm.message("Уведомления не разрешены. Их можно включить в настройках Android")}
 Page("Профиль",kicker="ВАША СЕМЬЯ") {
  SoftCard(tone=MaterialTheme.colorScheme.primaryContainer) {ActionRow(R.drawable.ic_user_round,state.settings.parent,"Имя родителя") {parent=true}}
  Section("Дети", "Добавить") {child=null;editChild=true}
  state.local.children.forEach {c->SoftCard {
   ActionRow(R.drawable.ic_baby,c.name,"${c.birthday} · ${ru.withbaby.domain.Feeding.valueOf(c.feeding).title}") {child=c;editChild=true}
   TextButton(onClick={deleteChild=c}) {Text("Удалить профиль",color=MaterialTheme.colorScheme.error)}
  }}
  Section("Оформление")
  Picker("Тема",state.settings.theme,listOf("Системная","Светлая","Тёмная"),{it},{vm.setTheme(it)})
  PaletteChooser(state.settings.palette) {vm.setPalette(it)}
  SoftCard {
   Toggle("Напоминания",state.settings.notifications,{enabled->
    if(enabled && Build.VERSION.SDK_INT>=33 && ContextCompat.checkSelfPermission(context,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) permission.launch(Manifest.permission.POST_NOTIFICATIONS)
    else vm.saveSettings(state.settings.copy(notifications=enabled))
   },"Напоминания о выходе и пополнении расходников")
   Toggle("Погода",state.settings.weatherEnabled,{enabled->if(enabled && state.settings.city.isBlank()) weather() else vm.saveSettings(state.settings.copy(weatherEnabled=enabled))},"Запросы к Open-Meteo только при включённой погоде")
   ActionRow(R.drawable.ic_map_pin,"Место и прогноз",state.settings.city.ifBlank {"Город ещё не выбран"},weather)
  }
  TextButton(onClick={dataExpanded=!dataExpanded}) {Text(if(dataExpanded) "Скрыть управление данными" else "Копии и удаление данных")}
  if(dataExpanded) {
  SoftCard {
   ActionRow(R.drawable.ic_download,"Сохранить резервную копию","Зашифрованный файл с вашим паролем") {backupMode="export"}
   ActionRow(R.drawable.ic_upload,"Восстановить из копии","Заменяет текущие профили, поездки и сумки") {backupMode="restore"}
  }
  Text("Копия создаётся через системное меню Android. Вы сами выбираете, куда сохранить файл. Если забудете пароль, восстановить копию будет невозможно.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
  }
  SoftCard {
   ActionRow(R.drawable.ic_shield_check,"Политика конфиденциальности",onClick=privacy)
   ActionRow(R.drawable.ic_settings,"О приложении",onClick=about)
  }
  if(dataExpanded) TextButton(onClick={deleting=true}) {Text("Удалить все данные",color=MaterialTheme.colorScheme.error)}
 }
 if(editChild) ModalBottomSheet(onDismissRequest={editChild=false}) {Box(Modifier.fillMaxHeight(.94f)) {ChildEditor(child,false,vm,state) {editChild=false}}}
 if(parent) NameDialog("Как к вам обращаться?",state.settings.parent,{parent=false}) {vm.saveSettings(state.settings.copy(parent=it))}
 deleteChild?.let {c->Confirm("Удалить профиль ${c.name}?","Удалятся также выходы и накопленные корректировки этого ребёнка.",onDismiss={deleteChild=null}) {vm.deleteChild(c);deleteChild=null}}
 if(deleting) Confirm("Удалить все личные данные?","Будут удалены профили, поездки, запасы, настройки и сохранённая погода. Экспортированные вами файлы копий нужно удалить отдельно.",onDismiss={deleting=false}) {vm.clear();deleting=false}
 backupMode?.let {mode->
  var password by remember {mutableStateOf("")}
  AlertDialog(onDismissRequest={backupMode=null},title={Text(if(mode=="export") "Защитите резервную копию" else "Восстановить данные?")},text={Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
   Text(if(mode=="export") "Придумайте пароль из 8 или более символов. Сохраните его в надёжном месте." else "Текущие данные будут заменены данными из копии. Введите пароль, заданный при её создании.")
   OutlinedTextField(value=password,onValueChange={password=it},label={Text("Пароль копии")},visualTransformation=PasswordVisualTransformation(),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Password),singleLine=true)
  }},confirmButton={TextButton(enabled=password.length>=8,onClick={pendingPassword=password;backupMode=null;if(mode=="export") export.launch("С-малышом-${java.time.LocalDate.now()}.baby") else restore.launch(arrayOf("application/octet-stream","*/*"))}) {Text("Выбрать файл")}},dismissButton={TextButton(onClick={backupMode=null}){Text("Отмена")}})
 }
}
