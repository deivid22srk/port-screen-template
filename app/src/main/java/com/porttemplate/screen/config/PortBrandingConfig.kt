/*
 * ============================================================================
 *  PORT BRANDING CONFIG
 *  O ÚNICO ARQUIVO QUE UM NOVO PORT PRECISA EDITAR.
 * ============================================================================
 *
 *  Este template é 100% genérico: nenhuma tela, cor, texto, partícula ou
 *  referência de arte está fixada no código da interface. Tudo o que dá
 *  identidade a um port — nome, paleta, arte de fundo, arquivos de dados
 *  esperados, partículas ambiente, textos — vem daqui.
 *
 *  COMO ADAPTAR PARA UM NOVO PORT (resumo; guia completo no README.md):
 *
 *    1. Edite os valores do objeto [PortBranding] lá embaixo.
 *    2. Troque  app/src/main/res/drawable-nodpi/bg_cinematic.jpg  pela arte
 *       de fundo do seu port (mesmo nome de arquivo) — ou defina
 *       `backgroundArtRes = null` para um fundo procedural sem imagem.
 *    3. Troque  res/drawable/ic_logo_mark.xml  pelo logotipo do port (SVG
 *       convertido para VectorDrawable) — ou desative com `showLogo = false`.
 *    4. Ajuste [PortBrandingConfig.expectedDataFiles] para os arquivos que
 *       o seu motor procura (ex.: "default.xex", "game.iso", "data.pak").
 *    5. Conecte a inicialização do seu motor em
 *       [com.porttemplate.screen.viewmodel.DataSelectionViewModel.onStartGame].
 *
 *  NADA MAIS precisa ser tocado. Layout, animações, acessibilidade e fluxo
 *  SAF permanecem intactos entre ports.
 * ============================================================================
 */
package com.porttemplate.screen.config

import androidx.compose.ui.graphics.Color

/**
 * Tipos de partícula ambiente parametrizáveis. Escolha o clima do seu port:
 * - [DUST]   : poeira cinematográfica cinza, flutuação lenta e discreta.
 * - [EMBERS] : brasas quentes subindo (combina com paleta âmbar/fogo).
 * - [SPARKS] : faíscas rápidas com rastro, clima energético/arcano.
 * - [MIST]   : névoa volumétrica em blobs grandes e muito lentos.
 * - [NONE]   : sem partículas (economiza bateria em aparelhos fracos).
 */
enum class ParticleType { DUST, EMBERS, SPARKS, MIST, NONE }

/**
 * Contrato completo de branding do port. Cada campo documenta o efeito exato
 * que produz na tela, para que a personalização seja feita sem ler o layout.
 */
data class PortBrandingConfig(

    // ------------------------------------------------------------------
    // IDENTIDADE / CONTEÚDO
    // ------------------------------------------------------------------

    /** Título principal da tela (exibido grande, com halo/pulso se ativado). */
    val portTitle: String,

    /** Subtítulo curto em caixa alta acima do título (estilo "eyebrow"). */
    val portSubtitle: String,

    /** Exibe a marca vetorial [R.drawable.ic_logo_mark] acima do título. */
    val showLogo: Boolean,

    // ------------------------------------------------------------------
    // PALETA
    // ------------------------------------------------------------------

    /** Cor de acento: glow do título, orbes de luz, botão, partículas. */
    val accent: Color,

    /**
     * Variação escura do acento usada em gradientes (borda inferior do vidro,
     * camadas de profundidade). Em geral = accent com hue deslocado/mais escuro.
     */
    val accentDeep: Color,

    /** Cor das partículas quando o tipo é EMBERS/SPARKS (DUST usa cinza frio). */
    val particleColor: Color,

    // ------------------------------------------------------------------
    // ARTE DE FUNDO
    // ------------------------------------------------------------------

    /**
     * Resource da arte de fundo cinematográfica. Recomendado: JPG 1080x1920
     * em drawable-nodpi. Use `null` para fundo procedural (gradientes + orbes),
     * útil quando o port ainda não tem arte pronta.
     */
    val backgroundArtRes: Int?,

    // ------------------------------------------------------------------
    // DETECÇÃO DE DADOS DO JOGO
    // ------------------------------------------------------------------

    /**
     * Nomes de arquivo exatos procurados na pasta escolhida pelo usuário
     * (case-insensitive). O primeiro encontrado define o estado "pronto".
     * Exemplos por plataforma: "default.xex" (Xbox 360), "game.iso",
     * "SLUS_123.45" (PS1), "data.pak", "main.dol" (Wii).
     */
    val expectedDataFiles: List<String>,

    /**
     * Extensões aceitáveis como fallback: se nenhum nome exato for achado,
     * qualquer arquivo terminando com uma destas extensões valida a pasta.
     * Deixe vazia para aceitar SOMENTE os nomes exatos acima.
     */
    val acceptableExtensions: List<String>,

    // ------------------------------------------------------------------
    // ATMOSFERA / MOTION
    // ------------------------------------------------------------------

    /** Tipo do sistema de partículas ambiente. */
    val particleType: ParticleType,

    /** Quantidade base de partículas (reduzida automaticamente em telas baixas). */
    val particleCount: Int,

    /** Liga/desliga o sistema de partículas por completo. */
    val particlesEnabled: Boolean,

    /** Halo/pulso cinematográfico no título (multi-camadas, custo baixo). */
    val titleGlowEnabled: Boolean,

    /** Chip técnico (motor/GPU/ABI) no canto inferior esquerdo. */
    val showTechChip: Boolean,

    // ------------------------------------------------------------------
    // TEXTOS DA INTERFACE (PT-BR no demo; troque para localizar o port)
    // ------------------------------------------------------------------

    /** Botão primário no estado "dados não encontrados". */
    val labelSelectData: String,

    /** Botão primário no estado "dados encontrados" (dispara o jogo). */
    val labelStartGame: String,

    /** Botão secundário para escolher outra pasta. */
    val labelSelectFolder: String,

    /** Estado de status: detecção automática em andamento. */
    val labelSearching: String,

    /** Estado de status: pasta/dados não encontrados. */
    val labelNotFound: String,

    /** Dica do estado "não encontrado"; %s é substituído pelo arquivo esperado. */
    val labelNotFoundHint: String,

    /** Estado de status: dados validados e prontos. */
    val labelFound: String,

    /** Linha complementar do estado pronto; %s = nome do arquivo detectado. */
    val labelFoundFile: String,

    /** Texto do botão primário enquanto a pasta escolhida é validada. */
    val labelValidating: String,

    /** Estado de status: permissão de leitura negada/revogada. */
    val labelPermissionError: String,

    // ------------------------------------------------------------------
    // DIÁLOGO DE AJUSTES (botão de engrenagem)
    // ------------------------------------------------------------------

    /** Título do diálogo aberto pela engrenagem. */
    val labelSettingsTitle: String,

    /** Rótulo do toggle de partículas. */
    val labelToggleParticles: String,

    /** Rótulo do toggle "reduzir movimento" (força o modo acessível). */
    val labelToggleMotion: String,

    /** Botão de fechar do diálogo. */
    val labelClose: String,

    // ------------------------------------------------------------------
    // ACESSIBILIDADE (contentDescription)
    // ------------------------------------------------------------------

    /** Descrição do ícone de play / ação do botão primário. */
    val contentDescPlay: String,

    /** Descrição do botão de engrenagem. */
    val contentDescSettings: String,

    /** Descrição do botão secundário de pasta. */
    val contentDescFolder: String,

    /** Descrição da arte de fundo (lida por leitores de tela). */
    val contentDescBackground: String,
)

