# Especificação de Design — Tela de Seleção de Dados (Template AAA para Ports Android)

> **Status:** aprovada como base de implementação.
> **Escopo:** composição, hierarquia, paleta, tipografia, espaçamento, motion e estados.
> **Regra de ouro:** nenhum valor específico de jogo vive nesta spec ou no layout — tudo que é
> identidade de port vem de `PortBrandingConfig.kt`. Os valores abaixo são **exemplo neutro**.

---

## 1. Princípios

1. **Cinematográfico, não "app de sistema"** — fundo em camadas com parallax, vinheta, grain e
   partículas ambientes criam profundidade; nada de superfícies planas padrão Material.
2. **Uma ação por vez** — a tela tem UM objetivo (garantir que os dados do jogo existam). O botão
   primário domina; o resto é apoio discreto.
3. **Silêncio sonoro visual** — partículas e brilhos são sutis (alpha ≤ 0.5, movimento lento).
   Efeitos chamam atenção pela qualidade, não pelo volume.
4. **60 fps ou nada** — toda animação usa `graphicsLayer`/`Canvas` sem recomposição por frame,
   sem alocação em loop e com degradação graciosa via "reduzir movimento".
5. **Genérico por construção** — trocar `PortBrandingConfig.kt` + 2 imagens deve produzir uma
   tela que parece de outro jogo sem tocar em nenhum composable.

## 2. Composição (retrato como referência)

```
┌──────────────────────────────────────────────┐
│              [área do logo 22% altura]        │  ← logo/imagem ou texto display
│                  TAGLINE (12sp)               │
│                                               │
│                                               │
│           ┌───────────────────────┐           │
│           │   ▶  SELECIONAR DADOS │           │  ← botão primário glass (60% altura)
│           └───────────────────────┘           │
│         ● status / aviso (3 estados)          │  ← banner de status
│                                               │
│ [GPU chip]                     [pasta] [⚙]    │  ← rodapé alinhado às bordas
└──────────────────────────────────────────────┘
```

- Conteúdo central limitado a **maxWidth 480dp** (tablets/dobráveis mantêm coluna elegante).
- Em **paisagem**, o bloco central encolhe espaçamentos verticais proporcionalmente
  (`maxHeight < 400dp` → espaçamentos ×0.55); nada de scroll — a tela cabe sempre.
- Margens de borda: **24dp**; grid de espaçamento **8dp**.

## 3. Camadas de fundo (z-order)

| # | Camada | Conteúdo | Movimento |
|---|--------|----------|-----------|
| 1 | Arte de fundo | imagem da config (`backgroundRes`), escala 1.10 p/ margem de parallax | fator 1.0 (tilt + drag) |
| 2 | Scrim de contraste | gradiente vertical preto 0→45% na metade inferior | estático |
| 3 | Partículas | `ParticleField` conforme config (DUST/EMBERS/SPARKS/MIST) | animação própria |
| 4 | Vinheta | radial: transparente no centro → preto 55% nas bordas | estático |
| 5 | Grain | ruído 96×96 tileado, alpha 0.05 | troca de fase a cada 120 ms |
| 6 | Conteúdo | título, botão, status, rodapé | fator −0.18 (inverso, dá profundidade) |

Parallax: tilt do acelerômetro (amplitude 10 dp, suavização EMA 0.12) + arraste de toque
(amplitude 22 dp com retorno por spring ao soltar). Desativado por "reduzir movimento".

## 4. Paleta (valores de EXEMPLO — reais vêm da config)

| Token | Valor exemplo | Uso |
|-------|--------------|-----|
| `bgDeep` | `#07090D` | fundo base / splash |
| `textPrimary` | `#F2F5F7` | título, textos principais (contraste 16.2:1 sobre bg) |
| `textSecondary` | `#A8B4C0` | tagline, detalhes (contraste 8.1:1) |
| `glassFill` | branco 8% | preenchimento de vidro |
| `glassBorder` | branco 28% → acento 45% | borda gradiente dos vidros |
| `accentPrimary` | `#FFB74D` (âmbar) | botão primário, partículas, destaque — **da config** |
| `accentSecondary` | `#4DD0E1` (ciano) | detalhes, gradiente de borda — **da config** |
| `statusCheck` | `#A8B4C0` | estado "procurando" |
| `statusWarning` | `#FFC46B` | estado "não encontrado" |
| `statusOk` | `#6FD68C` | estado "encontrado" |
| `statusError` | `#FF6B6B` | estado "erro de permissão" |

## 5. Tipografia

- **Display** (título/logotipo textual, botão primário): **Barlow Condensed SemiBold** —
  46sp (título), 17sp (botão, tracking +0.08em, caixa alta).
- **Corpo/labels**: **Barlow Regular/Medium** — 15sp corpo, 12sp labels/chips (tracking +0.04em).
- Escala restrita: 12 / 15 / 17 / 22 / 46. Ambas as famílias são OFL e podem ser trocadas por
  fontes do port sem alterar código (apenas arquivos em `res/font/`).

## 6. Componentes e estados

### 6.1 Logo/Título (área dedicada)
- Se `logoRes != null`: imagem com **glow por pulso** (halo radial do acento, alpha 0.12↔0.28,
  ciclo 4 s) atrás do logo; entrada: fade + slide-up 24 dp, 600 ms, spring suave, delay 80 ms.
