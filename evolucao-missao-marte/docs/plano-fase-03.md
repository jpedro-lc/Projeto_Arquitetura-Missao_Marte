# Plano aprovado da Fase 3 — LibGDX conforme o referencial

**Autorização:** em 15/11/2026, o usuário solicitou: “vamos seguir a fase 3 conforme o referencial”.
**Origem:** `fases/fase-02-gradle/`.
**Destino:** `fases/fase-03-libgdx/`.

## 1. Diagnóstico e decisão de escopo

A Fase 2 contém 23 fontes de produção, Java 17, Gradle Wrapper 8.7, Groovy e
JUnit 5.10.2. A suíte herdada executa 162 verificações. `JogoService` recebe
`Acao` e devolve `ResultadoAcao`/`EstadoPartida`, sem depender do terminal.
Essa fronteira será preservada na adaptação gráfica.

A orientação mais recente substitui a proposta de três módulos: seguir a
organização do referencial, com **core e lwjgl3**, usando **LibGDX 1.12.1**.
`presentation` será um pacote dentro de core, não um terceiro subprojeto Gradle.
O artefato core passa a depender de LibGDX, mas seus pacotes de regras não
importam classes gráficas. Essa diferença entre independência de pacotes e
independência de módulos deve ficar explícita no tutorial.

Permanecem os pedidos anteriores compatíveis: pacote
`br.unifor.missaomarte`, fontes em `core/src/main/java/`, telas compartilhadas
sem dependência de backend e uso das sprites existentes já nesta fase.
Nenhum módulo Android será criado agora.

## 2. Ambiente e versões

- Java 17: `/usr/lib/jvm/java-17-openjdk-amd64` neste ambiente.
- Gradle Wrapper 8.7 e Groovy: reaproveitar os arquivos oficiais da Fase 2.
- LibGDX **1.12.1**, a versão usada no referencial; alinhar gdx, backend e natives.
- LWJGL: dependências transitivas escolhidas pelo backend 1.12.1.
- JUnit BOM/Jupiter **5.10.2**, preservando a base de testes.
- Maven Central; rede necessária para o primeiro download das bibliotecas.
- Sessão Linux com display e bibliotecas gráficas detectadas. A abertura da
  janela ainda precisa ser validada; compilação não comprova renderização.

## 3. Estrutura e responsabilidades

```text
fase-03-libgdx/
├── build.gradle / settings.gradle
├── gradlew / gradlew.bat / gradle/wrapper/
├── core/
│   ├── build.gradle
│   ├── assets/                         # imagens compartilhadas
│   └── src/main/java/br/unifor/missaomarte/
│       ├── MissaoMarteGame.java         # Game e ciclo de vida
│       ├── model/                      # regras herdadas, sem Gdx
│       ├── service/                    # JogoService e FabricaMissoes, sem Gdx
│       ├── repository/                 # contrato e RankingEntry
│       ├── screen/GameScreen.java      # tela compartilhada
│       └── presentation/               # entrada, coordenação, recursos e desenho
├── lwjgl3/
│   ├── build.gradle
│   └── src/main/java/br/unifor/missaomarte/lwjgl3/
│       ├── Lwjgl3Launcher.java
│       ├── input/                      # teclado desktop
│       ├── repository/                 # ranking em arquivo
│       └── console/                    # MainConsole, JogoConsole, MapaRenderer
├── README.md
└── docs/fase-03-tutorial.md
```

Dependência de compilação: `lwjgl3 -> core -> gdx`. O core não conhece o
desktop. O futuro módulo Android poderá depender desse mesmo core.
`Nave`, `Missao`, `JogoService` e os contratos continuam sem Texture, Sprite,
Screen, Gdx, LWJGL, Android, Scanner ou caminhos de imagens.

As telas recebem as dependências por construtor. O launcher monta repositório,
serviço e entrada desktop. O desenho consulta a fotografia imutável da partida;
não movimenta entidades nem recalcula colisões. Entrada produz intenções, sem
fixar teclas dentro das regras. Não criar framework adicional, Singleton ou
dependências para plataformas não utilizadas.

