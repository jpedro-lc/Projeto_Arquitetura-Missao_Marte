# Evolução arquitetural — Missão Marte

Esta pasta reúne a evolução do jogo Java da equipe. A Fase 1 partiu da versão
SOLID existente, a Fase 2 migrou essa versão para Gradle e a Fase 3 introduziu
LibGDX com os módulos core e lwjgl3. As entregas anteriores continuam preservadas.

## Onde estamos

- **Preparação:** concluída; caminhos, regras de trabalho e planejamento organizados.
- **Fase 1:** aprovada pelo usuário, implementada e validada; veja o
  [projeto de console](fases/fase-01-refatoracao/README.md) e o
  [tutorial da refatoração](fases/fase-01-refatoracao/docs/fase-01-tutorial.md).
- **Fase 2:** plano aprovado, implementada e validada com Gradle 8.7 e Java 17;
  veja o [projeto Gradle](fases/fase-02-gradle/README.md) e o
  [tutorial da migração](fases/fase-02-gradle/docs/fase-02-tutorial.md).
- **Fase 3:** aprovada conforme o referencial, implementada com LibGDX 1.12.1,
  core/lwjgl3, HUD superior, ambiente espacial, menu, pausa e áudio; testes e janela gráfica validados. Veja o
  [projeto LibGDX](fases/fase-03-libgdx/README.md) e o
  [tutorial do framework](fases/fase-03-libgdx/docs/fase-03-tutorial.md).
  A [reformulação visual de 16/11/2026](fases/fase-03-libgdx/docs/reformulacao-interface.md)
  foi solicitada como evolução da própria Fase 3, mantendo as regras existentes.
- **Fase 4:** plano aprovado em 17/11/2026 (opção A: JDK 17), implementada e validada;
  veja o [projeto da interface gráfica](fases/fase-04-interface-grafica/README.md) e o
  [tutorial](fases/fase-04-interface-grafica/docs/fase-04-tutorial.md).
- **Fase 5:** plano aprovado em 22/11/2026 seguindo o enunciado do professor (modo
  convidado mantido), implementada e validada; veja o
  [projeto cliente + servidor](fases/fase-05-servidor-ranking/README.md) e o
  [tutorial](fases/fase-05-servidor-ranking/docs/fase-05-tutorial.md).
- **Fase 6:** implementada pela equipe e revisada em 24/11/2026 (registro retroativo no
  plano); desktop e Android compartilham o `core`, com toque, *swipe* e controle
  Bluetooth. Veja o [projeto](fases/fase-06-android/README.md) e o
  [tutorial](fases/fase-06-android/docs/fase-06-tutorial.md). Capturas no Android em
  [`docs/evidencias/android/`](fases/fase-06-android/docs/evidencias/android/).
