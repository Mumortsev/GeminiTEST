package ru.withbaby.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val blueLight=lightColorScheme(
 primary=Color(0xFF355C9B),onPrimary=Color.White,primaryContainer=Color(0xFFDDEBFF),onPrimaryContainer=Color(0xFF193761),
 secondary=Color(0xFF69529A),secondaryContainer=Color(0xFFECE5FA),onSecondaryContainer=Color(0xFF382656),
 tertiary=Color(0xFF347C8E),background=Color(0xFFF5F8FF),onBackground=Color(0xFF202C42),
 surface=Color(0xFFF5F8FF),onSurface=Color(0xFF202C42),surfaceVariant=Color(0xFFEAF0FC),onSurfaceVariant=Color(0xFF52617B),
 surfaceContainer=Color(0xFFEDF3FF),surfaceContainerHigh=Color(0xFFE4EDFC),surfaceContainerLow=Color(0xFFFAFCFF),
 outline=Color(0xFF73819A),outlineVariant=Color(0xFFD1DEEF))
private val pinkLight=lightColorScheme(
 primary=Color(0xFF994466),onPrimary=Color.White,primaryContainer=Color(0xFFFFE0EB),onPrimaryContainer=Color(0xFF652740),
 secondary=Color(0xFF7956A0),secondaryContainer=Color(0xFFF0E5FA),onSecondaryContainer=Color(0xFF4B3067),
 tertiary=Color(0xFF9E547F),background=Color(0xFFFFF6FA),onBackground=Color(0xFF3A2530),
 surface=Color(0xFFFFF6FA),onSurface=Color(0xFF3A2530),surfaceVariant=Color(0xFFF9EAF1),onSurfaceVariant=Color(0xFF7B5667),
 surfaceContainer=Color(0xFFFCECF3),surfaceContainerHigh=Color(0xFFF5E1EB),surfaceContainerLow=Color(0xFFFFFAFC),
 outline=Color(0xFF9B7889),outlineVariant=Color(0xFFEBCFD9))
private val blueDark=darkColorScheme(
 primary=Color(0xFFB8C8FF),onPrimary=Color(0xFF1D2E67),primaryContainer=Color(0xFF2C356B),onPrimaryContainer=Color(0xFFE3E8FF),
 secondary=Color(0xFFD1BDF3),secondaryContainer=Color(0xFF3B2D60),onSecondaryContainer=Color(0xFFF0E6FF),
 tertiary=Color(0xFF95CFEA),background=Color(0xFF11132A),onBackground=Color(0xFFE7E8FC),
 surface=Color(0xFF11132A),onSurface=Color(0xFFE7E8FC),surfaceVariant=Color(0xFF202541),onSurfaceVariant=Color(0xFFB9C1E1),
 surfaceContainer=Color(0xFF1B203B),surfaceContainerHigh=Color(0xFF292F50),surfaceContainerLow=Color(0xFF171B32),
 outline=Color(0xFF838DB4),outlineVariant=Color(0xFF3C4265))
private val pinkDark=darkColorScheme(
 primary=Color(0xFFFFB4D4),onPrimary=Color(0xFF57203B),primaryContainer=Color(0xFF602B47),onPrimaryContainer=Color(0xFFFFDFED),
 secondary=Color(0xFFDABBF4),secondaryContainer=Color(0xFF493055),onSecondaryContainer=Color(0xFFF5E3FF),
 tertiary=Color(0xFFF0B2D7),background=Color(0xFF24121F),onBackground=Color(0xFFFBE3EF),
 surface=Color(0xFF24121F),onSurface=Color(0xFFFBE3EF),surfaceVariant=Color(0xFF3B2334),onSurfaceVariant=Color(0xFFDEB9CD),
 surfaceContainer=Color(0xFF331E2D),surfaceContainerHigh=Color(0xFF492A3D),surfaceContainerLow=Color(0xFF2B1725),
 outline=Color(0xFFAE809A),outlineVariant=Color(0xFF674154))
