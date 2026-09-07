package com.portkit.template.util

import java.util.Locale

/** Formata tamanho de arquivo de forma curta e legível (ex.: "712.4 MB"). */
fun humanizeBytes(bytes: Long): String {
    if (bytes <= 0L) return "0 B"
    val b = bytes.toDouble()
    return when {
        b >= (1L shl 30) -> String.format(Locale.getDefault(), "%.1f GB", b / GB)
        b >= (1L shl 20) -> String.format(Locale.getDefault(), "%.1f MB", b / MB)
        b >= (1L shl 10) -> String.format(Locale.getDefault(), "%.1f KB", b / KB)
        else -> "$bytes B"
    }
}

private const val KB = 1024.0
private const val MB = KB * 1024.0
private const val GB = MB * 1024.0
