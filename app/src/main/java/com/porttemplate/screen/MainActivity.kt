package com.porttemplate.screen

import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.porttemplate.screen.ui.DataSelectionRoute
import com.porttemplate.screen.ui.settings.SettingsRoute
import com.porttemplate.screen.ui.theme.PortScreenTheme
import com.porttemplate.screen.viewmodel.DataSelectionViewModel
import com.porttemplate.screen.settings.PortSettingsViewModel

/**
 * Activity única do template. Todo o conteúdo é Jetpack Compose edge-to-edge
 * em MODO IMERSIVO (fullscreen de verdade): barras de sistema ocultas e
 * re-ocultadas sempre que o foco volta (diálogos, swipe temporário etc.).
 * O usuário as revela deslizando da borda — comportamento padrão de ports.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Mantém a tela ligada durante o jogo (comportamento esperado de um port).
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            PortScreenTheme {
                // ViewModels no escopo da Activity: compartilhados entre as
                // telas de Seleção e Configurações via navegação.
                val selectionViewModel: DataSelectionViewModel = viewModel()
                val settingsViewModel: PortSettingsViewModel = viewModel()

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
                selectionViewModel.onLaunchGame = { folderUri, fileName ->
                    Toast.makeText(
                        this,
                        "Motor do port receberia: $fileName ($folderUri)",
                        Toast.LENGTH_LONG
                    ).show()
                }

                AppNavigation(selectionViewModel, settingsViewModel)
            }
        }

        hideSystemBars()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    private fun hideSystemBars() {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
            hide(WindowInsetsCompat.Type.systemBars())
        }
    }
}

private object Routes {
    const val SELECTION = "selection"
    const val SETTINGS = "settings"
}

@Composable
private fun AppNavigation(
    selectionViewModel: DataSelectionViewModel,
    settingsViewModel: PortSettingsViewModel,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SELECTION,
        enterTransition = { fadeIn(tween(320)) },
        exitTransition = { fadeOut(tween(220)) },
        popEnterTransition = { fadeIn(tween(320)) },
        popExitTransition = { fadeOut(tween(220)) }
    ) {
        composable(Routes.SELECTION) {
            DataSelectionRoute(
                viewModel = selectionViewModel,
                settingsViewModel = settingsViewModel,
                onOpenSettings = {
                    navController.navigate(Routes.SETTINGS) { launchSingleTop = true }
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsRoute(
                settingsViewModel = settingsViewModel,
                selectionViewModel = selectionViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
