package com.portkit.template.branding

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.portkit.template.R

/*
 * ════════════════════════════════════════════════════════════════════════════
 *  CONFIGURAÇÃO DE BRANDING DO PORT — O ÚNICO ARQUIVO QUE VOCÊ PRECISA EDITAR
 * ════════════════════════════════════════════════════════════════════════════
 *
 *  Este template é 100% genérico: nenhum composable, animação ou regra de
 *  negócio conhece um jogo específico. TODA a identidade do port está aqui.
 *
 *  COMO ADAPTAR PARA UM NOVO PORT (3 passos):
 *
 *    1) Edite os valores do objeto `PortBranding.current` logo abaixo
 *       (nome, cores, arquivo esperado, extensões, partículas etc.).
 *
 *    2) Substitua os 2 assets de nome fixo:
 *         • app/src/main/res/drawable-nodpi/port_bg.jpg  → arte de fundo do port
 *           (recomendado: ~1080×2340, JPG; a tela aplica parallax, vinheta e
 *           scrim por cima — arte escura e atmosférica funciona melhor)
 *         • app/src/main/res/drawable/port_logo.xml      → logo do port
 *           (ou crie outro drawable e mude `logoRes`; use null para exibir
 *           o nome do port em texto estilizado)
 *
 *    3) Ajuste o label do launcher em `gradle.properties` (chave `portName`).
 *
 *  Pronto. Nenhum layout, animação ou lógica precisa ser tocado.
 *
 *  OBSERVAÇÃO SOBRE COPY/TEXTOS: rótulos genéricos de interface (botões,
 *  status, configurações) ficam em res/values/strings.xml (PT-BR) e
 *  res/values-en/strings.xml (EN) porque são tradução, não identidade.
 *  Tudo que é "cara do port" (nome, tagline, versão, cores, arte, regras
 *  de arquivo) está exclusivamente neste arquivo.
 * ════════════════════════════════════════════════════════════════════════════
 */

/**
 * Tipo de partícula ambiente exibida atrás do conteúdo.
 *
 *  [ParticleType.DUST]   — poeira neutra, deriva lenta em todas as direções
 *  [ParticleType.EMBERS] — brasas quentes subindo com tremulação (clima épico)
 *  [ParticleType.SPARKS] — fagulhas cintilantes, movimento mais vivo
 *  [ParticleType.MIST]   — névoa: círculos grandes, alpha baixíssimo, lento
 */
enum class ParticleType { DUST, EMBERS, SPARKS, MIST }

/**
 * Estilo do sistema de partículas ambiente.
 *
 * @param type      comportamento visual (ver [ParticleType])
 * @param count     quantidade simulada (10–200; 60–90 é o ponto ideal p/ 60 fps)
 * @param tint      cor opcional; `null` usa a cor padrão do tipo de partícula
 * @param minSizeDp tamanho mínimo (diâmetro) em dp
 * @param maxSizeDp tamanho máximo (diâmetro) em dp
 * @param speed     multiplicador de velocidade (1f = padrão; 0.5f = câmera lenta)
 */
data class ParticleStyle(
    val type: ParticleType = ParticleType.EMBERS,
    val count: Int = 80,
    val tint: Color? = null,
    val minSizeDp: Float = 1.5f,
    val maxSizeDp: Float = 3.5f,
    val speed: Float = 1f,
)

