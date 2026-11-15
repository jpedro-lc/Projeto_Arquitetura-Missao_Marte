# Plano da Fase 3 — Core independente, apresentação LibGDX e launcher desktop

**Estado:** proposta para aprovação; implementação não iniciada.
**Diagnóstico:** 15/11/2026.
**Origem:** `evolucao-missao-marte/fases/fase-02-gradle/`.
**Destino após aprovação:** `evolucao-missao-marte/fases/fase-03-libgdx/`.

## 1. Solicitação e ponto de partida

Introduzir LibGDX no jogo existente, com isolamento explícito entre regras e
apresentação. O refinamento solicitado pelo usuário exige três módulos Gradle:
`core` (Java puro), `presentation` (telas LibGDX compartilhadas) e `lwjgl3`
(launcher e adaptadores desktop). A proposta inicial de dois módulos foi revista
para que o próprio artefato core não dependa de LibGDX.

Por solicitação explícita do usuário, as regras compartilhadas ficarão em
**`core/src/main/java/br/unifor/missaomarte/`**. A migração de namespace
de `solidexercicio10` para `br.unifor.missaomarte` integra o escopo desta fase,
apenas na nova cópia. Os pacotes das fases anteriores permanecem intactos.

**Complemento solicitado:** o usuário já possui as sprites e quer telas
compartilhadas que facilitem a futura versão Android. Planejar seu reaproveitamento
e separar apresentação, entrada, recursos e serviços específicos de plataforma.
As sprites foram adicionadas pelo usuário em
`evolucao-missao-marte/fases/fase-02-gradle/assets/`, nas coleções `SpaceRage` e
`Stylized Space Shooter Asset Kit`. Essa pasta permanece sem alterações; após
aprovação, os recursos necessários serão copiados para os assets da Fase 3.
Ainda será preciso inspecionar imagens, regiões e associação aos objetos do jogo
antes de escolher os arquivos efetivamente utilizados.

A Fase 2 contém 23 fontes de produção e dois arquivos de testes: a suíte com
162 verificações e o adaptador JUnit. Usa Java 17, Wrapper Gradle 8.7 e Groovy.
O relatório existente registra um teste JUnit aprovado, sem falhas, erros ou
skips. A lógica já recebe `Acao` e devolve `ResultadoAcao`/`EstadoPartida`, sem
depender do terminal: essa fronteira será reaproveitada.

