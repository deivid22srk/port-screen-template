package com.portkit.template.ui.screens.components

import android.graphics.BitmapShader
import android.graphics.Matrix
import android.graphics.Shader
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asFrameworkPaint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalContext
import com.portkit.template.ui.util.BitmapFx
import kotlinx.coroutines.delay

/**
 * Camadas 4–5 da composição: vinheta radial + grain fotográfico.
 *
 *  • A vinheta é um Brush radial cacheado por tamanho (drawWithCache).
 *  • O grain é uma textura 96×96 gerada uma única vez, tileada via
 *    BitmapShader com fase trocada a cada 120 ms (film grain "vivo").
 *    Com "reduzir movimento", a fase fica estática.
 *
 * Custo por frame: 1 drawRect (vinheta) + 1 drawRect (shader tileado).
 */
@Composable
fun VignetteAndGrain(reducedMotion: Boolean, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val grainBitmap = remember { BitmapFx.createGrainBitmap() }
    val grainPaint = remember {
        Paint().asFrameworkPaint().apply {
            isAntiAlias = false
            shader = BitmapShader(grainBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        }
    }
    val grainMatrix = remember { Matrix() }

    var phase by remember { mutableIntStateOf(0) }
    LaunchedEffect(reducedMotion) {
        if (!reducedMotion) {
            while (true) {
                delay(120)
                phase = (phase + 1) % GRAIN_PHASES.size
            }
        }
    }

    Box(
        modifier
            .fillMaxSize()
            .drawWithCache {
                // cache válido enquanto o tamanho não muda
                val vignette = Brush.radialGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = size.maxDimension * 0.72f,
                )
                onDrawBehind {
                    drawRect(vignette)

                    val (dx, dy) = GRAIN_PHASES[phase] // leitura de estado → invalida só o draw
                    grainMatrix.setTranslate(dx.toFloat(), dy.toFloat())
                    grainPaint.shader.setLocalMatrix(grainMatrix)
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawRect(0f, 0f, size.width, size.height, grainPaint)
                    }
                }
            },
    )
}

private val GRAIN_PHASES = arrayOf(
    0 to 0,
    24 to -16,
    -20 to 12,
    12 to 22,
)
