package com.moyarossiya.tv.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val MoyaGold = Color(0xFFD9A441)
val MoyaRed = Color(0xFFE03E2D)
val MoyaBlue = Color(0xFF2F6DB3)
val MoyaText = Color(0xFFEEF3FA)
val MoyaMuted = Color(0xFFA8B8D0)
val MoyaNavy = Color(0xFF131C2E)
val MoyaCard = Color(0xFF1D2A42)
val MoyaCardLight = Color(0xFF2C3D5C)

private val Colors = darkColorScheme(
    primary = MoyaGold,
    onPrimary = MoyaNavy,
    secondary = MoyaBlue,
    background = MoyaNavy,
    onBackground = MoyaText,
    surface = MoyaCard,
    onSurface = MoyaText,
    surfaceVariant = MoyaCardLight,
    onSurfaceVariant = MoyaMuted,
    error = MoyaRed,
)

private val TvTypography = Typography(
    headlineMedium = Typography().headlineMedium.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 40.sp,
        fontWeight = FontWeight.Bold,
    ),
    titleLarge = Typography().titleLarge.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 30.sp,
        fontWeight = FontWeight.Bold,
    ),
    titleMedium = Typography().titleMedium.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 23.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    bodyLarge = Typography().bodyLarge.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    ),
    bodyMedium = Typography().bodyMedium.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 17.sp,
        lineHeight = 24.sp,
    ),
    labelLarge = Typography().labelLarge.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
    ),
)

@Composable
fun MoyaTvTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Colors,
        typography = TvTypography,
        content = content,
    )
}
