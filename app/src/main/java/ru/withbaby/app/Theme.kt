package ru.withbaby.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val light=lightColorScheme(primary=Color(0xFF466857), onPrimary=Color(0xFFFFFFFF), primaryContainer=Color(0xFFDDE9DC), onPrimaryContainer=Color(0xFF263F30),
 secondary=Color(0xFF85654B), secondaryContainer=Color(0xFFF0E1D2), onSecondaryContainer=Color(0xFF4A3424),
 background=Color(0xFFF9F8F3),onBackground=Color(0xFF252D28), surface=Color(0xFFF9F8F3),onSurface=Color(0xFF252D28),surfaceVariant=Color(0xFFEEEFE6),onSurfaceVariant=Color(0xFF697169),outline=Color(0xFF81897F),outlineVariant=Color(0xFFDEE1D7))
private val dark=darkColorScheme(primary=Color(0xFFB0CFB6),onPrimary=Color(0xFF183525),primaryContainer=Color(0xFF304D3B),onPrimaryContainer=Color(0xFFDDE9DC),
 secondary=Color(0xFFDEBEA5),secondaryContainer=Color(0xFF554332),onSecondaryContainer=Color(0xFFF3E2D2),
 background=Color(0xFF171D19),onBackground=Color(0xFFE4E9DF),surface=Color(0xFF171D19),onSurface=Color(0xFFE4E9DF),surfaceVariant=Color(0xFF29322B),onSurfaceVariant=Color(0xFFB7C0B4),outlineVariant=Color(0xFF39453A))
private val family=FontFamily(Font(R.font.manrope))
private fun type(size:Int,weight:FontWeight=FontWeight.Normal)=TextStyle(fontFamily=family,fontSize=size.sp,lineHeight=(size*1.4).sp,fontWeight=weight)
@Composable fun BabyTheme(theme:String,content:@Composable ()->Unit) {
 MaterialTheme(colorScheme=if(theme=="Тёмная" || theme=="Системная" && isSystemInDarkTheme()) dark else light,
 typography=Typography(displaySmall=type(36,FontWeight.SemiBold),headlineLarge=type(30,FontWeight.SemiBold),headlineMedium=type(26,FontWeight.SemiBold),headlineSmall=type(22,FontWeight.SemiBold),titleLarge=type(20,FontWeight.SemiBold),titleMedium=type(16,FontWeight.SemiBold),titleSmall=type(14,FontWeight.SemiBold),bodyLarge=type(16),bodyMedium=type(14),bodySmall=type(12),labelLarge=type(14,FontWeight.SemiBold),labelMedium=type(12,FontWeight.SemiBold),labelSmall=type(11)),content=content)
}
