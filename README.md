# Port Screen Template

Template de interface inicial de **seleção de dados** nível AAA para apps Android que empacotam ports/recompilações nativas de jogos (Xbox 360, PS2, GameCube, PC indie, etc.). Construído em **Kotlin + Jetpack Compose**, é 100% genérico e reutilizável: **nenhum nome de jogo, franquia, logo ou cor está fixado no código da tela** — tudo vem de um único arquivo de configuração.

O que a tela faz:

1. **Detecta automaticamente** a presença do arquivo de dados do jogo (`default.xex`, `game.iso`, `data.pak`, o que você configurar) na pasta persistida de uma sessão anterior.
2. Se não encontrar, **deixa o usuário selecionar a pasta** via Storage Access Framework (`ACTION_OPEN_DOCUMENT_TREE`), com permissão persistida (`takePersistableUriPermission`) que sobrevive a reboots.
3. Quando valida a pasta, entrega `(folderUri, fileName)` ao motor do port por um único callback — o template não inicia nada por si só.

## Padrão de qualidade AAA

| Camada | Recurso |
|---|---|
| Fundo | Arte cinematográfica em camadas, parallax sutil em 3 profundidades, scrims de legibilidade, vinheta radial e grain de filme animado |
| Atmosfera | Sistema de partículas parametrizável (poeira, brasas, faíscas com rastro, névoa volumétrica), zero alocação por frame, 60 fps |
| Título | Eyebrow espaçado + marca vetorial com glow pulsante + halo multi-camada (funciona em qualquer API, sem blur de hardware) |
| Botão primário | Glassmorphism de 4 camadas, sombra colorida do accent, ícone de play, haptics, micro-escala ao pressionar, estado loading |
| Status | 3 estados com ícone, cor e microanimação próprios: procurando (arco girando) / não encontrado (alerta + dica do arquivo esperado) / pronto (check + arquivo detectado) |
| Detalhes | Chip técnico real (motor · GLES · ABI) no canto inferior esquerdo, engrenagem com rotação mola no canto direito, diálogo de ajustes visuais |

## Estrutura do projeto

```
app/src/main/java/com/porttemplate/screen/
├── config/
│   └── PortBrandingConfig.kt   ← ÚNICO arquivo que um port edita
├── data/
│   └── GameDataScanner.kt      ← busca nomes exatos → extensões na pasta SAF
├── viewmodel/
│   ├── DataSelectionState.kt   ← DataPhase (Searching/NotFound/Found/PermissionError)
│   └── DataSelectionViewModel.kt
├── ui/
│   ├── DataSelectionScreen.kt  ← composição da cena + integração SAF
│   ├── theme/PortScreenTheme.kt
│   ├── background/
│   │   ├── ParallaxBackground.kt   ← camadas + vinheta + grain
│   │   └── AmbientParticles.kt     ← partículas parametrizáveis
│   └── components/
│       ├── AnimatedTitle.kt        ├── PrimarySelectButton.kt
│       ├── FolderButton.kt         ├── StatusArea.kt
│       ├── TechStatusChip.kt       ├── SettingsButton.kt
│       ├── SettingsDialog.kt       └── Entrance.kt (orquestração de entrada)
└── MainActivity.kt             ← registra onLaunchGame (integração do motor)
```

## Como adaptar para um NOVO port (5 passos)

O guia completo com tabela de campos está documentado no cabeçalho de `PortBrandingConfig.kt`. Resumo:

1. **Edite `config/PortBrandingConfig.kt`** — objeto `PortBranding`: título, subtítulo, paleta (`accent`, `accentDeep`, `particleColor`), textos, tipo e quantidade de partículas, e `expectedDataFiles` (os arquivos que SEU motor procura).
2. **Troque a arte de fundo** — substitua `app/src/main/res/drawable-nodpi/bg_cinematic.jpg` pela arte do port (1080x1920 JPG recomendado, mesmo nome). Sem arte? Defina `backgroundArtRes = null` e o template usa fundo procedural.
3. **Troque a marca** — substitua `res/drawable/ic_logo_mark.xml` pelo logo do port (VectorDrawable), ou desative com `showLogo = false`.
4. **Conecte o motor** — em `MainActivity.kt`, preencha o bloco `viewModel.onLaunchGame = { folderUri, fileName -> ... }`.
5. **Pronto.** Nenhum layout, animação, acessibilidade ou fluxo SAF precisa ser tocado.

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
| `label*` | Todos os textos (pronto para localizar o port) |
| `contentDesc*` | Descrições de acessibilidade de cada controle |

## Arquitetura

- **Estado separado da UI**: `DataSelectionViewModel` (AndroidViewModel) expõe `StateFlow<DataSelectionUiState>`; a tela é uma função pura do estado. Trocar Compose por Views não afeta a lógica.
- **Máquina de estados**: `Searching` → `NotFound` | `Found` | `PermissionError`, com transição cruzada animada entre estados.
- **SAF**: seleção via `ActivityResultContracts.OpenDocumentTree`; a URI é persistida em `SharedPreferences` com `takePersistableUriPermission(FLAG_GRANT_READ_URI_PERMISSION)`. No boot, a pasta salva é revalidada em `Dispatchers.IO` via `DocumentFile` + `GameDataScanner`.
- **Detecção**: passada 1 procura nomes exatos; passada 2 aceita extensões configuradas. Somente a raiz da pasta é varrida (rápido e previsível).

## Acessibilidade

- Alvos de toque ≥ 48 dp em todos os controles interativos.
- `contentDescription` configurável em todo controle (vindo do config).
- Contraste AA: texto principal `#F4F4F8` sobre base `#07070C`; textos secundários ≥ 50% de alpha sobre fundo escurecido por scrim/vinheta.
- **"Reduzir movimento"** respeita a escala de animação do sistema (auto-detecção) e expõe override manual no diálogo de ajustes — desativa parallax, partículas, pulso e entradas animadas.

## Performance

- Partículas atualizadas in-place (zero alocação por frame); invalidação apenas da fase de desenho.
- Parallax, pulso e grain rodam em `graphicsLayer`/Canvas — sem recomposição de árvore.
- Grain: bitmap 128px de ruído gerado uma única vez (seed fixa), tiling com `FilterQuality.None`.
- Entradas com stagger único (`rememberEntrance`) — nenhuma animação infinita além das 4 esperadas (parallax, pulso, grain, indicador de busca).

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
- Kotlin 2.0.21 · AGP 8.7.3 · Gradle 8.9 · Compose BOM 2024.10.01
- Sem dependências de terceiros (nada de Accompanist, Coil ou Hilt — proposital para virar módulo de qualquer port)

## Licença

MIT — veja [LICENSE](LICENSE).