- **Fase 7:** plano aprovado em 27/11/2026 e entregue em 28/11/2026 (PRs #18 a #22).
  Tem servidor no Render (Docker + PostgreSQL), jogo em tempo real (clássico
  mantido), modo Infinito, 9 rankings por modo e dificuldade, corações, direcional
  no celular e pacote da apresentação
  ([Release](https://github.com/jpedro-lc/Projeto_Arquitetura-Missao_Marte/releases/tag/v7.0-apresentacao)).
  Veja o [projeto](fases/fase-07-evolucao-livre/README.md) e o
  [tutorial](fases/fase-07-evolucao-livre/docs/fase-07-tutorial.md).
- **Base dos fontes:** [`../missao2/src/solidexercicio10/`](../missao2/src/solidexercicio10/).
- **Referência histórica:** [`../missao2/src/exercicio10/`](../missao2/src/exercicio10/).

A pasta `missao2` é uma entrega anterior e não corresponde à Fase 2 desta evolução.
Não é necessário copiar o repositório, mover as entregas ou criar outro workspace.

## Documentos de trabalho

- [Prompt mestre](PROMPT_MESTRE.md): escopo das sete fases e regras de execução.
- [Plano da Fase 1](docs/plano-fase-01.md): diagnóstico e escopo aprovado.
  O documento foi preservado como registro da proposta anterior à implementação;
  o estado atual e os resultados estão neste índice e no README da fase.
- [Plano da Fase 2](docs/plano-fase-02.md): proposta aprovada para migrar a Fase 1
  para Gradle, preservada como registro do planejamento. A aprovação e os
  resultados da implementação estão registrados neste índice e na nova fase.
- [Plano da Fase 3](docs/plano-fase-03.md): organização aprovada conforme o
  referencial, com separação entre regras e apresentação dentro do core.
- [Plano da Fase 7](docs/plano-fase-07.md): evolução livre aprovada (nuvem, tempo real,
  Infinito e rankings), com as decisões D1 a D5 e o registro da entrega.
- [Plano da Fase 6](docs/plano-fase-06.md): registro retroativo do Android, decisões,
  validação e pendências.
- [Plano da Fase 5](docs/plano-fase-05.md): servidor Spring Boot, JWT, JPA e
  ranking global; registra as divergências com o prompt mestre.
- [Plano da Fase 4](docs/plano-fase-04.md): interface gráfica com SpaceRage,
  movimento visual e animações, sem alterar regras.
- [Referencial de API, Gradle e LibGDX](tutorial-api-gradle-libgdx.md): material
  somente leitura para as Fases 2, 3 e 4; não é a especificação da Fase 1.

## Organização

```text
evolucao-missao-marte/
├── .gitignore
├── README.md
├── PROMPT_MESTRE.md
├── tutorial-api-gradle-libgdx.md
├── docs/
│   ├── plano-fase-01.md
│   ├── plano-fase-02.md
│   ├── plano-fase-03.md
│   ├── plano-fase-04.md
│   ├── plano-fase-05.md
│   └── plano-fase-06.md
└── fases/
    ├── .gitkeep
    ├── fase-01-refatoracao/
    │   ├── README.md
    │   ├── executar.sh
    │   ├── src/solidexercicio10/
    │   ├── tests/
    │   └── docs/fase-01-tutorial.md
    ├── fase-02-gradle/
    │   ├── README.md
    │   ├── build.gradle / settings.gradle
    │   ├── gradlew / gradlew.bat
    │   ├── gradle/wrapper/
    │   ├── src/main/java/solidexercicio10/
    │   ├── src/test/java/solidexercicio10/tests/
    │   └── docs/fase-02-tutorial.md
    ├── fase-03-libgdx/
    │   ├── README.md
    │   ├── build.gradle / settings.gradle
    │   ├── gradlew / gradlew.bat / gradle/wrapper/
    │   ├── core/src/main/java/br/unifor/missaomarte/
    │   ├── core/assets/            # sprites, sons, fontes, licenças e referências
    │   ├── lwjgl3/src/main/java/br/unifor/missaomarte/lwjgl3/
    │   └── docs/fase-03-tutorial.md
    ├── fase-04-interface-grafica/  # mesma estrutura; apresentação animada
    │   ├── README.md
    │   └── docs/fase-04-tutorial.md
    ├── fase-05-servidor-ranking/
    │   ├── README.md
    │   ├── cliente/                # Fase 4 evoluída (Gradle 8.7)
    │   ├── servidor/               # Spring Boot 4.1.2 (Gradle 9.8.2)
    │   └── docs/fase-05-tutorial.md + evidencias/
    └── fase-06-android/
        ├── README.md
        ├── cliente/                # core + lwjgl3 + android (AGP 8.3.2, SDK 34)
        ├── servidor/               # mesmo servidor, bind configurável
        └── docs/fase-06-tutorial.md + evidencias/
```

Cada pasta de fase será criada somente depois da aprovação do respectivo plano.
A Fase 1 contém uma cópia refatorada dos fontes da versão SOLID; as seguintes receberão
os arquivos necessários da fase anterior concluída. Não copie rankings, classes
compiladas ou caches. Depois de criada, cada fase deve compilar sem depender dos
fontes da `missao2` ou de outras fases.

## Índice das fases

| Fase | Pasta prevista dentro de `fases/` | Situação |
|---|---|---|
| 1 — Refatoração arquitetural | `fase-01-refatoracao/` | Concluída: 162 verificações aprovadas |
| 2 — Gradle | `fase-02-gradle/` | Concluída: Gradle 8.7, Java 17 e 162 verificações via JUnit |
| 3 — LibGDX | `fase-03-libgdx/` | Concluída: HUD, menu, pausa, áudio e sprites; 44 casos JUnit e janela validados |
| 4 — Interface gráfica | `fase-04-interface-grafica/` | Concluída: animações, movimento visual, HUD SpaceRage; 48 casos JUnit e smoke em 3 resoluções |
| 5 — Servidor e ranking | `fase-05-servidor-ranking/` | Concluída: servidor com 27 testes MockMvc; cliente com 70 casos JUnit; smoke ponta a ponta |
| 6 — Android | `fase-06-android/` | Concluída: 27 testes no servidor, 83 no cliente, smokes desktop, APK testado num celular via USB, com capturas |
| 7 — Evolução livre | `fase-07-evolucao-livre/` | Concluída: 41 testes no servidor, 132 no cliente, 4 smokes com janela, deploy no Render e teste em celular |

As sete fases previstas no prompt mestre estão concluídas. Novas evoluções exigem
plano próprio e aprovação.

## Executar a Fase 7

Com JDK 17, dentro de `fases/fase-07-evolucao-livre/`:

```powershell
cd servidor; .\gradlew.bat test                       # 41 testes
cd ..\cliente; .\gradlew.bat clean build             # 132 testes
.\gradlew.bat :lwjgl3:run "-Dmissaomarte.servidor=https://missao-marte-servidor.onrender.com"
.\gradlew.bat :lwjgl3:zipApresentacao                 # pacote para outro Windows
```

O servidor publicado fica em `https://missao-marte-servidor.onrender.com`.
Detalhes no [README da Fase 7](fases/fase-07-evolucao-livre/README.md) e em
[APRESENTACAO.md](fases/fase-07-evolucao-livre/APRESENTACAO.md).

## Executar a Fase 6

Com JDK 17, dentro de `fases/fase-06-android/`: suba o servidor
(`cd servidor && ./gradlew bootRun`) e, no cliente, rode o desktop
(`./gradlew :lwjgl3:run`) ou instale no Android com o SDK configurado
(`./gradlew :android:installDebug`; no celular USB use antes
`adb reverse tcp:8080 tcp:8080`). Detalhes no
[README da Fase 6](fases/fase-06-android/README.md).

## Executar a Fase 5

Com JDK 17, em dois terminais dentro de `fases/fase-05-servidor-ranking/`:

```bash
cd servidor && ./gradlew test && ./gradlew bootRun --console=plain
```

```bash
cd cliente && ./gradlew clean build && ./gradlew :lwjgl3:run --console=plain
```

Sem login o jogo funciona como convidado (ranking local). Detalhes, API e
limitações no [README da Fase 5](fases/fase-05-servidor-ranking/README.md).

## Executar a Fase 4

Com JDK 17, entre em `fases/fase-04-interface-grafica/` e execute:

```bash
./gradlew build
./gradlew :lwjgl3:run
./gradlew :lwjgl3:smokeDesktop
```

Mesma arquitetura da Fase 3; só `core/presentation` e `core/screen` mudaram.
Detalhes e controles no [README da Fase 4](fases/fase-04-interface-grafica/README.md).

## Executar a Fase 3

Com JDK 17, entre em `fases/fase-03-libgdx/` e execute:

```bash
./gradlew build
./gradlew :lwjgl3:run
```

`core` contém regras e telas compartilhadas; `lwjgl3` contém a inicialização e os
adaptadores desktop. A organização segue o referencial com LibGDX 1.12.1.
As entidades/serviços permanecem sem imports gráficos. O jogo continua por turnos
e usa nave, inimigo, asteroide e fundo copiados dos assets fornecidos.

O console está disponível com `./gradlew :lwjgl3:runConsole --console=plain`.
`./gradlew :lwjgl3:smokeDesktop` executa a verificação gráfica com janela real e
entrada determinística; `./gradlew :lwjgl3:installDist` gera a distribuição.
Os 44 casos JUnit incluem um adaptador das 162 verificações herdadas ajustadas,
além dos testes de coordenação, entrada, regressão e apresentação. As correções
limitam inimigos ao mapa e interrompem o turno ao abortar. A aplicação inicia
no menu; P/ESC abrem a pausa e congelam o cronômetro/música. SPACE é apenas um
controle futuro desativado. Consulte os controles e limites no
[README da Fase 3](fases/fase-03-libgdx/README.md).

## Executar a Fase 2

No Ubuntu amd64 deste ambiente, selecione JDK 17 e entre na pasta da fase:

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
cd evolucao-missao-marte/fases/fase-02-gradle
./gradlew build
./gradlew test --rerun-tasks
./gradlew run --console=plain
```

Em outras máquinas, ajuste o caminho do JDK. O Wrapper seleciona Gradle 8.7;
o Java que o inicia deve ser compatível. `build` compila, testa e empacota;
`run` abre o jogo sem executar automaticamente a suíte.

O relatório em `build/reports/tests/test/index.html` registra um teste JUnit
que executa as 162 verificações preservadas. `installDist` e `distZip` geram
distribuições com scripts de inicialização. Nenhuma biblioteca JUnit entra na
distribuição de produção. A fase também foi validada numa cópia independente.

## Executar a Fase 1

Na raiz do workspace:

```bash
bash evolucao-missao-marte/fases/fase-01-refatoracao/executar.sh compilar
bash evolucao-missao-marte/fases/fase-01-refatoracao/executar.sh testar
bash evolucao-missao-marte/fases/fase-01-refatoracao/executar.sh jogar
```

A compilação passa com `-Xlint:all -Werror`. A suíte executa 162 verificações de
regras, imutabilidade, criação, persistência e console. A versão também foi
compilada e testada numa cópia independente fora do repositório. O ranking é
gerado somente no diretório da fase ao executar pelo script.

As regras e limitações preservadas estão no [README da fase](fases/fase-01-refatoracao/README.md).

## Ambiente verificado na preparação

Em 13/11/2026, o Java disponível é OpenJDK 25.0.5.0.3. O executável `javac` não
está no PATH, mas o compilador funciona com:

```bash
java -m jdk.compiler/com.sun.tools.javac.Main -version
```

As duas bases existentes compilaram sem avisos com `-Xlint:all`. Foram executados
menu, consulta de ranking vazio e saída, com artefatos isolados em `/tmp`.
Não foi realizada uma partida completa nessa preparação, nem encontrados testes
automatizados existentes nessas bases. Relatos de testes nos documentos antigos
não substituem testes executáveis ou uma nova validação.

A Fase 1 permanece em Java de console, sem Gradle ou dependências externas.
Na preparação da Fase 2 foram encontrados Gradle 8.7 e JDK 17 instalados. A Fase 2
foi validada com essas versões, incluindo compilação `--release 17`. O registro
acima descreve o ambiente anterior da preparação; a seleção de versões LibGDX
permanece para a fase correspondente.

## Continuidade

As Fases 1 a 5 e 7 foram aprovadas e implementadas; a Fase 6 foi implementada
pela equipe e registrada depois, na revisão de 24/11/2026. A Fase 7 foi entregue
em etapas (PRs #18 a #22) e publicada numa Release para a apresentação. A Fase 3 recebeu uma
reformulação visual autorizada em 16/11/2026, e a Fase 5 segue o enunciado do
professor (modo convidado mantido). Novas etapas continuam exigindo
planejamento e aprovação, com revisão por pull request antes de integrar na master.