- Se `logoRes == null`: `portDisplayName` em display font, tracking +0.12em, mesmo tratamento.
- Tagline sempre abaixo, 12sp, `textSecondary`, tracking +0.30em, caixa alta.

### 6.2 Botão primário "Selecionar Dados"
- Formato: pílula **280–320 × 60 dp** (largura mín 280, máx 320), raio 30 dp.
- **Glassmorphism real**: fundo = recorte desfocado da própria arte de fundo alinhado ao
  backdrop (bitmap pré-desfocado por escala 1/8, uma vez só, sem custo por frame) + gradiente
  de preenchimento acento 18% → 6% + borda 1 dp gradiente branco 28% → acento 45%.
- Conteúdo: ícone play 24 dp + label 17sp. Gap 12 dp.
- Estados: `enabled` (descrito acima) · `pressed` (escala 0.97, sheen diagonal branco 10%
  varrendo 400 ms, haptic `LongPress`) · `loading` (spinner 20 dp no lugar do ícone, clique
  desativado, label "Verificando…") · `launch` (quando dados prontos: label "Iniciar", borda
  acento 60%, halo respirando 2.4 s).
- Entrada: fade + scale 0.96→1, 500 ms, delay 200 ms.

### 6.3 Banner de status (abaixo do botão primário)
Três estados + erro, cada um com ícone em medalhão de vidro 28 dp, título 15sp e detalhe 12sp:

| Estado | Ícone | Cor | Microanimação |
|--------|-------|-----|---------------|
| Procurando | arco giratório 270° | `statusCheck` | arco varre continuamente; pontos "…" pulsando |
| Não encontrado | triângulo-alerta | `statusWarning` | shake horizontal ±3 dp, 3 ciclos na entrada |
| Encontrado | check | `statusOk` | check "desenha" por path 350 ms + bounce escala 1.15→1 |
| Erro | escudo/cadeado | `statusError` | pulso de opacidade 1↔0.6, 2 ciclos |

Transição entre estados: `AnimatedContent` fade + slide vertical 240 ms.

### 6.4 Chip técnico (canto inferior esquerdo)
- Vidro suave (fill branco 6%, borda branca 18%), raio 16 dp, altura 36 dp, padding 12 dp.
- Ícone GPU 16 dp + texto 12sp: `{engineLabel} · GLES {versão do device} · {rendererLabel}`.
- Ícone com leve tinta do acento secundário.

### 6.5 Botão "Selecionar Pasta" (canto inferior direito)
- Ícone-pílula de vidro 48×48 dp (alvo de toque garantido ≥ 48 dp).
- **Desabilitado** (durante "procurando"): fill branco 4%, ícone `textSecondary` 38%,
  SEM borda — parece "em espera", não quebrado.
- **Habilitado**: vidro padrão. **Ênfase** quando dados não encontrados: borda acento 55%
  + ícone acento + halo respirando discreto.

### 6.6 Botão configurações (canto inferior direito, ao lado da pasta)
- Ícone engrenagem 48×48 dp de vidro; ao toque, rotação −90° com spring e abre sheet.
- Sheet (bottom sheet de vidro escuro): Partículas (on), Parallax (on), Reduzir animações
  (off; respeita sistema), versão do port no rodapé.

## 7. Motion (diretrizes)

| Evento | Duração | Curva |
|--------|---------|-------|
| Entrada de elementos (stagger) | 500–600 ms | spring (damping 0.8) + delays 80/200/320/440 ms |
| Transições de estado | 240 ms | FastOutSlowIn |
| Press/released | 120 ms | FastOutSlowIn |
| Pulso de glow | 4 s loop | FastOutSlowIn (sin) |
| Retorno do drag de parallax | spring médio | dampingRatio 0.75 |
| Partículas | contínuo | dt real com clamp 50 ms |

**Reduced motion** (sistema `ANIMATOR_DURATION_SCALE == 0` ou manual): parallax off,
partículas viram quadro estático, pulsos/loops off, entradas viram fade 120 ms (ou instantâneo).

## 8. Acessibilidade

- Contraste: texto primário 16.2:1, secundário 8.1:1, detalhes de status ≥ 4.6:1 (AA ✓).
- Alvos de toque: todos os interativos ≥ 48×48 dp (inclui ícones de canto).
- `contentDescription` em logo, botão primário, pasta, configurações, chip técnico e banner.
- Estados de UI anunciados por texto (não só cor); ícone sempre acompanha a cor.
- Fontes respeitam escala do sistema (sp em tudo); layout tolera 1.3× sem quebrar (coluna
  central com espaçamentos fluidos).

## 9. Critérios de aceite (QA)

1. Nenhum valor de port fora de `PortBrandingConfig.kt` + assets de nome fixo (`port_bg`,
   `port_logo`) + `portName` no `gradle.properties` (label do launcher).
2. Os três estados de status (procurando / não encontrado / encontrado) + erro de permissão
   renderizam com ícone, cor e microanimação próprios.
3. Contraste AA verificado por cálculo; alvos ≥ 48 dp verificados no código.
4. Composições de preview para: 360×640, 411×891, 768×1024, paisagem 891×411.
5. Reduced motion desliga parallax + partículas animadas (quadro estático).
