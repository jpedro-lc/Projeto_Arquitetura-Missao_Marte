# Missão Marte — Fase 3

Interface LibGDX com HUD superior, ambiente espacial central, rodapé, menu,
instruções, pausa e áudio. Mantém os módulos **core e lwjgl3**, Java 17,
Gradle Wrapper 8.7 e LibGDX 1.12.1.

## Executar

Na pasta desta fase, com JDK 17:

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew build
./gradlew :lwjgl3:run
```

Em outra máquina, ajuste o caminho do JDK. O menu abre antes da partida.
Clique em **Iniciar jogo**, **Instruções** ou **Sair**. O início conserva os
padrões existentes: piloto anônimo, dificuldade média e limite 5 (11×11).
Reiniciar conserva piloto, dificuldade e limite da sessão.

| Controle | Comportamento |
|---|---|
| WASD | Um movimento discreto por pressão |
| C | Embarcar o passageiro na posição da nave |
| Q | Abortar imediatamente |
| P | Pausar/continuar |
| ESC | Abrir/fechar opções de pausa durante a partida |
| R | Reiniciar uma partida encerrada |
| SPACE | Indisponível; aparece apenas como função futura |

O painel de pausa permite continuar, reiniciar ou voltar ao menu. Continuar
preserva a mesma partida e desconta o tempo pausado. Voltar ao menu aborta
uma missão em andamento, sem registrar vitória. Para fechar a aplicação,
use Sair no menu ou o botão de fechamento da janela.

## Interface e áudio

O HUD mostra Pontos, Dificuldade, Movimentos e Tempo (MM:SS), seguidos por vidas
e passageiros embarcados/total da missão. Não existe indicador de escudo.
A grade deixou de ser desenhada: as coordenadas lógicas continuam discretas,
convertidas para posições dentro da área central. Imagens mantêm a proporção.

As telas usam Audiowide, bordas ciano e detalhes laranja recortados dos assets.
A música toca na partida, pausa com a interface e para na vitória/derrota/aborto.
Impactos, resgate, vitória e seleção usam efeitos curtos. Game Over é reproduzido
por streaming, por ser um áudio de 7,68 segundos. Nenhum recurso é criado por frame.

## Assets atuais

Os arquivos reorganizados pelo usuário estão em `core/assets/`:

| Recurso | Origem relativa a core/assets |
|---|---|
| Nave/inimigo | `sprites/spacerage_original/Player/player_b_m.png`, `Enemies/enemy_1_r_m.png` sob o mesmo diretório SpaceRage |
| Fundo | `sprites/spacerage_original/BG.png` |
| Asteroide | `sprites/asteroides/asteroide_cinza_03.png` |
| Passageiros | `sprites/passageiros/passageiro_{branco_azul,laranja,verde}_parado.png` |
| Resgate | `sprites/passageiros/passageiro_branco_azul_resgatado.png` |
| Painéis | `sprites/hud/painel_area_jogo_referencia.png` — somente regiões de borda e fundo limpo |
| Teclas do rodapé | `sprites/hud/controles_referencia.png` — recortes das teclas, com ações desenhadas dinamicamente |
| Explosão | `sprites/spacerage_original/Explosions/explosion_1_01.png` a `explosion_1_11.png` |
| Propulsor | `sprites/spacerage_original/FX/exhaust_01.png` a `exhaust_05.png` |
| Fonte | `fonts/Audiowide-Regular.ttf` |
| Música principal | `sounds/musicas/musica_fundo.mp3` |
| Efeitos | `sounds/efeitos/colisao_asteroide.ogg`, `colisao_inimigo.ogg`, `resgate.ogg`, `vitoria.ogg`, `menu_selecionar.ogg`, `erro_acao.ogg`, `derrota.mp3` |

`referencias/` contém composições visuais; não é desenhado como tela pronta.
Áudios de tiro/escudo e a trilha alternativa permanecem disponíveis para uma
etapa futura e não são carregados pelo jogo. Licenças fornecidas acompanham
os assets em `licenses/`. Os antigos caminhos `images/*.png` foram substituídos
pelos caminhos reorganizados; não é necessário restaurar os arquivos antigos.

## Arquitetura

- `core/model`, `core/service` e contratos: regras Java sem imports LibGDX.
- `core/presentation` e `core/screen`: HUD, navegação, áudio, efeitos e entrada.
- `lwjgl3`: janela, teclado, composição do relógio/repositório e console.

O artefato core inclui LibGDX por conter telas, mas suas regras não dependem
dessas classes. Não foi criado terceiro módulo. O relógio pausável é Java puro
injetado no contrato já existente do JogoService; as classes de regras não
foram modificadas nesta reformulação.

## Validar e distribuir

```bash
./gradlew clean build
./gradlew :lwjgl3:smokeDesktop
./gradlew :lwjgl3:installDist
./gradlew :lwjgl3:runConsole --console=plain
```

O build comum não exige display. O smoke exige janela/OpenGL e testa cliques
nos botões reais do Stage, teclas injetadas, HUD, pausa/cronômetro, áudio,
colisões, efeitos finitos, vitória/derrota, reinício e redimensionamento.
As nove capturas ficam em `lwjgl3/build/reports/smoke/`.

Em 16/11/2026, passaram **44 casos JUnit**, incluindo um adaptador que executa
as **162 verificações herdadas**, sem alterar seus testes existentes. As nove
checagens adicionais do core e três do teclado verificam a nova apresentação.
O smoke passou com 390 frames e áudio reproduzido pela API do backend desktop.
A inspeção visual conferiu menu, instruções, HUD, pausa e diferentes proporções.
O console abriu/encerrou pela opção 4. A distribuição também passou pelo smoke
fora da pasta do projeto, com fonte, imagens e áudios lidos dos JARs.

A distribuição completa fica em `lwjgl3/build/install/lwjgl3/`, com script
`bin/lwjgl3`, JARs, imagens, fonte, bibliotecas nativas e sons. Requer Java 17.
O console mantém configuração completa e ranking local. Nas tarefas Gradle,
o ranking é gravado na raiz desta fase; na distribuição, no diretório de trabalho.

## Documentação e limites

- [Reformulação atual](docs/reformulacao-interface.md): decisões, arquivos e validação.
- [Tutorial da introdução ao framework](docs/fase-03-tutorial.md): registro didático da migração inicial.
- [Correções de limites/encerramento](docs/correcoes-fase-03.md): regressões anteriores.
- [Plano original](../../docs/plano-fase-03.md) e [referencial](../../tutorial-api-gradle-libgdx.md).

Tiro, escudo, ranking global e Android permanecem fora desta entrega. A fábrica
atual gera professor/engenheiro; existe representação de astronauta quando o
estado contiver esse tipo. Geração em mapas pequenos e formato legado do ranking
continuam como antes. O volume/loop musical foi exercitado pelo backend; ajustes
artísticos de mixagem e cortes perfeitos da faixa dependem de avaliação auditiva.
