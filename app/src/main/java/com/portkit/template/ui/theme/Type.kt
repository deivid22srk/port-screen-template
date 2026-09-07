package com.portkit.template.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.portkit.template.R

/*
 * Tipografia do template: Barlow Condensed (display) + Barlow (texto).
 * Famílias OFL — para um novo port basta trocar os arquivos em res/font/ e
 * renomeá-los (nenhum composable precisa mudar).
 */

val BarlowFamily = FontFamily(
    Font(R.font.barlow_regular, FontWeight.Normal),
    Font(R.font.barlow_medium, FontWeight.Medium),
    Font(R.font.barlow_semibold, FontWeight.SemiBold),
)

val BarlowDisplayFamily = FontFamily(
    Font(R.font.barlow_condensed_semibold, FontWeight.SemiBold),
    Font(R.font.barlow_condensed_bold, FontWeight.Bold),
)

/** Escala restrita: 12 / 15 / 17 / 22 / 46 sp (spec em docs/DESIGN_SPEC.md §5). */
val PortTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = BarlowDisplayFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 46.sp,
        letterSpacing = 0.12.em,
    ),
    titleMedium = TextStyle(
        fontFamily = BarlowFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        letterSpacing = 0.02.em,
    ),
    labelLarge = TextStyle(
        fontFamily = BarlowFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        letterSpacing = 0.08.em,
    ),
    bodyLarge = TextStyle(
        fontFamily = BarlowFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        letterSpacing = 0.01.em,
    ),
    bodySmall = TextStyle(
        fontFamily = BarlowFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = 0.02.em,
    ),
    labelSmall = TextStyle(
        fontFamily = BarlowFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.04.em,
    ),
)
