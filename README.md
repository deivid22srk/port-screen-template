# Port Screen Template

Template de interface inicial de **seleção de dados** nível AAA para apps Android que empacotam ports/recompilações nativas de jogos (Xbox 360, PS2, GameCube, PC indie, etc.). Construído em **Kotlin + Jetpack Compose**, é 100% genérico e reutilizável: **nenhum nome de jogo, franquia, logo, cor ou link está fixado no código da tela** — tudo vem de um único arquivo de configuração.

O que a tela faz (v1.1):

1. **Abre em modo imersivo fullscreen** — sem barras de sistema; elas só aparecem com swipe da borda (comportamento padrão de ports).
2. **Não procura nada automaticamente** — a tela inicia no estado "Nenhum dado selecionado" e espera você tocar em **Selecionar Dados**.
3. **Valida a pasta escolhida** via Storage Access Framework (`ACTION_OPEN_DOCUMENT_TREE`), com permissão persistida (`takePersistableUriPermission`) que sobrevive a reboots. A pasta salva de sessões anteriores é restaurada silenciosamente.
4. Quando valida, entrega `(folderUri, fileName)` ao motor do port por um único callback — o template não inicia nada por si só.
5. **Botão "Portado por Hailgames"** abre créditos com links do portador: YouTube, GitHub, Telegram e repositório do projeto base — todos configuráveis.
6. **Tela de Configurações dedicada** (engrenagem): proporção de tela, escala de resolução, renderizador, FPS, áudio, controles e efeitos — persistida e pronta para plugar ao motor.

## Padrão de qualidade AAA

| Camada | Recurso |
|---|---|
| Fundo | Arte cinematográfica em camadas, parallax sutil em 3 profundidades, scrims de legibilidade, vinheta radial e grain de filme animado |
| Atmosfera | Sistema de partículas parametrizável (poeira, brasas, faíscas com rastro, névoa volumétrica), zero alocação por frame, 60 fps |
| Título | Eyebrow espaçado + marca vetorial com glow pulsante + halo multi-camada (funciona em qualquer API, sem blur de hardware) |
| Botão primário | Glassmorphism de 4 camadas, sombra colorida do accent, ícone de play, haptics, micro-escala ao pressionar, estado loading |
| Status | Estados com ícone, cor e microanimação próprios: aguardando (lupa) / validando (arco girando) / não encontrado (alerta + dica) / pronto (check + arquivo) |
| Detalhes | Chip técnico real (motor · GLES · ABI), engrenagem com rotação mola, botão de créditos com coração, entrada orquestrada em stagger |
| Adaptativo | **Portrait e landscape sem cortes**: `safeDrawing` cobre notch lateral; largura ≥ 560 dp vira layout de duas colunas roláveis |
| Créditos | Diálogo "Portado por ..." com linhas de link tintadas por marca (YouTube/GitHub/Telegram/projeto base) |

## Estrutura do projeto

```
app/src/main/java/com/porttemplate/screen/
├── config/
│   └── PortBrandingConfig.kt   ← ÚNICO arquivo que um port edita
├── data/
│   └── GameDataScanner.kt      ← busca nomes exatos → extensões na pasta SAF
├── settings/
│   ├── PortSettings.kt         ← modelo + enums + persistência das configurações
│   └── PortSettingsViewModel.kt
├── viewmodel/
│   ├── DataSelectionState.kt   ← DataPhase (Idle/Validating/NotFound/Found/PermissionError)
│   └── DataSelectionViewModel.kt
├── ui/
│   ├── DataSelectionScreen.kt  ← cena adaptativa (portrait/landscape) + SAF
│   ├── settings/
│   │   └── SettingsScreen.kt   ← tela de Configurações dedicada
│   ├── theme/PortScreenTheme.kt
│   ├── background/
│   │   ├── ParallaxBackground.kt   ← camadas + vinheta + grain
│   │   └── AmbientParticles.kt     ← partículas parametrizáveis
│   └── components/
│       ├── AnimatedTitle.kt        ├── PrimarySelectButton.kt
│       ├── FolderButton.kt         ├── StatusArea.kt
│       ├── TechStatusChip.kt       ├── SettingsButton.kt
│       ├── CreditsSheet.kt         └── Entrance.kt (orquestração de entrada)
└── MainActivity.kt             ← fullscreen imersivo + navegação + onLaunchGame
```