O [pull request da Fase 2](https://github.com/jpedro-lc/Projeto_Arquitetura-Missao_Marte/pull/1)
foi integrado à `master`. A nova branch poderá partir da principal atualizada.
Não reutilizar automaticamente outra adaptação Gradle que esteja fora da sequência
de fases, nem modificar arquivos da equipe fora deste escopo.

## 2. Pré-requisitos e ambiente observado

| Requisito | Situação |
|---|---|
| JDK 17 | Instalado: OpenJDK 17.0.21.0.3. Continuar selecionando-o para Gradle e execução desktop. |
| Gradle | Wrapper 8.7 existente na Fase 2; copiar seus arquivos oficiais, não caches. |
| JUnit | BOM/Jupiter 5.10.2 e adaptador já existentes; reaproveitar. |
| Rede | Necessária inicialmente para obter LibGDX, backend e bibliotecas nativas pelo Maven Central. |
| Desktop Linux | Sessão Wayland, `DISPLAY=:0` e sockets X11 detectados. |
| Bibliotecas gráficas | `libGL`, `libGLX`, `libX11`, `libXrandr` e `libXi` presentes. |
| Execução gráfica | Ainda não validada para este projeto; deve ser conferida na implementação. |

Não é necessário instalar um aplicativo ou SDK separado chamado LibGDX.
As bibliotecas serão declaradas nos builds e obtidas pelo Gradle. Não usar o
gerador de um jogo novo para substituir o código existente.

## 3. Versões propostas e diferença em relação ao referencial

- **Java 17**, **Gradle 8.7**, **Groovy DSL** e **JUnit 5.10.2**: manter a base.
- **LibGDX 1.14.2**: versão estável apresentada pela documentação oficial
  consultada em 15/11/2026; usar a mesma versão para `gdx`, backend e natives.
- **LWJGL**: utilizar as dependências compatíveis declaradas pelo backend da
  versão escolhida, sem substituir arbitrariamente sua versão transitiva.

O referencial usa **LibGDX 1.12.1**, portanto **1.14.2 é uma divergência proposta
que depende da aprovação deste plano**. As ideias de Game, Screen e módulos serão
adaptadas ao código e conferidas na documentação oficial. Não alterar o arquivo
de referência ou apresentar sua versão antiga como a mais recente.

Fontes oficiais:

- [Versão estável LibGDX](https://libgdx.com/dev/versions/).
- [Ciclo de vida](https://libgdx.com/wiki/app/the-life-cycle).
- [Game e Screen](https://libgdx.com/wiki/start/simple-game-extended).
- [Backend da versão 1.14.2](https://github.com/libgdx/libgdx/tree/1.14.2/backends/gdx-backend-lwjgl3).
- [Interfaces para serviços de plataforma](https://libgdx.com/wiki/app/interfacing-with-platform-specific-code).
- [Assets e ciclo de vida](https://libgdx.com/wiki/managing-your-assets).
- [Arquivos internos e Android](https://libgdx.com/wiki/file-handling).
- [Viewports e diferentes resoluções](https://libgdx.com/wiki/graphics/viewports).

Não há uma afirmação de validação completa dessa combinação com janela gráfica
antes de criar o projeto. A resolução de dependências, compilação e inicialização
desktop fazem parte dos critérios de conclusão. Se exigirem outra versão ou
ampliação de escopo, apresentar a mudança antes de aplicá-la.

## 4. Responsabilidades dos módulos e estrutura

`core` contém somente regras, modelos, casos de uso e contratos de saída, como
`RankingRepository`. Não depende de LibGDX, OpenGL, Android, LWJGL, console,
texturas, resolução de tela ou implementação de arquivos. `JogoService` recebe
`Acao` e entrega `ResultadoAcao`/`EstadoPartida`, usando tipos Java próprios.

`presentation` contém a aplicação `Game`, telas `Screen`, renderização,
contratos/adaptadores compartilhados de entrada e gestão de sprites/recursos.
Depende do core e da API comum LibGDX, mas não de LWJGL ou Android. As mesmas
classes de apresentação serão reaproveitadas pelas aplicações das plataformas.

`lwjgl3` é a aplicação desktop: launcher, configuração de janela, composição
dos serviços, teclado desktop, implementação do ranking em arquivo e adaptador
de console legado. Depende de presentation e core, sem dependência inversa.

```text
lwjgl3 ----------> presentation ----------> core
   |                                       ^
   +---------------------------------------+

android futuro -> presentation e core
```

As setas indicam dependências de compilação. `core` não conhece os módulos que
utilizam suas regras. Android não será criado nesta etapa.

```text
fase-03-libgdx/
├── settings.gradle
├── build.gradle
├── gradlew / gradlew.bat
├── gradle/wrapper/
├── assets/                        # Imagens compartilhadas; não pertencem ao domínio
├── core/
│   ├── build.gradle
│   ├── src/main/java/br/unifor/missaomarte/
│   │   ├── model/                 # Entidades, Acao, EstadoPartida, ResultadoAcao
│   │   ├── service/               # JogoService e FabricaMissoes
│   │   └── repository/            # RankingRepository e RankingEntry
│   └── src/test/java/br/unifor/missaomarte/
├── presentation/
│   ├── build.gradle
│   ├── src/main/java/br/unifor/missaomarte/presentation/
│   │   ├── MissaoMarteGame.java
│   │   ├── screens/GameScreen.java
│   │   ├── input/                 # Contrato de entrada da apresentação
│   │   ├── assets/                # Gestão de recursos, não os arquivos de imagem
│   │   └── rendering/             # EstadoPartida -> desenho com sprites
│   └── src/test/java/br/unifor/missaomarte/presentation/
├── lwjgl3/
│   ├── build.gradle
│   ├── src/main/java/br/unifor/missaomarte/lwjgl3/
│   │   ├── Lwjgl3Launcher.java
│   │   ├── input/                 # Adaptador de teclado desktop
│   │   ├── repository/RankingArquivoRepository.java
│   │   └── console/               # MainConsole, JogoConsole, MapaRenderer
│   └── src/test/java/br/unifor/missaomarte/tests/
│       ├── TestesFase01.java
│       └── RegrasJogoTest.java
├── README.md
└── docs/fase-03-tutorial.md
```

### Contratos de independência

1. **Domínio sem imagens:** `Nave` mantém posição, vidas e passageiros. Ela não
   recebe Texture/Sprite nem herda de classes gráficas. O renderizador consulta
   seus dados de leitura para decidir qual região da imagem desenhar.
2. **Telas na apresentação:** cada tela é uma classe própria de
   `Screen`/`ScreenAdapter`, em presentation. Não importa launcher desktop,
   `org.lwjgl`, `java.awt`, Swing ou `android.*`. Core também não importa essas telas.
3. **Navegação comum:** `MissaoMarteGame` coordena a troca de telas. A tela não
   cria janelas, Activities, repositórios de disco ou outra aplicação. Recebe
   dependências por construtor, evitando Singleton.
4. **Entrada por intenção:** `EntradaJogo` pertence à apresentação, pois traduz
   interação em intenções do core. O adaptador desktop produz `Acao`; futuros
   botões de toque podem produzir as mesmas ações. O serviço não conhece teclas
   ou pixels. Não duplicar regras para cada tipo de controle.
5. **Resolução:** câmera/Viewport, inicialmente `FitViewport`, usam unidades
   virtuais e atualização em `resize`. A lógica do mapa não usa pixels físicos.
   Coordenadas de mouse/toque devem passar por `unproject` quando usadas no mundo.
6. **Recursos comuns:** sprites ficam em `assets/` na raiz da fase, com caminhos
   relativos e nomes exatos. O desktop empacota esses recursos; a futura
   configuração Android apontará para a mesma origem. Leitura de imagens usa a
   API LibGDX na apresentação, nunca caminhos absolutos ou File no core.
7. **Ownership:** o gerenciador de recursos da instância da apresentação usa
   `AssetManager`. Texturas não são estáticas, carregadas por frame ou descartadas
   por telas que apenas as tomaram emprestadas.
8. **Desenho separado:** renderizador transforma `EstadoPartida` em imagem.
   `GameScreen` coordena apresentação e entrada, sem conter regras de colisão,
   pontuação, persistência ou criação de entidades.
9. **Persistência por contrato:** `RankingRepository` fica no core, a
   implementação de arquivo no desktop. O launcher injeta a implementação no
   serviço. Assets de leitura não são destino do ranking ou de saves.

Exemplo: teclado envia `Acao.CIMA`, o serviço move a nave e desconta um ponto,
entrega `EstadoPartida`, e a apresentação desenha a sprite na posição recebida.
Um botão Android futuro troca a origem da ação; a regra e o renderizador comuns
continuam utilizáveis. Uma apresentação de console também pode usar core sem
carregar bibliotecas gráficas para executar suas regras.

Essa arquitetura permite compilar/testar core sem LibGDX e evita ciclos entre
módulos. A fronteira será conferida pela árvore de dependências e pelos imports.
Multiplataforma permanece uma direção arquitetural: launcher/Activity, build,
controles de toque, armazenamento e compatibilidade Java serão configurados e
validados na fase Android. Não prometer suporte antes desses testes.

## 5. Alterações nos arquivos existentes

| Origem na Fase 2 | Destino e alteração na nova fase |
|---|---|
| `model/*.java` | `core/.../model/`; alterar packages/imports para o novo namespace, mantendo regras e records. |
| `service/JogoService.java`, `FabricaMissoes.java` | `core/.../service/`; preservar operações, ordem do turno e injeção. |
| `repository/RankingRepository.java`, `RankingEntry.java` | `core/.../repository/`; contrato disponível às plataformas. |
| `repository/RankingArquivoRepository.java` | `lwjgl3/.../repository/`; manter formato e comportamento, atualizar imports. |
| `presentation/JogoConsole.java`, `MapaRenderer.java` | `lwjgl3/.../console/`; continuar como alternativa de execução e regressão. |
| `Main.java` | Transformar sua composição de console em `MainConsole` no desktop; o launcher gráfico é outra entrada. |
| Suíte e adaptador JUnit | `lwjgl3/src/test/java/...`; ajustar packages/imports para usar core e adaptadores desktop. Não remover verificações. |
| Build único | Dividir em build raiz e builds de core, presentation e lwjgl3; manter Wrapper/JDK e flags. |

As verificações existentes incluem persistência em arquivo e console. Por isso
serão executadas no módulo desktop, que enxerga a biblioteca compartilhada, sem
fazer `core` depender de `lwjgl3` ou presentation. Testes de regras independentes
ficarão no core; testes de adaptação da entrada ficarão em presentation e não
devem exigir OpenGL quando apenas verificarem a tradução em ações.

## 6. Configuração Gradle proposta

No `settings.gradle`:

```groovy
rootProject.name = 'missao-marte-fase-03'
include 'core', 'presentation', 'lwjgl3'
```

- Build raiz: coordenar subprojetos, versão única de LibGDX, Maven Central,
  compilação Java 17/UTF-8/`--release 17`/`-Xlint:all -Werror` e testes JUnit.
- `core`: plugin `java-library`, sem dependências externas de produção. Não
  declarar `gdx`, backend, natives ou uma dependência em presentation.
- `presentation`: plugin `java-library`, com `api project(':core')` e `gdx` como
  `api`, pois sua API pública usa tipos do core e `MissaoMarteGame` estende Game.
  Não adicionar backend, natives, AWT ou classes Android.
- `lwjgl3`: plugin `application`, dependências explícitas em `project(':core')`
  e `project(':presentation')`, backend
  `gdx-backend-lwjgl3` e bibliotecas nativas desktop no runtime, mantendo as
  versões alinhadas e conferindo as transitivas já fornecidas pelo backend.
- Classe principal desktop:
  `br.unifor.missaomarte.lwjgl3.Lwjgl3Launcher`.
- Tarefa `:lwjgl3:run` para janela gráfica e `:lwjgl3:runConsole` para a versão
  de console preservada, esta com `standardInput = System.in`.
- Diretório de trabalho das duas tarefas fixado na raiz da Fase 3, isolando seu
  ranking e sem copiar arquivos de pontuação anteriores.
- Recursos visuais mínimos, quando necessários, devem ser empacotados no
  classpath, não depender de arquivos da Fase 2 ou de caminhos absolutos.
- A origem compartilhada dos recursos será `assets/` na raiz da fase, incluída
  nos recursos desktop; um módulo Android futuro poderá configurar seu diretório
  de assets para essa mesma origem. A pasta não terá dados graváveis do usuário.

A pasta de fontes é exatamente a pedida pelo usuário. Ela não será substituída
por `core/src/br/...` ou por `br.edu.unifor`. O launcher pertence ao módulo próprio.

## 7. Adaptação inicial ao ciclo de vida LibGDX

Introduzir `MissaoMarteGame extends Game` no módulo presentation, recebendo
o serviço do core por construtor.
O launcher desktop cria o repositório e o serviço e os fornece ao jogo.
`GameScreen` consulta o estado de leitura e recebe `Acao` pelo contrato de entrada;
o adaptador desktop faz a tradução das teclas.

Explicar e implementar o ciclo de vida relevante:

- `Game.create()`: inicializar a aplicação e selecionar a primeira Screen.
- `Screen.show()`: preparar/ativar a tela; Screen não possui método `create()`.
- `render(float delta)`: desenhar e processar entrada sem bloquear no Scanner.
- `resize(...)`: atualizar viewport e projeção.
- `pause()`/`resume()`: manter estado lógico e gestão de recursos vinculados à
  instância, sem inventar persistência de retomada de processo ou alterar a
  contagem de tempo real da base. Destruição/recriação de Activity e morte do
  processo exigirão validação específica quando o Android for implementado.
- `hide()`/`dispose()`: definir dono e descarte dos recursos; liberar a tela e
  suas fontes/batches explicitamente, sem descarte duplicado.

O jogo continua **por turnos**. Renderizar vários frames não pode consumir pontos,
mover inimigos ou repetir uma ação sozinho. Uma pressão de tecla produz uma ação;
sem entrada, o estado lógico permanece. `JogoService` não recebe um loop de
renderização nem passa a fazer desenho.

## 8. Limite entre Fases 3 e 4

Nesta fase, a janela terá uma tela simples que consulta a partida existente e
prepara o desenho com sprites fornecidas pelo usuário. Antes de integrar imagens,
identificar os arquivos, dimensões, transparência e, se houver, as regiões de
spritesheet/atlas. Não inventar nomes de arquivos, recortes, significado de frames
ou animações. Não gerar novas artes para substituir as já disponíveis.

Uma representação textual pode servir como diagnóstico enquanto os recursos
não forem disponibilizados, mas isso não será apresentado como integração das
sprites concluída. Os recursos agora estão disponíveis na pasta indicada. A modularização e os
testes de lógica são independentes da escolha das artes; a validação do desenho
depende da inspeção e seleção dos arquivos reais.

A tela inicia com os mesmos valores padrão do console: piloto anônimo,
dificuldade média e limite 5. Mostra controles e resultado. `w/s/a/d`, `c` e `q`
viram as ações existentes pelo adaptador desktop; `R` permite reiniciar após o
encerramento e `Esc` solicita a saída pelo fluxo da aplicação, sem inserir
teclas obrigatórias nas regras ou telas compartilhadas. Configuração completa de piloto/dificuldade/mapa e
menu de ranking continuam disponíveis pela alternativa de console nesta etapa.

O reaproveitamento básico das sprites passa a integrar este plano por solicitação
do usuário. Animação, sons, HUD elaborado, menus gráficos completos e movimento
contínuo continuam na Fase 4. Cada tela que vier a ser criada deverá respeitar
os contratos de independência acima. Não substituir Nave e Asteroide pelos
exemplos do referencial nem alterar suas colisões, pontuação ou vitória.

## 9. Referencial e divergências

Usar as seções pelo título do
[tutorial-api-gradle-libgdx.md](../tutorial-api-gradle-libgdx.md):

- **Parte 3 – A API libGDX**: **História e Origem**, **O que é a libGDX?**,
  **Características Principais** (módulos e classes), **Versões da libGDX**,
  **Por que a libGDX usa Gradle?**, **Alternativas à libGDX** e **Ecossistema da libGDX**.
- **Projeto Multi-módulo (libGDX)** da Parte 2 e **Parte 8 – Configuração do
  build.gradle**: raiz, `settings.gradle`, `core/build.gradle`, `lwjgl3/build.gradle`.
- **Parte 5 – Arquitetura do Jogo**: **O Game Loop da libGDX**, **Conceito de
  Screen** e **Diagrama de Classes**.
- **Parte 6 – Implementação do Jogo**: **Classe Principal – MissaoMarteGame.java**,
  **Tela Principal – GameScreen.java** e **Launcher Desktop – Lwjgl3Launcher.java**,
  somente como exemplos de estrutura; a lógica vem da Fase 2.

Diferenças explícitas: LibGDX 1.14.2 em vez de 1.12.1; namespace solicitado pelo
usuário em vez de `solidexercicio10`; adaptação por turnos em vez do jogo contínuo
simplificado; persistência e console no desktop; terceiro módulo presentation
para retirar completamente LibGDX do core; `api` em presentation para sua API
pública de Game/core; natives no runtime e somente repositórios necessários. Não copiar
configurações de RoboVM, Android ou extensões não utilizadas.

A separação adicional de presentation, a inclusão básica das sprites e o
contrato de entrada são complementos solicitados pelo usuário. O uso de três
módulos diverge da organização em dois módulos do referencial por uma razão
explícita: manter o artefato core independente da interface gráfica. Gestão por `AssetManager`, empacotamento de recursos e
viewports serão fundamentados também pela documentação oficial. A parte gráfica
avançada do referencial permanece para a Fase 4.

No tutorial da fase, citar o referencial por link relativo
`../../../tutorial-api-gradle-libgdx.md`, complementar ciclo de vida com fontes
oficiais e manter os 12 itens didáticos exigidos pelo prompt mestre.

## 10. Riscos, critérios de conclusão e validação

| Risco | Estratégia |
|---|---|
| Ciclo entre módulos | presentation depende de core; lwjgl3 depende de ambos; core não depende deles. |
| Troca de package quebrar imports/testes | Compilar os três módulos e reexecutar todas as verificações herdadas. |
| Lógica avançar a cada frame | Testar que atualização visual sem entrada não dispara ações do serviço. |
| Interface bloquear a janela | Scanner fica apenas na alternativa de console, nunca em render. |
| Natives/driver/display não funcionarem | Validar janela real e relatar falhas separadamente da compilação e dos testes de lógica. |
| Recursos gráficos vazarem | Ownership explícito e teste de inicialização, resize e encerramento. |
| Ranking afetar fase anterior | Diretório de trabalho isolado e hashes/arquivos temporários nos testes. |
| Desktop contaminar as telas comuns | Conferir imports/dependências e manter entrada/serviços específicos atrás dos contratos. |
| Imagens não funcionarem fora da máquina original | Caminhos relativos em assets, recursos empacotados e validação da distribuição. |
| Resoluções diferentes distorcerem o jogo | Validar resize e mais de uma proporção usando o viewport. |

Após aprovação e implementação, com JDK 17:

```bash
./gradlew projects
./gradlew clean build
./gradlew :core:test :presentation:test :lwjgl3:test --rerun-tasks
./gradlew :lwjgl3:runConsole --console=plain
./gradlew :lwjgl3:run
./gradlew :lwjgl3:installDist
```

Conferir a execução das 162 verificações após as mudanças de imports e novos
testes relevantes, sem contar tarefas `NO-SOURCE` como testes aprovados. Conferir
árvore de dependências para confirmar core sem LibGDX e presentation sem backend.
Validar janela, renderização,
entrada por turno, redimensionamento e fechamento; quando viável, usar smoke
desktop com encerramento automático para verificar criação e descarte.
Conferir que renderizar sem entrada não avança a missão, que teclado é uma
implementação substituível e que texturas compartilhadas têm um único dono.
Inspecionar e validar as sprites reais já adicionadas pelo usuário, sem mover
ou alterar sua pasta de origem.

O ambiente tem bibliotecas e sessão gráfica detectadas, mas isso não comprova
suporte OpenGL suficiente ou acesso do executor ao display. Se a execução gráfica
não for possível, informar claramente o bloqueio e os testes executados; não
apresentar teste headless como prova de renderização. Não instalar drivers ou
alterar o sistema automaticamente.

Critérios: três módulos independentes de fases anteriores, core no caminho
solicitado e sem dependência de apresentação/LibGDX, regras preservadas,
framework sem bloqueio de console, testes
relevantes aprovados, execução desktop verificada ou limitação declarada,
tutorial/README completos e atualização do índice da evolução.

## 11. Git e aprovação

Depois da aprovação, atualizar as referências remotas e criar `fase-03-libgdx`
a partir da principal que já contém a Fase 2 integrada. Registrar commits por
etapa e preparar pull request para revisão, sem integrar diretamente na master.

As versões anteriores, o referencial e o prompt permanecem intactos. Até a
aprovação explícita deste plano, não criar a pasta da Fase 3, copiar fontes ou
implementar seus builds. A regra 8 do [prompt mestre](../PROMPT_MESTRE.md)
exige essa aprovação separada, inclusive para a versão de LibGDX proposta.
