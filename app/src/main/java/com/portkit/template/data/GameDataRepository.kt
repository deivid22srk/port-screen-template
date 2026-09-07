package com.portkit.template.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.portkit.template.branding.PortBrandingConfig

/** Informações do arquivo de dados do jogo validado. */
data class GameDataInfo(
    val fileName: String,
    val sizeBytes: Long,
    val uri: Uri,
)

/** Resultado da verificação/validação dos dados do jogo. */
sealed interface DataResolution {
    /** Arquivo válido encontrado (nome/extensão conferem com a config). */
    data class Found(val info: GameDataInfo) : DataResolution

    /** Acesso válido, mas nada compatível encontrado. */
    data object NotFound : DataResolution

    /** Permissão persistida expirou/foi revogada (exige nova seleção do usuário). */
    data class PermissionLost(val cause: Throwable? = null) : DataResolution
}

/**
 * Toda a lógica de acesso a dados via Storage Access Framework.
 *
 * Fluxo:
 *  1) `resolveSaved` — confere as URIs persistidas de sessões anteriores;
 *  2) `inspectFile`  — valida um arquivo escolhido com ACTION_OPEN_DOCUMENT;
 *  3) `scanTree`     — varre a pasta escolhida com ACTION_OPEN_DOCUMENT_TREE.
 *
 * A correspondência segue a config do port: nome exato (`default.xex`) ou
 * qualquer extensão aceita (`xex`, `iso`, `bin`…). Toda E/S roda fora da
 * main thread (o ViewModel chama em Dispatchers.IO).
 */
class GameDataRepository(
    private val context: Context,
    private val config: PortBrandingConfig,
) {

    /**
     * Confere o que foi persistido em sessões anteriores.
     * Prioridade: arquivo escolhido > pasta escolhida.
     */
    fun resolveSaved(prefs: PortPreferences): DataResolution {
        prefs.fileUri?.let { saved ->
            when (val r = inspectFile(Uri.parse(saved))) {
                is DataResolution.Found -> return r
                is DataResolution.PermissionLost -> return r
                DataResolution.NotFound -> Unit // segue para a pasta
            }
        }
        prefs.folderUri?.let { saved ->
            return scanTree(Uri.parse(saved))
        }
        return DataResolution.NotFound
    }

    /** Valida um arquivo escolhido pelo usuário (ACTION_OPEN_DOCUMENT). */
    fun inspectFile(uri: Uri): DataResolution {
        return try {
            val doc = DocumentFile.fromSingleUri(context, uri)
                ?: return DataResolution.NotFound
            if (!doc.exists() || !doc.canRead()) {
                return DataResolution.PermissionLost()
            }
            val name = doc.name
            if (!matches(name)) return DataResolution.NotFound
            DataResolution.Found(GameDataInfo(name ?: "?", doc.length(), uri))
        } catch (e: SecurityException) {
            DataResolution.PermissionLost(e)
        } catch (t: Throwable) {
            DataResolution.NotFound
        }
    }

    /**
     * Varre a pasta escolhida (ACTION_OPEN_DOCUMENT_TREE) procurando o arquivo
     * de dados. Se [PortBrandingConfig.dataSubdirectory] estiver definida,
     * a busca acontece dentro dela.
     */
    fun scanTree(treeUri: Uri): DataResolution {
        return try {
            val root = DocumentFile.fromTreeUri(context, treeUri)
                ?: return DataResolution.PermissionLost()
            if (!root.exists() || !root.canRead()) {
                return DataResolution.PermissionLost()
            }

            val dir = config.dataSubdirectory
                ?.let { sub -> root.findFile(sub)?.takeIf { it.isDirectory } ?: root }
                ?: root

            // 1) nome exato primeiro (prioridade canônica)
            val exact = dir.findFile(config.expectedFileName)
            if (exact != null && exact.isFile && exact.canRead()) {
                return DataResolution.Found(GameDataInfo(exact.name ?: config.expectedFileName, exact.length(), exact.uri))
            }

            // 2) qualquer arquivo com extensão aceita (quando permitido)
            if (!config.matchFileNameExactly) {
                dir.listFiles().firstOrNull { it.isFile && matchesExtension(it.name) }
                    ?.let { hit ->
                        return DataResolution.Found(GameDataInfo(hit.name ?: "?", hit.length(), hit.uri))
                    }
            }
            DataResolution.NotFound
        } catch (e: SecurityException) {
            DataResolution.PermissionLost(e)
        } catch (t: Throwable) {
            DataResolution.NotFound
        }
    }

    /** true se [name] casa com a regra de dados da config (nome exato ou extensão). */
    fun matches(name: String?): Boolean {
        if (name == null) return false
        val n = name.lowercase()
        if (n == config.expectedFileName.lowercase()) return true
        if (config.matchFileNameExactly) return false
        return matchesExtension(n)
    }

    private fun matchesExtension(name: String?): Boolean {
        if (name == null) return false
        val n = name.lowercase()
        return config.acceptedExtensions.any { ext -> n.endsWith(".$ext") }
    }
}