## Como adaptar para um NOVO port (6 passos)

O guia completo com tabela de campos está documentado no cabeçalho de `PortBrandingConfig.kt`. Resumo:

1. **Edite `config/PortBrandingConfig.kt`** — objeto `PortBranding`: título, subtítulo, paleta (`accent`, `accentDeep`, `particleColor`), textos, tipo e quantidade de partículas, `expectedDataFiles` (os arquivos que SEU motor procura) e `links` (YouTube/GitHub/Telegram/projeto base do portador).
2. **Troque a arte de fundo** — substitua `app/src/main/res/drawable-nodpi/bg_cinematic.jpg` pela arte do port (1080x1920 JPG recomendado, mesmo nome). Sem arte? Defina `backgroundArtRes = null` e o template usa fundo procedural.
3. **Troque a marca** — substitua `res/drawable/ic_logo_mark.xml` pelo logo do port (VectorDrawable), ou desative com `showLogo = false`.
4. **Configure os créditos** — edite `portedByLabel`, `creditsTitle` e a lista `links` (rótulo, detalhe, URL, ícone e cor de cada linha).
5. **Conecte o motor** — em `MainActivity.kt`, preencha o bloco `selectionViewModel.onLaunchGame = { folderUri, fileName -> ... }`.
6. **Pronto.** Nenhum layout, animação, acessibilidade ou fluxo SAF precisa ser tocado.

### Referência rápida dos principais campos

| Campo | Efeito na tela |
|---|---|
| `portTitle` / `portSubtitle` | Título com halo + eyebrow em caixa alta |
| `accent` / `accentDeep` | Glow, orbes de luz, botão, switches, partículas |
| `expectedDataFiles` | Nomes exatos procurados na pasta (case-insensitive) |
| `acceptableExtensions` | Fallback por extensão (`.iso`, `.bin`, `.pak`...) |
| `particleType` | `DUST`, `EMBERS`, `SPARKS`, `MIST` ou `NONE` |
| `particleCount` | Quantidade base (reduz 40% automaticamente em telas baixas) |
| `titleGlowEnabled` / `showTechChip` / `showLogo` | Interruptores de efeitos individuais |
| `portedByLabel` / `links` | Botão e diálogo "Portado por ..." (créditos) |
| `label*` | Todos os textos (pronto para localizar o port) |
| `contentDesc*` | Descrições de acessibilidade de cada controle |

## Tela de Configurações (exemplo funcional)

A engrenagem navega para uma tela dedicada com o mesmo idioma visual (fundo em camadas, painéis de vidro, grain). Seções:

- **Vídeo**: proporção da tela (Auto/4:3/16:9/16:10/21:9/Esticada, com pré-visualização do frame), escala de resolução (0.5x–3.0x), filtro de textura, VSync.
- **Desempenho**: renderizador (Auto/OpenGL ES/Vulkan), limite de FPS (30–120/Ilimitado), frame skip.
- **Áudio**: latência (20–200 ms), silenciar.
- **Controles**: overlay na tela, opacidade, vibração.
- **Efeitos**: partículas ambiente, reduzir movimento (os toggles da v1.0 migraram para cá).
- **Dados do jogo**: resumo do estado atual (ligado ao ViewModel real), arquivos esperados e botão "Limpar seleção salva".

Tudo é persistido em `SharedPreferences` por `PortSettingsRepository` (chave `port_screen_prefs`) e exposto como `StateFlow<PortSettings>`.

### Conectando as configurações ao motor

Observe o `settingsViewModel.settings` e repasse cada campo ao engine:

```kotlin
lifecycleScope.launch {
    settingsViewModel.settings.collect { s ->
        engine.setAspectRatio(s.aspectRatio.ratio)       // null = nativa
        engine.setResolutionScale(s.resolutionScale)
        engine.setRenderer(s.renderer)                    // AUTO / OPENGL_ES / VULKAN
        engine.setFpsLimit(s.fpsLimit.fps)                // 0 = ilimitado
        // ... áudio, controles, etc.
    }
}
```

