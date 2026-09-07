package com.portkit.template.ui.screens.components

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.portkit.template.R
import com.portkit.template.branding.PortBrandingConfig
import com.portkit.template.ui.theme.GlassFillSoft
import com.portkit.template.ui.theme.TextSecondary

/**
 * Chip técnico do canto inferior esquerdo (DESIGN_SPEC §6.4).
 * Mostra motor/runtime da config + GLES real do aparelho + renderizador
 * (Vulkan quando o device anuncia suporte, senão "GLES").
 */
@Composable
fun TechChip(config: PortBrandingConfig, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val glesVersion = remember {
        (context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)
            ?.deviceConfigurationInfo
            ?.glEsVersion
    }
    val supportsVulkan = remember {
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_VULKAN_HARDWARE_VERSION)
    }
    val renderer = if (supportsVulkan) config.rendererLabel else "GLES"
    val line = remember(glesVersion, supportsVulkan, config) {
        listOfNotNull(
            config.engineLabel,
            glesVersion?.let { "GLES $it" },
            renderer,
        ).joinToString(" · ")
    }
    val cd = stringResource(R.string.cd_tech_chip)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(GlassFillSoft)
            .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp)
            .semantics { contentDescription = cd },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_gpu),
            contentDescription = null,
            modifier = Modifier.size(15.dp),
            tint = config.accentSecondary,
        )
        Text(
            text = line,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
        )
    }
}