private val family=FontFamily(Font(R.font.manrope))
private fun type(size:Int,weight:FontWeight=FontWeight.Normal)=TextStyle(fontFamily=family,fontSize=size.sp,lineHeight=(size*1.4).sp,fontWeight=weight)
@Composable fun BabyTheme(theme:String,palette:String="blue",content:@Composable ()->Unit) {
 val dark=theme=="Тёмная" || theme=="Системная" && isSystemInDarkTheme()
 val colors=if(palette=="pink") {if(dark) pinkDark else pinkLight} else {if(dark) blueDark else blueLight}
 MaterialTheme(colorScheme=colors,
 typography=Typography(displaySmall=type(36,FontWeight.SemiBold),headlineLarge=type(30,FontWeight.SemiBold),headlineMedium=type(26,FontWeight.SemiBold),headlineSmall=type(22,FontWeight.SemiBold),titleLarge=type(20,FontWeight.SemiBold),titleMedium=type(16,FontWeight.SemiBold),titleSmall=type(14,FontWeight.SemiBold),bodyLarge=type(16),bodyMedium=type(14),bodySmall=type(12),labelLarge=type(14,FontWeight.SemiBold),labelMedium=type(12,FontWeight.SemiBold),labelSmall=type(11)),content=content)
}

/** Soft radial light, rendered on every supported Android version without blur layers. */
@Composable fun Modifier.auroraGlow(base:Color=MaterialTheme.colorScheme.background,card:Boolean=false):Modifier {
 val colors=MaterialTheme.colorScheme
 val isDark=colors.background.luminance()<.5f
 val first=colors.primary.copy(alpha=if(isDark) .19f else .13f)
 val second=colors.secondary.copy(alpha=if(isDark) .16f else .10f)
 val third=colors.tertiary.copy(alpha=if(isDark) .10f else .08f)
 return drawWithCache {
  val radius=size.width*(if(card) .9f else 1.1f)
  val p1=Offset(size.width*.06f,size.height*(if(card) .08f else .04f))
  val p2=Offset(size.width*.98f,size.height*(if(card) .75f else .28f))
  val p3=Offset(size.width*.25f,size.height*(if(card) .95f else .65f))
  val b1=Brush.radialGradient(listOf(first,Color.Transparent),p1,radius)
  val b2=Brush.radialGradient(listOf(second,Color.Transparent),p2,radius*.9f)
  val b3=Brush.radialGradient(listOf(third,Color.Transparent),p3,radius*.8f)
  onDrawBehind {drawRect(base);drawCircle(b1,radius,p1);drawCircle(b2,radius*.9f,p2);drawCircle(b3,radius*.8f,p3)}
 }
}

@Composable fun PaletteChooser(selected:String,onSelect:(String)->Unit) {
 androidx.compose.foundation.layout.Column(verticalArrangement=androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)) {
  Text("Цвет приложения",style=MaterialTheme.typography.titleMedium)
  androidx.compose.foundation.layout.Row(horizontalArrangement=androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)) {
   listOf("blue","pink").forEach {key->
    val chosen=key==selected
    Surface(onClick={onSelect(key)},modifier=Modifier.weight(1f),shape=androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
     color=MaterialTheme.colorScheme.surfaceVariant.copy(alpha=.9f),border=androidx.compose.foundation.BorderStroke(if(chosen) 2.dp else 1.dp,if(chosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)) {
     androidx.compose.foundation.layout.Column(Modifier.padding(14.dp),verticalArrangement=androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
      androidx.compose.foundation.layout.Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically,horizontalArrangement=androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)) {
       listOf(if(key=="blue") Color(0xFFBFDFFF) else Color(0xFFFFC9DF),if(key=="blue") Color(0xFF34376D) else Color(0xFF743D5C)).forEach {color->
        Surface(modifier=Modifier.size(22.dp),shape=androidx.compose.foundation.shape.CircleShape,color=color) {}
       }
       if(chosen) Glyph(R.drawable.ic_check,"Выбранная палитра",modifier=Modifier.size(20.dp),tint=MaterialTheme.colorScheme.primary)
      }
      Text(if(key=="blue") "Голубая" else "Розовая",style=MaterialTheme.typography.titleSmall)
      Text(if(key=="blue") "Небо / индиго" else "Пудра / тёмная роза",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
     }
    }
   }
  }
 }
}
