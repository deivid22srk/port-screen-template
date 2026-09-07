package com.portkit.template.ui.screens.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.portkit.template.R
import com.portkit.template.ui.theme.TextPrimary
import com.portkit.template.ui.theme.TextSecondary
import com.portkit.template.viewmodel.ScreenSettings

/**
 * Painel de configurações (bottom sheet de vidro escuro, DESIGN_SPEC §6.6):
 *  • Partículas ambiente (on)
 *  • Parallax do fundo (on)
 *  • Reduzir animações (off — soma-se à preferência do sistema)
 *  • Versão do port no rodapé (vem da config)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    visible: Boolean,
    settings: ScreenSettings,
    versionLabel: String,
    accent: Color,
    onDismiss: () -> Unit,
    onParticles: (Boolean) -> Unit,
    onParallax: (Boolean) -> Unit,
    onReducedMotion: (Boolean) -> Unit,
) {
    if (!visible) return

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xE60D1117),
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 10.dp),
            )

            SettingRow(
                label = stringResource(R.string.settings_particles),
                checked = settings.particlesEnabled,
                accent = accent,
                onChange = onParticles,
            )
            SettingRow(
                label = stringResource(R.string.settings_parallax),
                checked = settings.parallaxEnabled,
                accent = accent,
                onChange = onParallax,
            )

            Column {
                SettingRow(
                    label = stringResource(R.string.settings_reduced_motion),
                    checked = settings.reducedMotionManual,
                    accent = accent,
                    onChange = onReducedMotion,
                )
                Text(
                    text = stringResource(R.string.settings_reduced_motion_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
                )
            }

            Spacer(Modifier.height(10.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.08f)),
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.settings_version, versionLabel),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
        }
    }
}

@Composable
private fun SettingRow(
    label: String,
    checked: Boolean,
    accent: Color,
    onChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = accent,
                checkedThumbColor = Color(0xFF0B0F14),
                uncheckedTrackColor = Color.White.copy(alpha = 0.10f),
                uncheckedThumbColor = TextSecondary,
                uncheckedBorderColor = Color.White.copy(alpha = 0.22f),
            ),
        )
    }
}
