package com.porttemplate.screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.porttemplate.screen.ui.DataSelectionRoute
import com.porttemplate.screen.ui.theme.PortScreenTheme
import com.porttemplate.screen.viewmodel.DataSelectionViewModel

/**
 * Activity única do template. Todo o conteúdo é Jetpack Compose edge-to-edge.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PortScreenTheme {
                val viewModel: DataSelectionViewModel = viewModel()

                // ==========================================================
                // PONTO DE INTEGRAÇÃO DO MOTOR DO PORT
                //
                // Quando os dados estão prontos e o usuário toca em
                // "Iniciar Jogo", este callback recebe:
                //   folderUri — árvore SAF persistida escolhida pelo usuário
                //   fileName  — arquivo de dados detectado (do config)
                //
                // Aqui o seu port entregaria ao engine/emulador, por exemplo
                // resolvendo com contentResolver.openInputStream/DocumentFile.
                // ==========================================================
                viewModel.onLaunchGame = { folderUri, fileName ->
                    android.widget.Toast.makeText(
                        this,
                        "Motor do port receberia: $fileName ($folderUri)",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }

                DataSelectionRoute(viewModel)
            }
        }
    }
}
