package com.portkit.template.data

import android.content.Context

/**
 * Persistência leve da tela (SharedPreferences).
 *
 * Guarda:
 *  • o arquivo de dados escolhido via SAF (uri persistida com permissão)
 *  • a pasta de dados escolhida via ACTION_OPEN_DOCUMENT_TREE
 *  • preferências de apresentação (partículas, parallax, reduzir animações)
 *
 * Classe deliberadamente sem dependências além do Context para servir de
 * referência simples em qualquer port.
 */
class PortPreferences(context: Context) {

    private val prefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    /** URI do arquivo de dados escolhido (ACTION_OPEN_DOCUMENT), ou null. */
    var fileUri: String?
        get() = prefs.getString(KEY_FILE_URI, null)
        set(value) = prefs.edit().putString(KEY_FILE_URI, value).apply()

    /** URI da pasta de dados escolhida (ACTION_OPEN_DOCUMENT_TREE), ou null. */
    var folderUri: String?
        get() = prefs.getString(KEY_FOLDER_URI, null)
        set(value) = prefs.edit().putString(KEY_FOLDER_URI, value).apply()

    var particlesEnabled: Boolean
        get() = prefs.getBoolean(KEY_PARTICLES, true)
        set(value) = prefs.edit().putBoolean(KEY_PARTICLES, value).apply()

    var parallaxEnabled: Boolean
        get() = prefs.getBoolean(KEY_PARALLAX, true)
        set(value) = prefs.edit().putBoolean(KEY_PARALLAX, value).apply()

    /** Override manual de "reduzir animações" (soma-se ao estado do sistema). */
    var reducedMotionManual: Boolean
        get() = prefs.getBoolean(KEY_REDUCED_MOTION, false)
        set(value) = prefs.edit().putBoolean(KEY_REDUCED_MOTION, value).apply()

    fun clearSelection() {
        prefs.edit()
            .remove(KEY_FILE_URI)
            .remove(KEY_FOLDER_URI)
            .apply()
    }

    private companion object {
        const val FILE_NAME = "port_screen_prefs"
        const val KEY_FILE_URI = "data_file_uri"
        const val KEY_FOLDER_URI = "data_folder_uri"
        const val KEY_PARTICLES = "fx_particles_enabled"
        const val KEY_PARALLAX = "fx_parallax_enabled"
        const val KEY_REDUCED_MOTION = "fx_reduced_motion_manual"
    }
}
