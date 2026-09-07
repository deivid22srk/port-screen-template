package com.porttemplate.screen.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.porttemplate.screen.config.PortBranding

/**
 * Diálogo aberto pela engrenagem: ajustes visuais do template
 * (partículas on/off e "reduzir movimento" manual). Os toggles são
 * persistidos pelo ViewModel — exemplo vivo de como um port estende
 * a tela sem tocar no layout.
 */
@Composable
fun SettingsDialog(
    particlesEnabled: Boolean,
    reduceMotionEnabled: Boolean,
    onParticlesEnabled: (Boolean) -> Unit,
    onReduceMotionEnabled: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val config = PortBranding.config

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF14141E),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(24.dp)) {
                Text(
                    text = config.labelSettingsTitle,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(18.dp))

                SettingsToggle(
                    title = config.labelToggleParticles,
                    checked = particlesEnabled,
                    onCheckedChange = onParticlesEnabled
                )
                Spacer(Modifier.height(14.dp))
                SettingsToggle(
                    title = config.labelToggleMotion,
                    checked = reduceMotionEnabled,
                    onCheckedChange = onReduceMotionEnabled
                )

                Spacer(Modifier.height(10.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = config.labelClose,
                        color = config.accent,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsToggle(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.82f),
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = PortBranding.config.accent,
                checkedThumbColor = Color(0xFF0B0B12)
            )
        )
    }
}
