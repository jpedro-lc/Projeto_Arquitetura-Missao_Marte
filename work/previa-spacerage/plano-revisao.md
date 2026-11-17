# Proposta de reformulação SpaceRage — aguardando aprovação

Este documento acompanha a prévia solicitada antes da implementação. Nenhuma
classe, build, teste ou asset do jogo foi modificado nesta preparação.

## Escopo e aplicação do prompt mestre

Proposta: executar a reformulação como **Fase 4 — Interface gráfica**, em
`evolucao-missao-marte/fases/fase-04-interface-grafica/`, partindo da Fase 3
concluída. Isso respeita a separação de entregas do prompt mestre e preserva
a versão anterior. A pasta da Fase 4 e sua branch só serão criadas depois
da aprovação desta proposta. As imagens novas já existentes na pasta da
Fase 3 serão fontes de leitura, sem alterações nos originais.

A prévia fica em work/previa-spacerage; não integra o runtime do jogo.
Números e posições são ilustrativos. Ela não implementa nem substitui regras.
A etapa aprovada modificará uma cópia executável do jogo Java/LibGDX existente,
com suas telas, controles, música, efeitos, pausa, ranking e condições de fim.

## Diagnóstico

- Tecnologia: LibGDX 1.12.1, Scene2D Stage/Table, SpriteBatch, texturas e FreeType.
- Estrutura: core e lwjgl3; presentation é um pacote do core, não outro módulo.
- GameScreen usa FitViewport 1280×800 e limpa a janela com cor escura.
- PartidaRenderer desenha BG.png somente no Actor central, deixando HUD e
  rodapé sobre o clear; a proporção fixa também produz margens laterais.
- HudPartida cria quatro painéis separados e uma linha de vidas sem moldura.
- A base ainda é um quadrado de apresentação na origem (0,0).
- Regras em model/service e contratos Java, sem imports gráficos; preservá-las.
- A última validação disponível tem 44 casos JUnit e 162 checagens internas.
- Controles reais: WASD, C, Q, R, P, ESC. Tiro não está implementado.

## Assets inspecionados

Caminhos relativos a core/assets da Fase 3. Todos são arquivos existentes.

| Elemento | Arquivo | Dimensões |
|---|---|---|
| Fundo | sprites/spacerage_original/BG.png | 700×800 |
| HUD principal | sprites/ui_spacerage/hud/hud_top_background.png | 1442×85 |
| HUD inferior | sprites/ui_spacerage/hud/hud_lower_background.png | 1014×89 |
| Rodapé | sprites/ui_spacerage/footer/footer_background.png | 1259×71 |
| Base | sprites/ui_spacerage/base/base_idle.png | 128×128 |
| Base ativa | sprites/ui_spacerage/base/base_active.png | 128×128 |
| Pouso visual | sprites/ui_spacerage/base/base_landing_01.png | 128×128 |
| Conclusão | sprites/ui_spacerage/base/base_complete.png | 128×128 |
| Passageiro | sprites/ui_spacerage/astronauts/astronaut_idle_01.png | 32×40 |
| Asteroide | sprites/ui_spacerage/asteroids/asteroid_large_01.png | 80×80 |

Também foram conferidos nave/inimigo originais, menu_panel.png, pause_panel.png,
menu_button_normal.png e a fonte fonts/Audiowide-Regular.ttf. HUD, rodapé e
plataformas têm canal alfa variável; os originais serão preservados.

## Alterações propostas

1. **GameScreen/MenuScreen e viewport:** background em uma passagem que cobre
   toda a janela, com recorte proporcional. ScreenViewport com escala de UI
   calculada no resize; Stage e tabelas ocupam a superfície completa. Remover
   dependência do retângulo central para desenhar o fundo. As transparências
   do HUD/rodapé deixam o mesmo cenário aparecer por trás de ambos.
2. **HudPartida:** painel superior contínuo com quatro seções conectadas;
   painel inferior alinhado, com nave para vidas e astronauta para passageiros.
   Valores continuam em labels, a partir de HudDados/EstadoPartida. Nine-slice
   preserva cantos e espessura dos contornos, sem brilho artificial.
3. **PartidaRenderer:** desenhar somente mundo/entidades/efeitos sobre o fundo
   comum; transformar coordenadas lógicas em posições na área entre os painéis.
   A mesma transformação posiciona a origem (0,0), inclusive após resize.
   Manter sprites proporcionais e filtragem nearest para pixel art.
4. **Base:** trocar o quadrado/texto pela plataforma. Idle durante resgates;
   active quando todos estão a bordo e ainda retornando; landing como transição
   visual curta depois do evento de vitória; complete ao terminar a transição.
   Vitória, pontuação e ranking já terão sido determinados pelo core. Não criar
   espera de pouso, novas colisões ou condição adicional para concluir a missão.
   A nave será desenhada separadamente, acima da plataforma.
5. **Rodapé:** usar o novo fundo metálico, integrado às bordas da janela. Mostrar
   WASD, P, ESC e C/Q/R. O pedido lista SPACE, mas também determina que controles
   inexistentes não sejam exibidos; portanto a proposta omite SPACE enquanto
   não existir tiro. Nenhuma mecânica de disparo/escudo será adicionada.
6. **Recursos/estilos:** cadastrar os novos caminhos no AssetManager uma vez,
   usar os painéis/botões do mesmo pacote no menu e na pausa e definir seu
   descarte. Não recriar fontes, texturas ou sons em render.

Arquivos principais na cópia nova: RecursosJogo, EstiloInterface, HudPartida,
GameScreen, MenuScreen, PartidaRenderer e o smoke gráfico. Um pequeno componente
de estado visual da base pode ser extraído se ajudar a testar a transição.
Não é necessária alteração dos modelos, JogoService, colisões ou pontuação.
Os builds apenas recebem o novo nome do projeto, mantendo os dois módulos.

## Versões e referencial

Manter Java 17, Gradle 8.7, Groovy, LibGDX/FreeType 1.12.1 e JUnit 5.10.2.
Referencial: seções de sprites/texturas, HUD, áudio e colisões da Parte 3;
GameScreen e composição da Parte 6; controles/execução da Parte 9.
O exemplo contínuo do tutorial não substitui as regras discretas existentes.
Animação e viewport podem ser complementados com a documentação oficial,
registrando as fontes no tutorial didático da fase. O referencial é somente leitura.

## Riscos e critérios de validação

- Sem faixas escuras por letterbox ou regiões sem background nas resoluções
  1366×768, 1600×900 e 1920×1080; recorte proporcional sem esticar a textura.
- Base (0,0) central na área jogável, nave separada e entidades dentro dos limites
  visuais, considerando tamanho de sprite e espaço ocupado por HUD/rodapé.
- HUD sem textos fixos vindos das imagens; verificações de valores reais.
- Transição de pouso não pode atrasar vitória, salvar ranking novamente ou
  executar movimentos. Efeitos existentes continuam consumindo eventos uma vez.
- Pausa/cronômetro/áudio, menus, console e comandos existentes preservados.
- Reexecutar as 44 verificações JUnit existentes, acrescentar testes da base e
  transformação de coordenadas e ampliar smoke/capturas para as três resoluções.
- Build, execução desktop, distribuição fora do projeto e descarte de recursos.
- Tutorial com os 12 itens do prompt mestre e README/comandos verificados;
  commits por etapa e revisão por pull request, sem integrar direto na master.

## Aprovação

Primeiro avaliar o layout, os estados da plataforma e as três resoluções na
prévia. Só depois da aprovação explícita criar a fase e implementar este plano.
