package br.unifor.missaomarte.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import br.unifor.missaomarte.MissaoMarteGame;
import br.unifor.missaomarte.model.EstadoPartida.Situacao;
import br.unifor.missaomarte.presentation.AnimadorPosicoes;
import br.unifor.missaomarte.presentation.ControladorPartida;
import br.unifor.missaomarte.presentation.EfeitosPartida;
import br.unifor.missaomarte.presentation.EstadoVisualBase;
import br.unifor.missaomarte.presentation.EstiloInterface;
import br.unifor.missaomarte.presentation.HudPartida;
import br.unifor.missaomarte.presentation.assets.RecursosJogo;
import br.unifor.missaomarte.presentation.rendering.PartidaRenderer;

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
    private final HudPartida hud;
    private boolean pausado;
    private float tempoVisual;
    private int geracao;

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
        raiz.add(ambiente).grow().pad(4, 0, 6, 0).row();
        var rodape = new Table();
        rodape.setBackground(recursos.painelRodape());
        rodape.pad(8, 48, 8, 48);
        // Só controles existentes: SPACE/tiro não é exibido enquanto não houver a mecânica.
        rodape.add(controle(recursos, 45, 278, "MOVER")).expandX();
        rodape.add(controle(recursos, 630, 94, "PAUSA")).expandX();
        rodape.add(controle(recursos, 805, 126, "MENU")).expandX();
        var ajuda = estilo.texto("C EMBARCAR  •  Q ABORTAR  •  R REINICIAR");
        ajuda.setColor(0.6f, 0.82f, 0.88f, 1);
        rodape.add(ajuda).expandX();
        raiz.add(rodape).growX().height(60);
        stage.addActor(raiz);

        resultado.setBackground(recursos.painelPausa());
        resultado.setSize(700, 130);
        resultado.pad(20, 34, 20, 34);
        tituloResultado = estilo.valor("");
        tituloResultado.setAlignment(Align.center);
        resultado.add(tituloResultado).colspan(2).growX().padBottom(8).row();
        resultado.add(estilo.botao("REINICIAR", () -> { game.audio().selecionar(); game.iniciarPartida(); })).size(275, 48).padRight(12);
        resultado.add(estilo.botao("VOLTAR AO MENU", () -> { game.audio().selecionar(); game.mostrarMenu(); })).size(275, 48);
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
            if (game.entrada().pausaSolicitada() || controlador.atualizar()) game.pausarPartida();
            if (!pausado) {
                boolean geracaoReiniciada = geracao != controlador.geracao();
                if (geracaoReiniciada) {
                    efeitos.limpar();
                    game.audio().iniciar();
                    geracao = controlador.geracao();
                }
                if (geracaoReiniciada) posicoes.fixar(controlador.estado());
                for (var novo : controlador.consumirResultados()) {
                    efeitos.receber(novo);
                    game.audio().processar(novo);
                }
                posicoes.acompanhar(controlador.estado());
                posicoes.atualizar(delta);
                efeitos.atualizar(delta);
                faseBase = base.atualizar(controlador.estado(), delta);
                tempoVisual += delta;
            }
        }
        hud.atualizar(controlador.estado());
        var situacao = controlador.estado().situacao();
        resultado.setVisible(!pausado && situacao != Situacao.EM_ANDAMENTO);
        tituloResultado.setText(switch (situacao) {
            case VITORIA -> "RESGATE CONCLUÍDO";
            case DERROTA -> "FIM DE MISSÃO";
            case ABORTADA -> "MISSÃO ABORTADA";
            default -> "";
        });
        ScreenUtils.clear(0.005f, 0.015f, 0.035f, 1);
        stage.act(Math.min(delta, 0.1f));
        stage.getViewport().apply();
        stage.draw();
    }

    public boolean pausado() { return pausado; }

    private static Table controle(RecursosJogo recursos, int x, int largura, String acao) {
        var grupo = new Table();
        var imagem = new Image(new TextureRegion(recursos.textura(RecursosJogo.CONTROLES), x, 25, largura, 70));
        imagem.setScaling(Scaling.fit);
        var texto = new EstiloInterface(recursos).texto(acao);
        grupo.add(imagem).size(largura * 0.43f, 31).padRight(10);
        grupo.add(texto);
        return grupo;
    }
    public void exibirPausa(boolean mostrar) { pausado = mostrar; overlay.setVisible(mostrar); }
    public Stage stage() { return stage; }
    public int efeitosAtivos() { return efeitos.ativos().size(); }
    @Override public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        resultado.setPosition((stage.getViewport().getWorldWidth() - resultado.getWidth()) / 2, 96);
    }
    @Override public void pause() { game.pausarPartida(); }
    @Override public void hide() { if (Gdx.input.getInputProcessor() == stage) Gdx.input.setInputProcessor(null); }
    @Override public void dispose() { stage.dispose(); }
}
