# Reformulação visual da Fase 3 — 16/11/2026

## Escopo autorizado

O novo pedido do usuário reformula a Fase 3 existente: HUD superior em duas
linhas, ambiente central sem grade dominante, rodapé, menu, instruções, pausa,
áudio e Audiowide. Esta orientação substitui o comportamento anterior de ESC
fechar o desktop e de o relógio incluir a pausa. Não cria a Fase 4 nem um
terceiro módulo. A branch de trabalho é `assets-fase-04`, já preparada para
receber os assets reorganizados pelo usuário.

A inspeção anterior às edições identificou regras em model/service,
EstadoPartida/ResultadoAcao, controlador e renderer existentes, dois builds,
32 casos JUnit e os controles atuais. Os quatro PNGs antigos tinham sido
removidos pelo usuário; o runtime passou a usar os arquivos reais reorganizados.
Foram conferidos imagens, transparência, dimensões, fontes, licenças e duração
/sample rate dos áudios. Nenhuma arte ou música foi gerada/substituída.

## Arquivos e responsabilidades

| Arquivo/classe | Responsabilidade |
|---|---|
| MissaoMarteGame | Navegação, composição e descarte das telas/recursos compartilhados. |
| MenuScreen | Menu inicial e instruções, com botões Scene2D. |
| GameScreen | HUD/ambiente/rodapé, painel de pausa e resultado. |
| HudPartida / HudDados | Exibição e formatação dos dados reais, sem valores da imagem de referência. |
| PartidaRenderer | Coordenadas discretas para pixels, sprites proporcionais e animações. |
| EstiloInterface | Fontes e estilos de painéis/botões ciano e laranja. |
| ControladorPartida | Reutiliza JogoService; entrega cada resultado uma vez e preserva configuração no reinício. |
| EfeitosPartida | Efeitos temporários de impacto/resgate, sem modificar entidades. |
| AudioPartida | Música, pausa/retomada e seleção de efeitos por evento. |
| RecursosJogo | AssetManager e dono das texturas, áudios e fontes geradas uma vez. |
| RelogioPausavel | LongSupplier Java puro que desconta pausas. |
| Lwjgl3Launcher | Injeta o mesmo relógio no serviço e na apresentação; cria janela. |

Os modelos e serviços mantêm o mesmo conteúdo. O domínio continua sem Gdx,
Screen, Texture, SpriteBatch ou referências a sons. Foram mantidos os dois
módulos da arquitetura aprovada. Core depende da API LibGDX por também abrigar
apresentação; não se deve confundir esse artefato com os pacotes de regras.

## HUD e ambiente

A primeira linha é Pontos, Dificuldade, Movimentos e Tempo. A segunda apresenta
vidas com ícones e passageiros embarcados/total. O total é embarcados + restantes,
não a capacidade máxima da nave. O cronômetro é formatado em MM:SS.

A posição do mapa é convertida linearmente para a área central. As coordenadas
lógicas, os turnos, limites e colisões são os mesmos. O tamanho da sprite usa
a menor escala entre eixos para manter proporção. O marcador BASE identifica
(0,0). O desenho não executa regras nem reinterpreta colisões por retângulos.

A fonte Audiowide gera BitmapFonts 18/26/58 uma vez. O centro dos painéis não
usa os textos fixos da folha: NinePatch combina bordas e uma região limpa da
mesma textura; ornamentos laranja são desenhados separadamente. As referências
não são utilizadas como fundo com objetos e números já impressos.

## Pausa e navegação

P ou ESC abrem opções sobre a mesma GameScreen. A coordenação bloqueia entrada,
o relógio congela a leitura e a música pausa. Continuar desconta a duração da
pausa antes de liberar a coordenação, preservando posições, pontos e movimentos.
Também há pausa ao minimizar a janela; a retomada exige continuar a missão.

Reiniciar cria uma partida pelo serviço existente, com piloto/dificuldade/limite
anteriores. Voltar ao menu usa a ação ABORTAR quando a missão ainda está ativa.
Não grava uma vitória nem mantém turnos rodando escondidos. O menu tem o botão
Sair; ESC durante a missão não encerra o processo.

O rodapé mantém WASD — MOVER | SPACE — TIRO | P — PAUSA | ESC — MENU. SPACE
está visualmente desativado e não produz ação. C/Q/R aparecem como controles
complementares para embarque, aborto e reinício após encerramento.
As teclas usam recortes de controles_referencia.png; os textos de ação são
renderizados separadamente. O smoke também pressiona SPACE e confere que
pontuação e movimentos permanecem intactos.