/**
 * ===========================================================================
 *  VALORES DO PORT — EDITE APENAS ESTE BLOCO
 * ===========================================================================
 *  Os valores abaixo formam o "demo neutro" do template: paleta âmbar sobre
 *  azul-noite, brasas subindo e detecção de "default.xex" como exemplo.
 *  Substitua por dados do seu port (veja o passo a passo no topo do arquivo).
 * ===========================================================================
 */
object PortBranding {

    val config: PortBrandingConfig = PortBrandingConfig(
        // ---- Identidade ---------------------------------------------------
        portTitle = "PORT TITLE",
        portSubtitle = "SELECT GAME DATA",
        showLogo = true,

        // ---- Paleta -------------------------------------------------------
        accent = Color(0xFFF0A44E),        // âmbar quente
        accentDeep = Color(0xFFB4581E),    // âmbar queimado
        particleColor = Color(0xFFFFB166), // brasas

        // ---- Arte de fundo ------------------------------------------------
        // Troque o arquivo bg_cinematic.jpg pela arte do port mantendo o nome.
        backgroundArtRes = com.porttemplate.screen.R.drawable.bg_cinematic,

        // ---- Detecção de dados --------------------------------------------
        expectedDataFiles = listOf(
            "default.xex",   // exemplo Xbox 360 — troque pelos arquivos do SEU motor
            "game.iso",      // exemplo genérico de imagem de disco
            "data.pak"       // exemplo genérico de pacote de assets
        ),
        acceptableExtensions = listOf(
            ".iso", ".bin", ".xex", ".pak"
        ),

        // ---- Atmosfera ----------------------------------------------------
        particleType = ParticleType.EMBERS,
        particleCount = 54,
        particlesEnabled = true,
        titleGlowEnabled = true,
        showTechChip = true,

        // ---- Textos -------------------------------------------------------
        labelSelectData = "Selecionar Dados",
        labelStartGame = "Iniciar Jogo",
        labelSelectFolder = "Selecionar Pasta",
        labelSearching = "Procurando dados do jogo…",
        labelNotFound = "Dados não encontrados",
        labelNotFoundHint = "Selecione a pasta que contém %s",
        labelFound = "Dados prontos",
        labelFoundFile = "Pronto para iniciar: %s",
        labelValidating = "Validando…",
        labelPermissionError = "Permissão negada",

        // ---- Diálogo de ajustes -------------------------------------------
        labelSettingsTitle = "Ajustes Visuais",
        labelToggleParticles = "Partículas ambiente",
        labelToggleMotion = "Reduzir movimento",
        labelClose = "Fechar",

        // ---- Acessibilidade ------------------------------------------------
        contentDescPlay = "Iniciar",
        contentDescSettings = "Abrir ajustes visuais",
        contentDescFolder = "Selecionar outra pasta de dados",
        contentDescBackground = "Arte de fundo do port",
    )
}
