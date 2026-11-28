package br.unifor.missaomarte.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import br.unifor.missaomarte.MissaoMarteGame;
import br.unifor.missaomarte.model.Acao;
import br.unifor.missaomarte.model.EstadoPartida.Situacao;
import br.unifor.missaomarte.presentation.AnimadorPosicoes;
import br.unifor.missaomarte.presentation.ControladorPartida;
import br.unifor.missaomarte.presentation.EfeitosPartida;
import br.unifor.missaomarte.presentation.EstadoVisualBase;
import br.unifor.missaomarte.presentation.EstiloInterface;
import br.unifor.missaomarte.presentation.HudPartida;
import br.unifor.missaomarte.presentation.assets.RecursosJogo;
import br.unifor.missaomarte.presentation.rendering.PartidaRenderer;
import br.unifor.missaomarte.remoto.PartidaConcluida;
import br.unifor.missaomarte.model.ModoJogo;
import java.util.EnumMap;
import java.util.Map;

/** Fundo em tela cheia, HUD, ambiente animado e rodapé, com pausa sobre a mesma partida. */
public final class GameScreen extends ScreenAdapter {
    private final MissaoMarteGame game;
    private final ControladorPartida controlador;
    // ExtendViewport amplia o mundo virtual em vez de criar faixas pretas (letterbox).
    private final Stage stage = new Stage(new ExtendViewport(PartidaRenderer.LARGURA, PartidaRenderer.ALTURA));
    private final EfeitosPartida efeitos = new EfeitosPartida();
    private final AnimadorPosicoes posicoes = new AnimadorPosicoes();
    private final EstadoVisualBase base = new EstadoVisualBase();
    private EstadoVisualBase.Fase faseBase = EstadoVisualBase.Fase.OCIOSA;
    private final Table overlay = new Table();
    private final Table resultado = new Table();
    private final Label tituloResultado;
    private final Label statusEnvio;
    private final TextButton reiniciar;
    private final TextButton voltar;
    private final TextButton tentarDeNovo;
    private final Table botoesResultado = new Table();
    /** Envio ao servidor (modo logado): uma vez por partida encerrada em vitória ou derrota. */
    public enum Envio { NENHUM, ENVIANDO, ENVIADO, FALHOU }
    private Envio envio = Envio.NENHUM;
    private PartidaConcluida pendente;
    private boolean ativa = true;
    private final HudPartida hud;
    private boolean pausado;
    private float tempoVisual;
    private int geracao;
    /** Botões de direção: no tempo real, segurar mantém a nave andando. */
    private final Map<Acao, Button> direcoes = new EnumMap<>(Acao.class);
    private final Label contagem;
    /** Modo Infinito: "SETOR n" acima do mapa durante toda a corrida. */
    private final Label setor;
    private float mostrarLargada;