## Eventos, efeitos e áudio

O controlador mantém mensagens para leitura, mas uma fila separada entrega
ResultadosAcao novos uma única vez. Consumir a fila a esvazia. Não se compara
lista/valor para eliminar duplicatas: duas ações iguais continuam sendo fatos
distintos. O GameScreen consome os resultados depois de atualizar a coordenação.

COLISAO/NAVE_DESTRUIDA geram uma explosão local usando os 11 frames originais
explosion_1_01..11, 0,08 segundo por frame, sem repetição. Isso representa o
impacto determinado pelo core; não remove inimigos ou cria regras de combate.
EMBARCOU gera o efeito de resgate por 0,7 segundo. Fim por pontos ou aborto não
inventa explosão. O relógio desses efeitos é visual; terminar uma animação não
chama executar() e apenas remove a instância temporária do efeito.

A origem sonora do impacto é lida da fotografia: coincidência de asteroide
na posição da nave tem a mesma prioridade do domínio; caso contrário, usa-se
o efeito de inimigo. Resgate, vitória, seleção e ação sem sucesso usam os
arquivos fornecidos. Não há áudio de tiros ou escudo em execução.

Música principal: musica_fundo.mp3, repetição, volume 0,22. Efeitos: volume 0,65;
seleção: 0,45. Vitória interrompe a trilha e toca seu efeito; derrota interrompe
a trilha e reproduz derrota.mp3 a 0,55, uma única vez, como Music (7,68 segundos).
Na pausa, a música congela e continua da posição anterior. Não há carga de
texturas, fontes ou sons dentro de render().

## Build e ciclo de vida

Mantidos Java 17, Gradle 8.7, LibGDX 1.12.1 e JUnit 5.10.2. Acrescentados
gdx-freetype e seus natives desktop da mesma versão, para usar a TTF existente.
AssetManager possui texturas/sons/músicas; RecursosJogo possui as BitmapFonts
criadas pelo gerador e a textura auxiliar. Telas possuem seus Stages/batches.

Trocar telas agenda o descarte da anterior para o próximo render, evitando
liberar o Stage durante seu próprio evento de clique. Encerrar o Game também
descarta qualquer tela pendente, para não depender de outro frame ao fechar.

## Validação

- Primeira etapa: os 32 casos existentes continuaram passando.
- Validação final: 44 casos JUnit, zero falhas/erros/skips, incluindo as 162
  verificações internas herdadas. Nenhum teste JUnit existente foi alterado.
- Nove novos testes do core: relógio, pausa, HUD, consumo único, configuração,
  término de efeitos e classificação de áudio.
- Três novos testes de teclado: P, SPACE indisponível e intenção de ESC.
- Smoke gráfico: menu/instruções por cliques reais no Stage, início, música,
  HUD, pausa por teclado, relógio congelado por vários frames, retomada,
  resgate, vitória, comandos após fim, colisões, animação finita, derrota,
  reinício por botão/tecla, retorno ao menu, aborto e Sair. Duas mudanças de
  tamanho, 390 frames, nove capturas e nenhum erro OpenGL.
- O smoke anterior foi adaptado porque agora a aplicação começa no menu e ESC
  abre pausa. Suas verificações de limites, turnos, ranking único e descarte
  foram mantidas e ampliadas, sem enfraquecer expectativas.
- A distribuição passou pelo mesmo cenário fora da pasta do projeto, usando
  fonte, imagens e áudios empacotados. A tarefa runConsole abriu e encerrou pela
  opção 4. Comparação por hash conferiu 731 arquivos anteriores, regras, testes
  e assets preservados. Os assets dentro do JAR são idênticos aos fornecidos.

Os comandos estão no [README](../README.md). O smoke comprova inicialização e
reprodução de áudio pela API do backend; não substitui uma avaliação auditiva
subjetiva da mixagem. Android, teclado físico manual e outras plataformas
não foram validados nesta entrega. Permanecem as limitações anteriores de
mapas pequenos e ranking local, além das funcionalidades futuras de tiro/escudo.

## Referências técnicas

- [Scene2D UI](https://libgdx.com/wiki/graphics/2d/scene2d/scene2d-ui).
- [Gdx FreeType](https://libgdx.com/wiki/extensions/gdx-freetype).
- [Animações](https://libgdx.com/wiki/graphics/2d/2d-animation).
- [Gestão de assets](https://libgdx.com/wiki/managing-your-assets).
- [Música por streaming](https://libgdx.com/wiki/audio/streaming-music).
- [Referencial local](../../../tutorial-api-gradle-libgdx.md), mantido intacto.
