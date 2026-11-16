# Fase 3 — Introdução ao LibGDX

Adaptação da Fase 2 ao ciclo de vida LibGDX, conforme o referencial: **core +
lwjgl3**, Java 17, Gradle 8.7, Groovy e LibGDX **1.12.1**. O jogo permanece por
turnos e utiliza as imagens fornecidas pelo usuário.

## Organização

- `core/src/main/java/br/unifor/missaomarte/`: modelos, serviços, contratos,
  `MissaoMarteGame`, `screen/GameScreen` e apresentação compartilhada.
- `core/assets/images/`: nave, inimigo, asteroide e fundo.
- `lwjgl3/`: launcher desktop, teclado, persistência em arquivo e console legado.
- [Tutorial da fase](docs/fase-03-tutorial.md): transformação, conceitos e exercícios.
- [Correções posteriores](docs/correcoes-fase-03.md): limites dos inimigos e encerramento imediato.
- [Plano aprovado](../../docs/plano-fase-03.md): escopo e critérios de validação.
- [Referencial](../../tutorial-api-gradle-libgdx.md): somente leitura.

O módulo core depende de LibGDX porque contém telas. Seus pacotes `model`,
`service` e `repository` continuam Java puro, sem importar a interface. Não há
terceiro módulo `presentation`: esse nome identifica pacotes dentro do core.
Telas comuns não importam LWJGL, AWT ou Android. O futuro backend Android ainda
precisará ser implementado e validado.

## Executar

A partir desta pasta, usando JDK 17:

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew build
./gradlew :lwjgl3:run
```

Em outra máquina, ajuste o caminho do JDK. O Wrapper baixa Gradle 8.7 e as
dependências declaradas. Não é necessário instalar LibGDX separadamente.
`build` compila, testa e empacota; `:lwjgl3:run` abre a janela e não executa
automaticamente os testes.

| Tecla | Ação |
|---|---|
| W / S / A / D | Cima / baixo / esquerda / direita |
| C | Embarcar passageiro na posição da nave |
| Q | Abortar a missão |
| R | Reiniciar depois do encerramento |
| Esc | Fechar a aplicação |

Uma pressão produz uma intenção; segurar a tecla não deve executar um turno por
frame. Teclas simultâneas seguem a prioridade definida pelo adaptador de teclado,
com Esc primeiro. A janela inicia com os padrões do console: piloto anônimo,
dificuldade média, limite 5, mapa 11x11. O tempo continua real, como na Fase 2.

```bash
./gradlew :lwjgl3:runConsole --console=plain
```

O console preserva configuração de piloto/dificuldade/mapa e o menu de ranking.
O ranking continua local e no formato legado delimitado por `|`, apesar da
extensão `.json`. Nas tarefas Gradle, é gravado na raiz desta fase, nunca nos
assets. Login e ranking global pertencem à Fase 5.

## Testar e distribuir

```bash
./gradlew :core:test :lwjgl3:test
./gradlew :lwjgl3:smokeDesktop
./gradlew :lwjgl3:installDist
```

`smokeDesktop` exige display/OpenGL e abre uma janela por alguns segundos. Usa
entrada determinística injetada para verificar frames sem ações, movimento,
embarque, vitória, reinício, aborto, redimensionamento e encerramento. Salva
três capturas em `lwjgl3/build/reports/smoke/`. Não substitui um teste manual de
teclado físico ou testes Android. `build` não depende dessa tarefa gráfica.

A distribuição fica em `lwjgl3/build/install/lwjgl3/`. Seu script `bin/lwjgl3`
abre o jogo com os JARs e assets empacotados; requer Java 17 instalado e não
inclui uma JRE. Também são gerados ZIP/TAR em `lwjgl3/build/distributions/`.
Ao executar a distribuição, o ranking é relativo ao diretório de trabalho
escolhido pelo usuário. O JAR isolado não é uma distribuição com dependências.

## Origem das imagens

Os arquivos foram copiados sem modificar pixels. Os caminhos de origem abaixo
são relativos a `../fase-02-gradle/assets/`; o destino é relativo a `core/assets/`.

| Destino | Origem |
|---|---|
| `images/nave.png` | `SpaceRage/SpaceRage/Player/player_b_m.png` |
| `images/inimigo.png` | `SpaceRage/SpaceRage/Enemies/enemy_1_r_m.png` |
| `images/asteroide.png` | `Stylized Space Shooter Asset Kit/Asteroid/Asteroid_1.png` |
| `images/fundo.png` | `Stylized Space Shooter Asset Kit/Background/background.png` |

O runtime não acessa a pasta da Fase 2. Professor/engenheiro/astronauta e base
usam identificadores de texto; animações, sons, menus completos e HUD elaborado
ficam para a Fase 4. As imagens de HUD com valores fixos e o XML da spritesheet
não são utilizados como HUD funcional ou como atlas nativo.

## Validação realizada

Em 15/11/2026, com JDK 17:

- `clean build`: 32 casos JUnit aprovados, sendo 5 de coordenação, 17 de regressão,
  9 de entrada e 1 adaptador que executa **162 verificações herdadas**, ajustadas
  às correções de limites/aborto; sem falhas ou erros.
- `installDist`: distribuição gerada com as quatro imagens dentro do JAR.
- `smokeDesktop`: janela/OpenGL reais, 210 frames, três capturas inspecionadas,
  vitória e gravação única, duas mudanças de resolução e descarte sem erro OpenGL.
  Após as correções, também verifica inimigos dentro do mapa e comandos de
  movimento ignorados após vitória e aborto.
- Cópia independente em `/tmp`, com espaços no caminho: build offline e testes
  aprovados; apenas core e lwjgl3 listados como subprojetos.
- Distribuição copiada para fora do projeto: verificação gráfica aprovada usando
  os JARs empacotados em um diretório sem assets locais.
- `runConsole`: menu aberto e encerrado pela opção 4 usando a entrada padrão.
- Na entrega inicial, regras/serviços/contratos diferiam da Fase 2 apenas pelo
  namespace. As correções posteriores alteram os limites dos inimigos e fazem
  o aborto interromper o turno imediatamente.
- Fases anteriores e assets originais conferidos por hash, sem alterações.

As correções mantêm os inimigos em `[-limite, limite]` em ambos os eixos. Ao
abortar, não há novo movimento de inimigos nem colisão. Depois de vitória,
derrota ou aborto, comandos não alteram movimentos, pontos, posições ou tempo.
O movimento que causa o encerramento ainda conta uma vez; comandos seguintes
são ignorados. Tentativas de mover a nave contra a borda continuam consumindo
ponto e contando movimento, como na regra anterior.

Permanecem as limitações herdadas de geração em mapas pequenos e do ranking em
arquivo. O jogo continua por turnos.
