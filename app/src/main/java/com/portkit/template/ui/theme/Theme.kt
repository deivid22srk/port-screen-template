package com.portkit.template.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import com.portkit.template.branding.PortBranding

/**
 * Tema da tela: escuro cinematográfico construído a partir da config do port.
 * As únicas cores "de identidade" que entram no scheme são os acentos — o
 * restante são tokens fixos de contraste (Color.kt).
 */
@Composable
fun PortTemplateTheme(content: @Composable () -> Unit) {
    val cfg = PortBranding.current
    val scheme = darkColorScheme(
        primary = cfg.accentPrimary,
        onPrimary = BgDeep,
        secondary = cfg.accentSecondary,
        onSecondary = BgDeep,
        background = BgDeep,
        onBackground = TextPrimary,
        surface = BgDeep,
        onSurface = TextPrimary,
        onSurfaceVariant = TextSecondary,
    )
    MaterialTheme(
        colorScheme = scheme,
        typography = PortTypography,
        content = content,
    )
}
