# Plano aprovado da Fase 4 — Interface gráfica

**Autorização:** em 17/11/2026, o usuário respondeu: “Aprovo com opção A” (instalar JDK 17).

**Origem:** `fases/fase-03-libgdx/` (concluída, PRs #2–#5 integrados).
**Destino:** `fases/fase-04-interface-grafica/`.
**Base complementar:** proposta SpaceRage em `../work/previa-spacerage/plano-revisao.md`
(layout, base, HUD e rodapé), incorporada aqui como parte da Fase 4.

## 1. Diagnóstico do estado de origem

A Fase 3 já antecipou parte do que a Fase 4 pede. O que existe e o que falta:

| Tema da Fase 4 | Situação na Fase 3 | Lacuna |
|---|---|---|
| Sprites/texturas | Nave, inimigo, asteroide, passageiros, fundo via `AssetManager` | Base é um quadrado + texto "BASE"; `new TextureRegion` criado a cada frame para asteroide/inimigo/nave; assets `ui_spacerage/` (base, HUD, rodapé, painéis, asteroides, astronautas) ainda não usados |
| Movimentação | Nave "teleporta" de célula em célula | Não há deslocamento visual entre células |
| Entrada | WASD/C/Q/R/P/ESC, um comando por pressão (`TecladoEntradaJogo`) | Adequada; só ajustes de feedback (som de erro já existe) |
| Colisões | Regra discreta (mesma célula) no `JogoService`; explosão na posição da nave | Sem feedback de dano na nave (piscar), sem explicação didática grid × `Intersector` |
| Animações | Explosão (11 quadros) e propulsor (5 quadros) com `Animation` | Passageiro parado, base estática, sem animação de resgate/pouso |
| Sons | Música, impactos, resgate, vitória, derrota, menu, erro | Completo; só falta som ligado a eventos novos se houver (nenhum previsto) |
| HUD | 4 painéis separados + linha de vidas | Painel contínuo SpaceRage com ícones de nave/astronauta |
| Layout | `FitViewport 1280×800` gera faixas laterais; fundo só na área central | Fundo cobrindo a janela inteira |

Regras (`model`, `service`, `repository`) não importam LibGDX; a fronteira
`JogoService.executar(Acao) → ResultadoAcao(EstadoPartida, eventos)` continua sendo
a única porta da apresentação para as regras. Último resultado validado: 44 casos
JUnit (incluindo as 162 verificações herdadas) e smoke gráfico com 9 capturas.

## 2. Escopo

**Dentro:** apresentação gráfica (sprites, texturas, animações, movimento visual,
feedback de colisão, sons, HUD, menus/pausa SpaceRage, layout de tela cheia).

**Fora:** qualquer alteração em `model`/`service`/regras de pontuação, colisão,
vitória ou ranking; tiro/escudo (SPACE continua omitido); movimento contínuo como
regra (o jogo **continua por turnos**); servidor e Android.

## 3. Alterações propostas (todas em `core/presentation` e `core/screen`)

1. **Cópia da Fase 3** para a nova pasta, sem `build/`, `.gradle/`, rankings ou
   capturas; renomear `rootProject.name`. Assets `Preview/` e `showcase/` não são
   carregados pelo jogo (servem só de referência).
2. **Movimentação visual (interpolação)** — novo `AnimadorPosicoes`: ao receber um
   `ResultadoAcao`, guarda a posição anterior e anima a nave até a nova célula em
   ~0,15 s (`Interpolation.smooth`). Inimigos também deslizam. Puramente visual:
   o estado lógico já mudou; nenhum frame executa turno. Durante a animação, novas
   teclas continuam sendo aceitas (a animação é “cortada” para o destino).
3. **Sprites e texturas** — `TextureRegion`s criados uma vez no construtor do
   renderer; filtragem `Nearest` para pixel art; asteroides em 3 tamanhos/2
   variantes escolhidos de forma determinística pela posição (só aparência).
4. **Animações por quadros** — astronauta `idle_01..04` (loop) e `rescue_01..04`
   (resgate); base com estados `idle` → `active` (todos a bordo) → `landing` →
   `complete` (após a vitória já decidida pelo core). Extrair `EstadoVisualBase`
   (Java puro, testável) que só lê `EstadoPartida`.
5. **Feedback de colisão** — explosão no ponto do impacto (já existe) + nave
   piscando ~0,6 s e leve tremor da câmera. Tutorial explica por que a colisão de
   regra é por célula (discreta) e onde `Rectangle.overlaps`/`Intersector` do
   referencial se aplicariam (jogo contínuo).
6. **HUD e layout SpaceRage** — painel superior contínuo e inferior com ícones;
   rodapé metálico; menu e pausa com `menu_panel`/`pause_panel` e botões
   normal/hover/pressed. Fundo desenhado uma vez na janela inteira
   (`ExtendViewport` + recorte proporcional), removendo as faixas laterais.
   Valores continuam vindo de `HudDados`/`EstadoPartida`.
7. **Recursos** — novos caminhos cadastrados em `RecursosJogo` (um único
   `AssetManager`, descarte único). Nada criado em `render()`.
8. **Sons** — manter `AudioPartida` como está; apenas conferir volume/mixagem com
   os novos eventos visuais (nenhum novo som obrigatório).

Classes afetadas: `RecursosJogo`, `PartidaRenderer`, `EfeitosPartida`,
`HudPartida`, `EstiloInterface`, `GameScreen`, `MenuScreen`; novas:
`AnimadorPosicoes`, `EstadoVisualBase`. Testes: `InterfacePartidaTest`,
novos testes das duas classes novas, `VerificacaoDesktop` (smoke).

## 4. Padrões e princípios

- **Observer (leve):** `ResultadoAcao` é consumido uma vez por áudio, efeitos e
  animador — já é o padrão da Fase 3; o animador entra como mais um consumidor.
- **State:** `EstadoVisualBase` modela os estados visuais da plataforma.
- **GRASP Information Expert / Pure Fabrication:** animação e conversão de
  coordenadas ficam em classes de apresentação, não na `Nave`.
- **SRP/OCP:** regras não mudam; nova aparência entra por novas classes de
  apresentação. **DIP:** telas continuam recebendo dependências por construtor.

## 5. Referencial teórico (somente leitura)

- Parte 3: “Sprites e Texturas”, “Sons e Áudio”, “HUD (Head-Up Display)”,
  “Detecção de Colisões” e “Efeito visual de explosão”.
- Parte 6: `Nave`, `Asteroide` e `GameScreen` — apenas como exemplos.
- Parte 7: `ShapeRenderer` — citado como alternativa (debug de hitbox opcional).
- Parte 9: controles e execução.
- Complementos oficiais (não cobertos pelo referencial): animação por quadros
  (`https://libgdx.com/wiki/graphics/2d/2d-animation`), interpolação
  (`https://libgdx.com/wiki/math-utils/interpolation`) e viewports
  (`https://libgdx.com/wiki/graphics/viewports`).

**Divergências com o referencial:** o exemplo do tutorial é contínuo (movimento
por `delta` e colisão por retângulos); aqui a regra é discreta por turnos e o
movimento contínuo é só visual. Pacote do tutorial e do projeto é
`br.unifor.missaomarte` (aprovado na Fase 3); o prompt cita
`br.edu.unifor.missaomarte` — **não** será migrado nesta fase.

## 6. Versões e ambiente

Manter Java 17 (`--release 17`), Gradle Wrapper 8.7 (Groovy), LibGDX/FreeType
1.12.1 e JUnit 5.10.2, iguais ao referencial e à Fase 3.

**Divergência de ambiente (Windows desta máquina, 17/11/2026):** não há JDK 17 —
apenas OpenJDK 25.0.2, 26.0.1 e 26.0.2.1 em `~/.jdks`, e nenhum `java` no PATH.
Gradle 8.7 não executa sobre JDK 25/26. Opções:

- **A (recomendada):** instalar JDK 17 (ex.: IntelliJ → *Download JDK* →
  Temurin 17) e apontar `JAVA_HOME` para ele. Nenhuma versão do projeto muda.
- **B:** atualizar o Wrapper para Gradle 9.x (roda em JDK 25) mantendo
  `--release 17`. Muda a versão do referencial e exige registro/aprovação.

## 7. Riscos e validação

| Risco | Mitigação/validação |
|---|---|
| Animação virar regra (turno por frame) | Teste: sem comando, `EstadoPartida` idêntico após N `render`; animador não chama `JogoService` |
| Animação atrasar vitória/ranking | `EstadoVisualBase` só lê a situação já decidida; teste de transição |
| Efeitos consumidos 2× | Mesmo teste de consumo único da Fase 3 |
| Vazamento de recursos | Nenhum `new Texture/TextureRegion` em `render`; descarte único conferido no smoke |
| Layout quebrar em outras resoluções | Smoke com capturas em 1366×768, 1600×900, 1920×1080 |
| Fases anteriores alteradas | `git diff --stat` restrito a `fase-04-*`, README do índice e este plano |

Critérios de conclusão: `./gradlew clean build` verde (44 casos + novos),
`:lwjgl3:smokeDesktop` com capturas nas 3 resoluções, `:lwjgl3:run` jogável,
`:lwjgl3:runConsole` funcionando, `installDist` executado fora do projeto,
`docs/fase-04-tutorial.md` com os 12 itens e diagramas Mermaid, README da fase e
índice atualizados. Branch `fase-04-interface-grafica` e PR, sem merge direto.
