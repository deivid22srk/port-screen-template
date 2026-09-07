package com.portkit.template.ui.motion

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/**
 * Estado de parallax compartilhado entre fundo, conteúdo e vidro do botão.
 *
 *  • tilt: acelerômetro suavizado por EMA (o aparelho "inclinado" move as camadas);
 *  • drag: arraste de toque com retorno por spring (feito no pointerInput do fundo).
 *
 * Os campos são lidos DENTRO de blocos graphicsLayer/Canvas na UI — mudanças
 * aqui atualizam propriedades de camada sem recompor a árvore (60 fps).
 */
class ParallaxState {
    var tiltX by mutableFloatStateOf(0f)
        internal set
    var tiltY by mutableFloatStateOf(0f)
        internal set
    var dragX by mutableFloatStateOf(0f)
        internal set
    var dragY by mutableFloatStateOf(0f)
        internal set

    var active: Boolean = false
        internal set

    /** Deslocamento total horizontal (px) para aplicar nas camadas. */
    fun offsetX(ampTiltPx: Float, ampDragPx: Float): Float =
        if (active) -tiltX * ampTiltPx + dragX * ampDragPx else 0f

    /** Deslocamento total vertical (px), amortecido para não cansar. */
    fun offsetY(ampTiltPx: Float, ampDragPx: Float): Float =
        if (active) tiltY * ampTiltPx * 0.6f + dragY * ampDragPx else 0f
}

/**
 * Registra o acelerômetro enquanto o parallax estiver ativo.
 * Com "reduzir movimento" ou parallax desligado, nenhum sensor fica registrado
 * (economia de bateria) e o offset volta a zero.
 */
@Composable
fun rememberParallaxState(enabled: Boolean): ParallaxState {
    val state = remember { ParallaxState() }
    val context = LocalContext.current

    DisposableEffect(enabled) {
        state.active = enabled
        if (!enabled) {
            state.tiltX = 0f
            state.tiltY = 0f
            state.dragX = 0f
            state.dragY = 0f
            onDispose { /* nada registrado */ }
        } else {
            val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val sensor: Sensor? = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    // EMA — suaviza o ruído sem alocar nada
                    val gx = (event.values[0] / SensorManager.GRAVITY_EARTH).coerceIn(-1f, 1f)
                    val gy = (event.values[1] / SensorManager.GRAVITY_EARTH).coerceIn(-1f, 1f)
                    state.tiltX += (gx - state.tiltX) * EMA_FACTOR
                    state.tiltY += (gy - state.tiltY) * EMA_FACTOR
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            if (sensor != null) {
                // Eventos chegam na main thread — seguro para mutableFloatStateOf
                manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
                onDispose { manager.unregisterListener(listener) }
            } else {
                // Sem acelerômetro: parallax fica só com o arraste de toque
                onDispose { /* nada registrado */ }
            }
        }
    }

    return state
}

private const val EMA_FACTOR = 0.12f
