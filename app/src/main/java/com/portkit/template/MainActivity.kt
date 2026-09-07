package com.portkit.template

/*
 * ════════════════════════════════════════════════════════════════════════════
 *  MainActivity — hospedeira da tela de seleção de dados.
 *
 *  PONTO DE INTEGRAÇÃO DO PORT:
 *  o callback `onLaunchGame` abaixo é onde o motor/emulador do seu port deve
 *  ser iniciado (ex.: intent para a Activity nativa do emulador). Nada mais
 *  nesta tela precisa mudar para reaproveitar o template.
 * ════════════════════════════════════════════════════════════════════════════
 */

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.portkit.template.ui.screens.DataSelectionScreen
import com.portkit.template.ui.theme.PortTemplateTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PortTemplateTheme {
                DataSelectionScreen(
                    onLaunchGame = {
                        // TODO(port): substitua pelo boot real do motor do port.
                        Toast.makeText(this, getString(R.string.launch_hook_toast), Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
    }
}
