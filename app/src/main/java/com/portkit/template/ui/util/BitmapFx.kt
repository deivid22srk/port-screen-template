package com.portkit.template.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.util.Random

/**
 * Utilitários de bitmap "one-shot": tudo é calculado UMA vez e cacheado com
 * `remember` — nenhum custo por frame na camada visual.
 */
object BitmapFx {

    /**
     * Desfoca rapidamente a arte de fundo para o vidro do botão primário.
     *
     * Técnica: decodifica com inSampleSize alto e faz ida-e-volta de escala
     * (160 → 80 → 160 px). O resultado é um "blur" suave custando ~0 ms,
     * compatível com TODAS as APIs (não depende de RenderEffect/API 31).
     */
    fun decodeBlurredBackdrop(context: Context, resId: Int, targetWidth: Int = 160): ImageBitmap? {
        return try {
            val opts = BitmapFactory.Options().apply { inSampleSize = 8 }
            val src = BitmapFactory.decodeResource(context.resources, resId, opts)
                ?: return null
            val h = (targetWidth.toLong() * src.height / src.width).toInt().coerceAtLeast(1)

            val step1 = Bitmap.createScaledBitmap(src, targetWidth, h, true)
            if (step1 !== src) src.recycle()

            val halfW = (targetWidth / 2).coerceAtLeast(1)
            val halfH = (h / 2).coerceAtLeast(1)
            val step2 = Bitmap.createScaledBitmap(step1, halfW, halfH, true)
            val out = Bitmap.createScaledBitmap(step2, targetWidth, h, true)
            if (step2 !== out) step2.recycle()
            if (step1 !== out && step1 !== step2) step1.recycle()

            out.asImageBitmap()
        } catch (t: Throwable) {
            null
        }
    }

    /**
     * Textura de grain (ruído fotográfico) 96×96 em ARGB_8888 com alpha bem
     * baixo — desenhada tileada sobre a tela para dar textura cinematográfica.
     * Semente fixa: o padrão é estável entre execuções (evita "fervilhado").
     */
    fun createGrainBitmap(size: Int = 96): Bitmap {
        val random = Random(SEED)
        val pixels = IntArray(size * size) {
            val alpha = random.nextInt(ALPHA_MAX + 1)
            val gray = 200 + random.nextInt(56)
            (alpha shl 24) or (gray shl 16) or (gray shl 8) or gray
        }
        return Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, size, 0, 0, size, size)
        }
    }

    private const val SEED = 20260907L
    private const val ALPHA_MAX = 16
}