## Arquitetura

- **Estado separado da UI**: `DataSelectionViewModel` + `PortSettingsViewModel` (AndroidViewModel) expõem `StateFlow`; as telas são funções puras do estado. Trocar Compose por Views não afeta a lógica.
- **Máquina de estados**: `Idle` → (usuário escolhe) → `Validating` → `NotFound` | `Found` | `PermissionError`, com transição cruzada animada. **Nada é procurado no boot** — a pasta salva é apenas revalidada em silêncio.
- **Navegação**: `navigation-compose` com duas rotas (`selection`, `settings`) e transições de fade; ViewModels no escopo da Activity são compartilhados entre as rotas.
- **SAF**: seleção via `ActivityResultContracts.OpenDocumentTree`; a URI é persistida em `SharedPreferences` com `takePersistableUriPermission(FLAG_GRANT_READ_URI_PERMISSION)`. "Limpar seleção" libera a permissão (`releasePersistableUriPermission`).
- **Detecção**: passada 1 procura nomes exatos; passada 2 aceita extensões configuradas. Somente a raiz da pasta é varrida (rápido e previsível).
- **Fullscreen imersivo**: `enableEdgeToEdge` + `WindowInsetsControllerCompat` (barras ocultas, re-ocultadas no foco, reveal por swipe) + `FLAG_KEEP_SCREEN_ON`.
- **Rotação sem recriar**: `android:configChanges` no Manifest — a cena Compose se re-adapta sem reiniciar Activity nem perder estado.

## Acessibilidade

- Alvos de toque ≥ 48 dp em todos os controles interativos (incluindo chips de configuração).
- `contentDescription` configurável em todo controle (vindo do config).
- Contraste AA: texto principal `#F4F4F8` sobre base `#07070C`; textos secundários ≥ 50% de alpha sobre fundo escurecido por scrim/vinheta.
- **"Reduzir movimento"** respeita a escala de animação do sistema (auto-detecção) e expõe override manual na tela de Configurações — desativa parallax, partículas, pulso e entradas animadas.

## Performance

- Partículas atualizadas in-place (zero alocação por frame); invalidação apenas da fase de desenho.
- Parallax, pulso e grain rodam em `graphicsLayer`/Canvas — sem recomposição de árvore.
- Grain: bitmap 128px de ruído gerado uma única vez (seed fixa), tiling com `FilterQuality.None`.
- Entradas com stagger único (`rememberEntrance`); as colunas de conteúdo só rolam quando o espaço não basta (landscape baixo), sem custo no caso normal.

## Build

### Via GitHub Actions (automático)

O workflow `.github/workflows/build.yml` compila a cada push em `main` (e manualmente via `workflow_dispatch`) e publica os artefatos:

- `app-debug` → `app-debug.apk`
- `app-release-unsigned` → `app-release-unsigned.apk` (assinatura do release fica por conta do port)

### Local

```bash
./gradlew assembleDebug          # APK de debug
./gradlew assembleRelease        # APK de release não assinado
```

Requisitos: JDK 17, Android SDK 35.

### Assinar o release (para distribuir)

```bash
keytool -genkey -v -keystore port.jks -keyalg RSA -keysize 2048 -validity 10000 -alias port
apksigner sign --ks port.jks --out app-release.apk app-release-unsigned.apk
```

## Requisitos do template

- minSdk 26 / targetSdk 35 / compileSdk 35
- Kotlin 2.0.21 · AGP 8.7.3 · Gradle 8.9 · Compose BOM 2024.10.01 · Navigation 2.8.4
- Sem dependências de terceiros (nada de Accompanist, Coil ou Hilt — proposital para virar módulo de qualquer port)

## Créditos e links

- YouTube: [@hail-games1](https://youtube.com/@hail-games1?si=rREmvIBB6s98N-2m)
- GitHub: [deivid22srk](https://github.com/deivid22srk?tab=repositories)
- Telegram: [@hailgames2](https://t.me/hailgames2)

## Licença

MIT — veja [LICENSE](LICENSE).