## 4. Migração de classes e build

| Origem da Fase 2 | Destino e alteração |
|---|---|
| `model`, `service` | core; apenas substituir namespace/imports. |
| `RankingRepository`, `RankingEntry` | core; contratos compartilhados. |
| `RankingArquivoRepository` | lwjgl3; preservar formato e comportamento. |
| `JogoConsole`, `MapaRenderer` | pacote console de lwjgl3; preservar execução. |
| `Main` | `MainConsole` no desktop; nova entrada gráfica separada. |
| Testes herdados | lwjgl3/test, pois verificam também console e arquivos; manter as 162 verificações. |

O `settings.gradle` inclui somente `core` e `lwjgl3`. Build raiz configura
Java 17, UTF-8, `--release 17`, `-Xlint:all -Werror`, JUnit e Maven Central.
Core usa `java-library` e expõe gdx com `api`, pois sua API pública estende Game
e usa Screen. Desktop usa `application`, depende de core e do backend lwjgl3;
as bibliotecas nativas pertencem ao runtime.

`core/assets` será incluído nos recursos da distribuição desktop, conforme o
referencial. Caminhos internos serão relativos. Não depender da pasta da Fase 2
durante execução. Ranking gravável fica na raiz da Fase 3 nas tarefas Gradle,
separado dos assets e das fases anteriores. Manter o checksum do Wrapper.

## 5. Ciclo de vida e comportamento

- `Game.create()`: criar recursos gráficos e selecionar GameScreen.
- `Screen.show()`: ativar a tela; Screen não tem create().
- `render(delta)`: processar uma intenção e desenhar, sem bloquear em Scanner.
- `resize`: aplicar FitViewport com coordenadas virtuais.
- `pause/resume`: suspender entrada; não alterar a regra herdada de tempo real.
- `hide/dispose`: explicitar propriedade dos recursos e liberar tela, batch,
  fontes e AssetManager uma única vez. Game não descarta telas automaticamente.

Preservar o jogo **por turnos**: frames sem comandos não movem inimigos, não
consomem pontos e não mudam colisões; o relógio real pode avançar como na base.
Movimento contínuo não está autorizado pela Fase 4. Animação visual futura não
deve modificar essa regra implicitamente.

A janela inicial usa piloto anônimo, dificuldade média e limite 5, os padrões
do console. Mostrar pontos, vidas, passageiros, resultado e controles.
Teclas w/s/a/d, c e q traduzem as ações existentes; R reinicia uma partida
encerrada e Esc fecha a aplicação. Configuração e ranking completos continuam
acessíveis pelo console nesta fase. Menus gráficos completos ficam para a Fase 4.

## 6. Assets existentes e limite da Fase 4

Copiar somente as quatro imagens necessárias da origem
`fases/fase-02-gradle/assets/` para `core/assets/` da nova fase, documentando
os nomes de destino e preservando os originais:

| Uso | Origem relativa | Destino relativo a core/assets |
|---|---|---|
| Nave | `SpaceRage/SpaceRage/Player/player_b_m.png` | `images/nave.png` |
| Inimigo | `SpaceRage/SpaceRage/Enemies/enemy_1_r_m.png` | `images/inimigo.png` |
| Asteroide | `Stylized Space Shooter Asset Kit/Asteroid/Asteroid_1.png` | `images/asteroide.png` |
| Fundo | `Stylized Space Shooter Asset Kit/Background/background.png` | `images/fundo.png` |

As imagens já foram inspecionadas: nave/inimigo 64x64 RGBA, asteroide 110x105
RGBA e fundo 1672x941 RGB. Usar AssetManager da instância, sem cargas por frame
ou texturas estáticas. Preservar proporções e colisões lógicas. Passageiros e
base podem usar identificadores de texto por não haver sprite específica mapeada.

