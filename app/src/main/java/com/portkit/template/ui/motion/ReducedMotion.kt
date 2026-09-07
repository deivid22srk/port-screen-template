package com.portkit.template.ui.motion

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.portkit.template.viewmodel.ScreenSettings

/**
 * Acessibilidade — "reduzir movimento".
 *
 * Considera reduzido quando:
 *  • o usuário ativou a opção manual nas configurações da tela, OU
 *  • o sistema está com animações zeradas (o toggle "remover animações" da
 *    acessibilidade do Android zera ANIMATOR_DURATION_SCALE / TRANSITION_ANIMATION_SCALE).
 *
 * Quando reduzido: parallax off, partículas viram quadro estático, loops
 * (glow/pulso) desligam e as entradas viram fade de 120 ms.
 */
fun isSystemReducedMotion(context: Context): Boolean {
    val resolver = context.contentResolver
    val animator = Settings.Global.getFloat(
        resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f
    )
    val transition = Settings.Global.getFloat(
        resolver, Settings.Global.TRANSITION_ANIMATION_SCALE, 1f
    )
    return animator == 0f || transition == 0f
}

@Composable
fun rememberReducedMotion(settings: ScreenSettings): Boolean {
    val context = LocalContext.current
    var systemReduced by remember { mutableStateOf(isSystemReducedMotion(context)) }

    DisposableEffect(Unit) {
        val resolver = context.contentResolver
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                systemReduced = isSystemReducedMotion(context)
            }
        }
        resolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE), false, observer
        )
        resolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.TRANSITION_ANIMATION_SCALE), false, observer
        )
        onDispose { resolver.unregisterContentObserver(observer) }
    }

    return settings.reducedMotionManual || systemReduced
}
