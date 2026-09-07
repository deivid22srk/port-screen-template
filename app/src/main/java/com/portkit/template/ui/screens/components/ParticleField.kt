package com.portkit.template.ui.screens.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.portkit.template.branding.ParticleStyle
import com.portkit.template.branding.ParticleType
import kotlin.math.PI
import kotlin.math.sin

/**
 * Camada 3: sistema de partículas ambiente (poeira, brasas, fagulhas, névoa).
 *
 * Restrições de performance (60 fps):
 *  • ZERO alocação por frame — todos os buffers são FloatArray pré-alocados;
 *  • o loop de física roda em `withFrameNanos` com dt real (clamp 50 ms);
 *  • a invalidação é scoped ao draw: um LongState "frame" é lido dentro do
 *    Canvas — recomposição nunca acontece durante a animação;
 *  • sem física pesada: velocidade constante + seno de fase para balanço.
 *
 * Com "reduzir movimento" (ou efeito desligado) o quadro fica estático: as
 * partículas continuam desenhadas (cenário bonito) mas nada se move.
 */
@Composable
fun ParticleField(
    style: ParticleStyle,
    active: Boolean,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val minRadiusPx = with(density) { style.minSizeDp.dp.toPx() }
    val maxRadiusPx = with(density) { style.maxSizeDp.dp.toPx() }

    val system = remember(style.type, style.count) { ParticleSystem(style.count) }
    val color = style.tint ?: defaultParticleColor(style.type)
    val frame = remember { mutableLongStateOf(0L) }

    LaunchedEffect(active, reducedMotion, style) {
        if (!active || reducedMotion) return@LaunchedEffect
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                if (last != 0L) {
                    val dt = ((now - last) / 1_000_000_000f).coerceIn(0f, 0.05f)
                    system.update(dt, style.type, style.speed)
                }
                last = now
            }
            frame.longValue++
        }
    }

    Canvas(modifier.fillMaxSize()) {
        frame.longValue // leitura invalida apenas o desenho
        system.ensureSeeded(size.width, size.height, minRadiusPx, maxRadiusPx)
        system.draw(this, style.type, color, reducedMotion)
    }
}

/** cores padrão por tipo — trocáveis via [ParticleStyle.tint] na config do port */
private fun defaultParticleColor(type: ParticleType): Color = when (type) {
    ParticleType.EMBERS -> Color(0xFFFFB060)
    ParticleType.DUST -> Color(0xFFE8EEF4)
    ParticleType.SPARKS -> Color(0xFFFFE9A8)
    ParticleType.MIST -> Color(0xFF7FB4D8)
}

/** Simulação minimalista — sem alocação após a semeadura. */
private class ParticleSystem(count: Int) {

    private val n: Int = count.coerceIn(10, 200)
    private val xs = FloatArray(n)
    private val ys = FloatArray(n)
    private val vxs = FloatArray(n) // fração da largura por segundo
    private val vys = FloatArray(n) // fração da altura por segundo
    private val radii = FloatArray(n)
    private val alphas = FloatArray(n)
    private val phases = FloatArray(n)

    private var w = 0f
    private var h = 0f
    private var time = 0f
    private val random = java.util.Random(SEED)

    fun ensureSeeded(width: Float, height: Float, minR: Float, maxR: Float) {
        if (width <= 0f || height <= 0f || (w == width && h == height)) return
        w = width
        h = height
        for (i in 0 until n) {
            xs[i] = random.nextFloat() * w
            ys[i] = random.nextFloat() * h
            radii[i] = minR + random.nextFloat() * (maxR - minR).coerceAtLeast(0.5f)
            alphas[i] = 0.22f + random.nextFloat() * 0.4f
            phases[i] = random.nextFloat() * TAU
            vxs[i] = (random.nextFloat() - 0.5f) * 0.02f
            vys[i] = 0.03f + random.nextFloat() * 0.05f
        }
    }

    fun update(dt: Float, type: ParticleType, speed: Float) {
        if (w == 0f || h == 0f) return
        time += dt
        for (i in 0 until n) {
            when (type) {
                ParticleType.EMBERS -> {
                    ys[i] -= vys[i] * h * speed * dt
                    xs[i] += sin(time * 1.4f + phases[i]) * 0.012f * w * dt
                    if (ys[i] < -radii[i] * 3f) {
                        ys[i] = h + radii[i] * 2f
                        xs[i] = random.nextFloat() * w
                    }
                }

                ParticleType.DUST -> {
                    xs[i] += vxs[i] * w * speed * dt
                    ys[i] += (0.2f * vys[i]) * h * speed * dt
                    wrap(i)
                }

                ParticleType.SPARKS -> {
                    ys[i] -= vys[i] * h * 1.8f * speed * dt
                    xs[i] += vxs[i] * w * 2f * speed * dt
                    if (ys[i] < -radii[i] * 3f) {
                        ys[i] = h + radii[i] * 2f
                        xs[i] = random.nextFloat() * w
                    }
                    wrapX(i)
                }

                ParticleType.MIST -> {
                    xs[i] += (0.25f * (vxs[i].takeIf { it != 0f } ?: 0.004f)) * w * speed * dt
                    ys[i] += sin(time * 0.3f + phases[i]) * 0.004f * h * dt
                    wrapX(i)
                }
            }
        }
    }

    fun draw(scope: DrawScope, type: ParticleType, color: Color, reducedMotion: Boolean) {
        if (w == 0f) return
        for (i in 0 until n) {
            val flicker = if (reducedMotion) 0.75f
            else 0.55f + 0.45f * sin(time * FLICKER_HZ + phases[i] * 3f)
            val a = (alphas[i] * flicker).coerceIn(0.03f, 1f)
            when (type) {
                ParticleType.MIST -> {
                    // halo suave por anéis concêntricos — sem alocar Brush por frame
                    val r = radii[i]
                    for (ring in 0 until 4) {
                        scope.drawCircle(
                            color = color,
                            radius = r * (1f - ring * 0.22f),
                            center = Offset(xs[i], ys[i]),
                            alpha = a * (0.5f - ring * 0.1f),
                        )
                    }
                }

                ParticleType.EMBERS -> {
                    scope.drawCircle(color, radii[i] * 2.6f, Offset(xs[i], ys[i]), alpha = a * 0.22f)
                    scope.drawCircle(color, radii[i], Offset(xs[i], ys[i]), alpha = a)
                }

                else -> scope.drawCircle(color, radii[i], Offset(xs[i], ys[i]), alpha = a)
            }
        }
    }

    private fun wrap(i: Int) {
        if (xs[i] < -MARGIN) xs[i] = w + MARGIN else if (xs[i] > w + MARGIN) xs[i] = -MARGIN
        if (ys[i] < -MARGIN) ys[i] = h + MARGIN else if (ys[i] > h + MARGIN) ys[i] = -MARGIN
    }

    private fun wrapX(i: Int) {
        if (xs[i] < -MARGIN) xs[i] = w + MARGIN else if (xs[i] > w + MARGIN) xs[i] = -MARGIN
    }

    private companion object {
        const val TAU = (2 * PI).toFloat()
        const val FLICKER_HZ = 2.2f
        const val MARGIN = 24f
        const val SEED = 42L
    }
}
