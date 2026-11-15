package ru.withbaby.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.SideEffect
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.luminance
import androidx.core.view.WindowCompat
import java.util.Locale

class MainActivity: ComponentActivity() {
 override fun attachBaseContext(base: android.content.Context) {
  val locale=Locale.forLanguageTag("ru")
  Locale.setDefault(locale)
  val config=android.content.res.Configuration(base.resources.configuration).apply {setLocale(locale)}
  super.attachBaseContext(base.createConfigurationContext(config))
 }
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  Locale.setDefault(Locale.forLanguageTag("ru"))
  enableEdgeToEdge()
  setContent {
   val vm:AppViewModel=viewModel();val state by vm.state.collectAsStateWithLifecycle()
   BabyTheme(state.settings.theme,state.settings.palette) {
    val lightBars=MaterialTheme.colorScheme.background.luminance()>.5f
    SideEffect { WindowCompat.getInsetsController(window,window.decorView).apply {isAppearanceLightStatusBars=lightBars;isAppearanceLightNavigationBars=lightBars} }
    BabyRoot(vm,state)
   }
  }
 }
}