/**
 * Identidade visual e regras de dados do port.
 *
 * IDENTIDADE
 * @param logoRes              drawable do logo (`null` → título em texto display);
 *                             monocromático é tingido com [accentPrimary] se
 *                             [tintLogoWithAccent] for true
 * @param tintLogoWithAccent   tinge o logo com a cor de acento (deixe true para
 *                             logos flat/monocromáticos; false para arte multicolor)
 * @param backgroundRes        drawable da arte de fundo (nome fixo: port_bg.jpg)
 * @param portDisplayName      nome exibido quando não há logo (caixa alta no layout)
 * @param tagline              frase curta sob o título (ex.: "Port nativo não oficial")
 * @param versionLabel         versão do port, exibida nas configurações
 * @param accentPrimary        cor de acento principal (botão, brilhos, partículas)
 * @param accentSecondary      cor de acento secundária (detalhes, gradiente de borda)
 *
 * DADOS DO JOGO (o que a tela procura)
 * @param expectedFileName     nome canônico do arquivo de dados (ex.: "default.xex")
 * @param acceptedExtensions   extensões aceitas quando não houver nome exato
 *                             (sem ponto: "xex", "iso", "bin"…)
 * @param matchFileNameExactly true → só aceita [expectedFileName];
 *                             false → aceita o nome exato OU qualquer extensão de
 *                             [acceptedExtensions] (recomendado para templates)
 * @param dataSubdirectory     subpasta opcional dentro da pasta escolhida onde os
 *                             dados devem estar (ex.: "games"); null = raiz
 *
 * APRESENTAÇÃO
 * @param particleStyle        configuração das partículas ambiente
 * @param engineLabel          rótulo do motor/runtime exibido no chip técnico
 * @param rendererLabel        rótulo do renderizador (aparece quando o device
 *                             suporta Vulkan; senão mostra "GLES")
 * @param showTechChip         exibe ou oculta o chip de status técnico
 * @param enableHaptics        feedback tátil nos toques (pode ser ignorado pelo SO)
 */
data class PortBrandingConfig(
    // ── Identidade ────────────────────────────────────────────────────────
    @DrawableRes val logoRes: Int?,
    val tintLogoWithAccent: Boolean,
    @DrawableRes val backgroundRes: Int,
    val portDisplayName: String,
    val tagline: String,
    val versionLabel: String,
    val accentPrimary: Color,
    val accentSecondary: Color,
    // ── Dados do jogo ─────────────────────────────────────────────────────
    val expectedFileName: String,
    val acceptedExtensions: List<String>,
    val matchFileNameExactly: Boolean,
    val dataSubdirectory: String?,
    // ── Apresentação ──────────────────────────────────────────────────────
    val particleStyle: ParticleStyle,
    val engineLabel: String,
    val rendererLabel: String,
    val showTechChip: Boolean,
    val enableHaptics: Boolean,
)

/**
 * A instância usada pela interface. ESTE é o bloco que um novo port edita.
 * Os valores abaixo são um exemplo neutro (cores âmbar/ciano + brasas).
 */
object PortBranding {

    val current: PortBrandingConfig = PortBrandingConfig(
        // ── Identidade ────────────────────────────────────────────────────
        logoRes = R.drawable.port_logo,          // ou null p/ usar só texto
        tintLogoWithAccent = true,
        backgroundRes = R.drawable.port_bg,      // res/drawable-nodpi/port_bg.jpg
        portDisplayName = "PORT TEMPLATE",
        tagline = "Port nativo não oficial",
        versionLabel = "1.0.0",
        accentPrimary = Color(0xFFFFB74D),       // âmbar quente
        accentSecondary = Color(0xFF4DD0E1),     // ciano frio

        // ── Dados do jogo ─────────────────────────────────────────────────
        expectedFileName = "default.xex",
        acceptedExtensions = listOf("xex", "iso", "bin", "cue", "chd"),
        matchFileNameExactly = false,
        dataSubdirectory = null,                 // ex.: "games" se os dados ficarem numa subpasta

        // ── Apresentação ──────────────────────────────────────────────────
        particleStyle = ParticleStyle(
            type = ParticleType.EMBERS,
            count = 80,
            tint = null,                         // null → cor padrão de brasas
            minSizeDp = 1.2f,
            maxSizeDp = 3.2f,
            speed = 1f,
        ),
        engineLabel = "Runtime 1.0",
        rendererLabel = "Vulkan",
        showTechChip = true,
        enableHaptics = true,
    )
}