    public GameScreen(MissaoMarteGame game, ControladorPartida controlador, RecursosJogo recursos) {
        this.game = game;
        this.controlador = controlador;
        geracao = controlador.geracao();
        posicoes.fixar(controlador.estado());
        var estilo = new EstiloInterface(recursos);
        var renderer = new PartidaRenderer(recursos);
        stage.addActor(new Actor() {
            @Override public void draw(Batch batch, float parentAlpha) {
                var mundo = getStage().getViewport();
                PartidaRenderer.fundo(batch, recursos, 0, 0, mundo.getWorldWidth(), mundo.getWorldHeight());
            }
        });
        var raiz = new Table();
        raiz.setFillParent(true);
        raiz.pad(8, 12, 8, 12);
        hud = new HudPartida(recursos);
        hud.atualizar(controlador.estado());
        raiz.add(hud).growX().row();
        Actor ambiente = new Actor() {
            @Override public void draw(Batch batch, float parentAlpha) {
                renderer.desenhar(batch, controlador.estado(), efeitos, posicoes, faseBase,
                        getX(), getY(), getWidth(), getHeight(), tempoVisual);
            }
        };
        ambiente.setName("ambiente-mapa");
        ambiente.setTouchable(Touchable.enabled);
        ambiente.addListener(new InputListener() {
            private float inicioX;
            private float inicioY;
            @Override public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                inicioX = x;
                inicioY = y;
                return true;
            }
            @Override public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                game.toque().deslizar(x - inicioX, y - inicioY);
            }
        });
        raiz.add(ambiente).grow().pad(4, 0, 6, 0).row();
        var rodape = new Table();
        rodape.setBackground(recursos.painelRodape());
        rodape.pad(6, 20, 6, 20);
        // Controles interativos por toque: enfileiram intenções em ToqueEntradaJogo.
        boolean celular = game.controlesNaTela();
        if (!celular) {
            // Desktop: a linha de botões das fases anteriores (o teclado continua sendo o principal).
            rodape.add(botaoDirecao(estilo.botao("ESQ", () -> { }), "TOQUE_ESQUERDA", Acao.ESQUERDA)).size(96, 44).padRight(6);
            rodape.add(botaoDirecao(estilo.botao("CIMA", () -> { }), "TOQUE_CIMA", Acao.CIMA)).size(104, 44).padRight(6);
            rodape.add(botaoDirecao(estilo.botao("BAIXO", () -> { }), "TOQUE_BAIXO", Acao.BAIXO)).size(112, 44).padRight(6);
            rodape.add(botaoDirecao(estilo.botao("DIR", () -> { }), "TOQUE_DIREITA", Acao.DIREITA)).size(96, 44).padRight(18);
            rodape.add(botaoToque(estilo, "EMBARCAR (C)", "TOQUE_EMBARCAR", () -> game.toque().acionar(Acao.EMBARCAR))).size(210, 44).padRight(8);
        }
        rodape.add(botaoToque(estilo, "ABORTAR (Q)", "TOQUE_ABORTAR", () -> game.toque().acionar(Acao.ABORTAR))).size(196, 44).padRight(18);
        rodape.add(botaoToque(estilo, "PAUSA (P)", "TOQUE_PAUSA", () -> game.toque().solicitarPausa())).size(164, 44).padRight(8);
        rodape.add(botaoToque(estilo, "MENU", "TOQUE_MENU", () -> { game.audio().selecionar(); game.mostrarMenu(); })).size(130, 44);
        raiz.add(rodape).growX().height(60);
        stage.addActor(raiz);
        if (celular) stage.addActor(controlesSobreOMapa(recursos, estilo));

        contagem = estilo.valor("");
        contagem.setName("contagem-largada");
        contagem.setFontScale(3f);
        contagem.setAlignment(Align.center);
        contagem.setTouchable(Touchable.disabled);
        var camadaContagem = new Table();
        camadaContagem.setFillParent(true);
        camadaContagem.setTouchable(Touchable.disabled);
        // Terço de cima do mapa: a nave larga do centro e não fica escondida atrás do número.
        setor = estilo.valor("");
        setor.setName("setor-infinito");
        setor.setAlignment(Align.center);
        camadaContagem.add(setor).expandX().top().padTop(130).row();
        camadaContagem.add(contagem).expand().top().padTop(4);
        stage.addActor(camadaContagem);

        resultado.setBackground(recursos.painelPausa());
        resultado.setSize(860, 172);
        resultado.pad(18, 30, 18, 30);
        tituloResultado = estilo.valor("");
        tituloResultado.setAlignment(Align.center);
        resultado.add(tituloResultado).colspan(3).growX().padBottom(4).row();
        statusEnvio = estilo.texto("");
        statusEnvio.setName("status-envio");
        statusEnvio.setAlignment(Align.center);
        resultado.add(statusEnvio).colspan(3).growX().padBottom(10).row();
        reiniciar = estilo.botao("REINICIAR", () -> { game.audio().selecionar(); game.iniciarPartida(); });
        voltar = estilo.botao("VOLTAR AO MENU", () -> { game.audio().selecionar(); game.mostrarMenu(); });
        tentarDeNovo = estilo.botao("TENTAR DE NOVO", () -> { game.audio().selecionar(); enviar(); });
        resultado.add(botoesResultado).colspan(3);
        mostrarTentarDeNovo(false);
        resultado.setVisible(false);
        stage.addActor(resultado);

        overlay.setFillParent(true);
        overlay.setBackground(new TextureRegionDrawable(new TextureRegion(recursos.pixel())).tint(new Color(0, 0.015f, 0.035f, 0.86f)));
        var painel = new Table();
        painel.setBackground(recursos.painelPausa());
        painel.pad(40, 70, 40, 70);
        painel.add(estilo.valor("MISSÃO EM PAUSA")).padBottom(16).row();
        painel.add(estilo.texto("A partida e o cronômetro estão congelados.")).padBottom(32).row();
        painel.add(estilo.botao("CONTINUAR", () -> { game.audio().selecionar(); game.continuarPartida(); })).size(360, 66).padBottom(12).row();
        painel.add(estilo.botao("REINICIAR PARTIDA", () -> { game.audio().selecionar(); game.iniciarPartida(); })).size(360, 66).padBottom(12).row();
        painel.add(estilo.botao("VOLTAR AO MENU", () -> { game.audio().selecionar(); game.mostrarMenu(); })).size(360, 66).padBottom(20).row();
        painel.add(estilo.texto("Voltar ao menu encerra a missão atual."));
        overlay.add(painel);
        overlay.setVisible(false);
        stage.addActor(overlay);
    }

    @Override public void show() { controlador.retomar(); Gdx.input.setInputProcessor(stage); }
    @Override public void render(float delta) {
        if (pausado) {
            if (game.entrada().pausaSolicitada() || game.entrada().ler().sair()) game.continuarPartida();
        } else {
            if (envio == Envio.ENVIANDO || envio == Envio.FALHOU) {
                // Partida já encerrada: R tenta reenviar em vez de reiniciar e perder o resultado.
                if (game.entrada().ler().reiniciar() && envio == Envio.FALHOU) enviar();
            } else {
                if (controlador.modo() != ModoJogo.CLASSICO) game.toque().manter(direcaoSegurada());
                if (game.entrada().pausaSolicitada() || controlador.atualizar(delta)) game.pausarPartida();
            }
            if (!pausado) {
                boolean geracaoReiniciada = geracao != controlador.geracao();
                if (geracaoReiniciada) {
                    efeitos.limpar();
                    hud.reiniciar();
                    game.audio().iniciar();
                    geracao = controlador.geracao();
                    envio = Envio.NENHUM;
                    pendente = null;
                    statusEnvio.setText("");
                    mostrarTentarDeNovo(false);
                }
                if (geracaoReiniciada) posicoes.fixar(controlador.estado());
                for (var novo : controlador.consumirResultados()) {
                    efeitos.receber(novo);
                    game.audio().processar(novo);
                    aoEncerrar(novo.estado());
                }
                posicoes.acompanhar(controlador.estado());
                posicoes.atualizar(delta);
                efeitos.atualizar(delta);
                faseBase = base.atualizar(controlador.estado(), delta);
                tempoVisual += delta;
            }
        }
        hud.atualizar(controlador.estado());
        atualizarContagem(delta);
        var situacao = controlador.estado().situacao();
        resultado.setVisible(!pausado && situacao != Situacao.EM_ANDAMENTO);
        tituloResultado.setText(switch (situacao) {
            case VITORIA -> "RESGATE CONCLUÍDO";
            case DERROTA -> controlador.modo() == ModoJogo.INFINITO ? "FIM DA CORRIDA — SETOR " + controlador.setor() : "FIM DE MISSÃO";
            case ABORTADA -> "MISSÃO ABORTADA";
            default -> "";
        });
        ScreenUtils.clear(0.005f, 0.015f, 0.035f, 1);
        stage.act(Math.min(delta, 0.1f));
        stage.getViewport().apply();
        stage.draw();
    }

    public boolean pausado() { return pausado; }

    private void aoEncerrar(br.unifor.missaomarte.model.EstadoPartida estado) {
        if (estado.situacao() == Situacao.EM_ANDAMENTO || envio != Envio.NENHUM) return;
        var concluida = PartidaConcluida.de(estado, controlador.modo(), controlador.setor());
        if (concluida.isEmpty()) return;
        if (!game.sessao().logado()) {
            if (controlador.modo() == ModoJogo.INFINITO) informar("Modo convidado: corrida salva no ranking local.", EstiloInterface.CIANO);
            else if (estado.situacao() == Situacao.VITORIA) informar("Modo convidado: vitória salva no ranking local.", EstiloInterface.CIANO);
            return;
        }
        pendente = concluida.get();
        enviar();
    }

    private void enviar() {
        if (pendente == null || !game.sessao().logado()) return;
        envio = Envio.ENVIANDO;
        mostrarTentarDeNovo(false);
        habilitar(false);
        informar("Enviando resultado ao servidor...", EstiloInterface.CIANO);
        var partida = pendente;
        game.servidor().enviarPartida(game.sessao().sessao(), partida, resposta -> {
            if (!ativa) return;
            habilitar(true);
            if (resposta.ok()) {
                envio = Envio.ENVIADO;
                informar("Resultado enviado.", EstiloInterface.CIANO);
                // No Infinito toda corrida conta para o ranking (lá não há vitória).
                if (partida.resultado() == PartidaConcluida.Resultado.VITORIA || partida.modo() == ModoJogo.INFINITO) {
                    posicaoNoRanking(partida);
                }
            } else if (resposta.naoAutorizado()) {
                envio = Envio.ENVIADO;
                game.sessaoExpirada();
                informar(resposta.erro() + " O resultado não foi registrado.", EstiloInterface.LARANJA);
            } else {
                envio = Envio.FALHOU;
                mostrarTentarDeNovo(true);
                informar("Falha ao enviar: " + resposta.erro() + " R tenta de novo.", EstiloInterface.LARANJA);
            }
        });
    }

    private void posicaoNoRanking(PartidaConcluida partida) {
        var sessao = game.sessao().sessao();
        game.servidor().carregarRanking(sessao, partida.modo(), partida.dificuldade(), resposta -> {
            if (!ativa || !resposta.ok()) return;
            var linhas = resposta.valor();
            for (var linha : linhas) {
                if (linha.nome().equalsIgnoreCase(sessao.nome())) {
                    informar("Resultado enviado. Você está em #" + linha.posicao() + " do Top 10!", EstiloInterface.CIANO);
                    return;
                }
            }
            informar("Resultado enviado. Você ainda não está no Top 10.", EstiloInterface.CIANO);
        });
    }

    /** Reconstrói a linha: um botão invisível ainda ocuparia espaço na Table. */
    private void mostrarTentarDeNovo(boolean mostrar) {
        botoesResultado.clearChildren();
        botoesResultado.add(reiniciar).size(240, 48).padRight(12);
        botoesResultado.add(voltar).size(240, 48);
        if (mostrar) botoesResultado.add(tentarDeNovo).size(240, 48).padLeft(12);
    }

    private void habilitar(boolean sim) {
        reiniciar.setDisabled(!sim);
        voltar.setDisabled(!sim);
    }

    private void informar(String texto, Color cor) {
        statusEnvio.setText(texto);
        statusEnvio.setColor(cor);
    }

    public Envio envio() { return envio; }

    /** "3", "2", "1" e "VAI!" sobre o mapa; some sozinho depois da largada. */
    private void atualizarContagem(float delta) {
        setor.setText(controlador.modo() == ModoJogo.INFINITO ? "SETOR " + controlador.setor() : "");
        float restante = controlador.contagemRestante();
        boolean correndo = controlador.estado().situacao() == Situacao.EM_ANDAMENTO;
        if (restante > 0 && correndo) {
            contagem.setText(String.valueOf((int) Math.ceil(restante)));
            mostrarLargada = controlador.modo() != ModoJogo.CLASSICO ? 0.6f : 0f;
        } else if (mostrarLargada > 0 && correndo) {
            contagem.setText("VAI!");
            if (!pausado) mostrarLargada -= delta;
        } else {
            contagem.setText("");
        }
    }

    private Acao direcaoSegurada() {
        for (var direcao : direcoes.entrySet()) {
            if (direcao.getValue().isPressed()) return direcao.getKey();
        }
        return Acao.VAZIA;
    }

    /** Clássico: um passo por clique. Tempo real: o toque já move e segurar continua andando. */
    /**
     * Celular: direcional em cruz à esquerda e EMBARCAR à direita, por cima do mapa e translúcidos,
     * para o mapa continuar do tamanho da tela e os elementos aparecerem por trás. Toques fora dos
     * botões passam para o mapa (swipe).
     */
    private Table controlesSobreOMapa(RecursosJogo recursos, EstiloInterface estilo) {
        var direcional = new Table();
        direcional.add();
        direcional.add(botaoDirecao(seta(recursos, Acao.CIMA), "TOQUE_CIMA", Acao.CIMA)).size(TAMANHO_SETA);
        direcional.add().row();
        direcional.add(botaoDirecao(seta(recursos, Acao.ESQUERDA), "TOQUE_ESQUERDA", Acao.ESQUERDA)).size(TAMANHO_SETA);
        direcional.add().size(TAMANHO_SETA);
        direcional.add(botaoDirecao(seta(recursos, Acao.DIREITA), "TOQUE_DIREITA", Acao.DIREITA)).size(TAMANHO_SETA).row();
        direcional.add();
        direcional.add(botaoDirecao(seta(recursos, Acao.BAIXO), "TOQUE_BAIXO", Acao.BAIXO)).size(TAMANHO_SETA);
        direcional.add();
        var embarcar = botaoToque(estilo, "EMBARCAR", "TOQUE_EMBARCAR", () -> game.toque().acionar(Acao.EMBARCAR));
        var camada = new Table();
        camada.setName("controles-na-tela");
        camada.setFillParent(true);
        camada.setTouchable(Touchable.childrenOnly);
        // Translúcido: a cor da Table multiplica o alfa de tudo o que está dentro dela.
        camada.setColor(1, 1, 1, TRANSPARENCIA_CONTROLES);
        camada.add(direcional).expand().bottom().left().padLeft(28).padBottom(84);
        camada.add(embarcar).size(200, 120).expand().bottom().right().padRight(36).padBottom(110);
        return camada;
    }

    static final float TAMANHO_SETA = 96;
    static final float TRANSPARENCIA_CONTROLES = 0.5f;

    private static Button seta(RecursosJogo recursos, Acao direcao) {
        var estiloSeta = new ImageButton.ImageButtonStyle();
        estiloSeta.up = recursos.botao(RecursosJogo.BOTAO_NORMAL);
        estiloSeta.down = recursos.botao(RecursosJogo.BOTAO_PRESSIONADO);
        estiloSeta.imageUp = new TextureRegionDrawable(new TextureRegion(recursos.seta(direcao)));
        var botao = new ImageButton(estiloSeta);
        botao.getImageCell().size(48);
        return botao;
    }

    private Button botaoDirecao(Button botao, String nomeAtor, Acao direcao) {
        botao.setName(nomeAtor);
        botao.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (controlador.modo() == ModoJogo.CLASSICO) game.toque().acionar(direcao);
            }
        });
        botao.addListener(new InputListener() {
            @Override public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                // Garante o passo mesmo se o dedo sair antes do próximo quadro.
                if (controlador.modo() != ModoJogo.CLASSICO) game.toque().acionar(direcao);
                return false;
            }
        });
        direcoes.put(direcao, botao);
        return botao;
    }

    private static TextButton botaoToque(EstiloInterface estilo, String rotulo, String nomeAtor, Runnable acao) {
        var botao = estilo.botao(rotulo, acao);
        botao.setName(nomeAtor);
        return botao;
    }
    public void exibirPausa(boolean mostrar) { pausado = mostrar; overlay.setVisible(mostrar); }
    public Stage stage() { return stage; }
    public int efeitosAtivos() { return efeitos.ativos().size(); }
    @Override public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        resultado.setPosition((stage.getViewport().getWorldWidth() - resultado.getWidth()) / 2, 96);
    }
    @Override public void pause() { game.pausarPartida(); }
    @Override public void hide() {
        ativa = false;
        if (Gdx.input.getInputProcessor() == stage) Gdx.input.setInputProcessor(null);
    }
    @Override public void dispose() { stage.dispose(); }
}
