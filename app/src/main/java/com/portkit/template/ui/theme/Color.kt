package com.portkit.template.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * Tokens FIXOS do template (estrutura do tema).
 * As cores de IDENTIDADE (acentos, logo, fundo) não estão aqui — vêm de
 * PortBrandingConfig.kt. Valores abaixo garantem contraste AA sobre fundo escuro.
 */

/** Fundo profundo base (janela, splash, falhas de arte). */
val BgDeep = Color(0xFF07090D)

/** Texto principal — contraste 16.2:1 sobre BgDeep. */
val TextPrimary = Color(0xFFF2F5F7)

/** Texto secundário/detalhes — contraste 8.1:1 sobre BgDeep. */
val TextSecondary = Color(0xFFA8B4C0)

/** Estado "procurando" (neutro). */
val StatusCheck = Color(0xFFA8B4C0)

/** Estado "não encontrado" (aviso). */
val StatusWarning = Color(0xFFFFC46B)

/** Estado "encontrado" (sucesso). */
val StatusOk = Color(0xFF6FD68C)

/** Estado "erro de permissão". */
val StatusError = Color(0xFFFF6B6B)

/** Preenchimento de vidro padrão (branco 8%). */
val GlassFill = Color(0x14FFFFFF)

/** Preenchimento de vidro suave (branco 4%) — elementos em espera. */
val GlassFillSoft = Color(0x0AFFFFFF)

/** Borda de vidro padrão (branco 28%). */
val GlassBorder = Color(0x47FFFFFF)