O XML SpaceRage não é um atlas nativo LibGDX; usar os PNGs individuais.
O HUD Neon contém valores e funcionalidades fixas ausentes no jogo e não será
exibido inteiro. Dados do HUD vêm de EstadoPartida. Não acrescentar tiros,
escudos ou regras para corresponder à arte. Sons, animações e HUD elaborado
permanecem para a Fase 4. A integração básica dessas quatro sprites nesta fase
preserva a solicitação anterior do usuário.

## 7. Referencial e adaptações necessárias

Usar o [referencial](../tutorial-api-gradle-libgdx.md), sem alterá-lo:

- Parte 3: API LibGDX, arquitetura, módulos, classes e versões.
- Projeto Multi-módulo da Parte 2 e Parte 8: settings e builds raiz/core/lwjgl3.
- Parte 5: Game Loop, Screen e arquitetura.
- Parte 6: MissaoMarteGame, GameScreen e Lwjgl3Launcher como estrutura.

Seguir seus dois módulos, namespace, versão LibGDX e `core/assets`. Adaptar os
exemplos ao jogo existente, conforme exige o prompt: não substituir entidades
pelas do jogo simplificado contínuo. Usar `api` para os tipos públicos LibGDX,
natives no runtime e apenas dependências utilizadas. Antecipar as quatro imagens
não transforma esta fase em implementação completa da Fase 4.

Complementos oficiais para conferir APIs, ciclo de vida e portabilidade:

- [Ciclo de vida](https://libgdx.com/wiki/app/the-life-cycle).
- [Game e Screen](https://libgdx.com/wiki/start/simple-game-extended).
- [Recursos](https://libgdx.com/wiki/managing-your-assets).
- [Arquivos](https://libgdx.com/wiki/file-handling).
- [Viewports](https://libgdx.com/wiki/graphics/viewports).

O tutorial da fase deve explicar os 12 itens exigidos no prompt, com código real,
antes/depois e link `../../../tutorial-api-gradle-libgdx.md` a partir de docs.

## 8. Riscos e validação

| Risco | Validação |
|---|---|
| Namespace quebrar referências | Build com warnings tratados como erros e suíte herdada. |
| Regras passarem a depender da interface | Conferir imports de model/service/repository e direção dos módulos. |
| Render avançar turnos | Testar coordenação sem entrada e uma ação por comando. |
| Desktop contaminar telas | Conferir ausência de lwjgl, AWT e Android no código compartilhado. |
| Assets dependerem desta máquina | Conferir cópias por hash e executar distribuição fora do diretório do projeto. |
| Recursos/ciclo de vida incorretos | Abrir janela, redimensionar, testar entrada e encerrar, conferindo logs. |
| Fases anteriores alteradas | Conferir arquivos/hashes antes e depois, sem copiar caches ou ranking. |

Comandos previstos: `./gradlew projects`, `./gradlew clean build`,
`:core:test`, `:lwjgl3:test`, `:lwjgl3:runConsole`, `:lwjgl3:run` e
`:lwjgl3:installDist`. Acrescentar testes significativos de coordenação/entrada,
sem exigir OpenGL para testar regras. Não contar NO-SOURCE como teste aprovado.
Verificar as 162 checagens herdadas e as quatro texturas na janela e distribuição.
Se o display não puder ser acessado, relatar a limitação; teste headless não
comprova renderização. Não instalar drivers ou alterar o sistema automaticamente.

## 9. Entrega e Git

Criar `fase-03-libgdx` a partir da principal atualizada. Implementar apenas essa
fase, atualizar seu tutorial/README e o índice da evolução. Registrar alterações
em branch própria e preparar revisão por pull request, sem integrar na master.
Preservar fases anteriores, missao1, missao2, prompt e referencial.

A autorização atual cobre esta Fase 3 conforme o referencial e as adaptações
necessárias para reutilizar a lógica e as imagens solicitadas. Não autoriza
implementação das fases seguintes.
