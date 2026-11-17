package br.unifor.missaomarte.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import br.unifor.missaomarte.MissaoMarteGame;
import br.unifor.missaomarte.presentation.EstiloInterface;
import br.unifor.missaomarte.presentation.assets.RecursosJogo;
import br.unifor.missaomarte.presentation.rendering.PartidaRenderer;

/** Menu e instruções compartilham recursos e o mesmo fluxo de navegação. */
public final class MenuScreen extends ScreenAdapter {
    private final MissaoMarteGame game;
    private final RecursosJogo recursos;
    private final Stage stage = new Stage(new ExtendViewport(PartidaRenderer.LARGURA, PartidaRenderer.ALTURA));
    private final Table conteudo = new Table();
    private boolean instrucoes;

    public MenuScreen(MissaoMarteGame game, RecursosJogo recursos) {
        this.game = game;
        this.recursos = recursos;
        stage.addActor(new Actor() {
            @Override public void draw(Batch batch, float parentAlpha) {
                float largura = getStage().getViewport().getWorldWidth();
                float altura = getStage().getViewport().getWorldHeight();
                PartidaRenderer.fundo(batch, recursos, 0, 0, largura, altura);
                batch.setColor(0, 0.015f, 0.04f, 0.45f);
                batch.draw(recursos.pixel(), 0, 0, largura, altura);
                batch.setColor(Color.WHITE);
            }
        });
        var raiz = new Table();
        raiz.setFillParent(true);
        conteudo.setBackground(recursos.painelMenu());
        conteudo.pad(46, 60, 46, 60);
        raiz.add(conteudo).width(850);
        stage.addActor(raiz);
        principal();
    }

    public void principal() {
        instrucoes = false;
        conteudo.clearChildren();
        var estilo = new EstiloInterface(recursos);
        conteudo.add(estilo.texto("UNIFOR  /  MISSÃO DE RESGATE")).padBottom(22).row();
        conteudo.add(estilo.titulo("MISSÃO MARTE")).padBottom(12).row();
        var subtitulo = estilo.valor("RESGATE ESPACIAL");
        subtitulo.getStyle().fontColor = Color.WHITE;
        subtitulo.setColor(EstiloInterface.LARANJA);
        conteudo.add(subtitulo).padBottom(52).row();
        conteudo.add(estilo.botao("INICIAR JOGO", () -> { game.audio().selecionar(); game.iniciarPartida(); }))
                .size(360, 76).padBottom(14).row();
        conteudo.add(estilo.botao("INSTRUÇÕES", () -> { game.audio().selecionar(); instrucoes(); }))
                .size(360, 76).padBottom(14).row();
        conteudo.add(estilo.botao("SAIR", () -> { game.audio().selecionar(); Gdx.app.exit(); }))
                .size(360, 76).padBottom(40).row();
        conteudo.add(estilo.texto("Explore. Resgate os passageiros. Retorne à base."));
    }

    public void instrucoes() {
        instrucoes = true;
        conteudo.clearChildren();
        var estilo = new EstiloInterface(recursos);
        conteudo.add(estilo.valor("COMO JOGAR")).padBottom(28).row();
        var texto = estilo.texto("Resgate todos os passageiros e retorne à BASE (0,0).\n\n"
                + "WASD  —  mover uma posição por comando\n"
                + "C  —  embarcar o passageiro na posição da nave\n"
                + "Q  —  abortar a missão\n"
                + "P ou ESC  —  pausar e abrir as opções\n"
                + "R  —  reiniciar depois do encerramento\n\n"
                + "Cada movimento consome um ponto. Colisões reduzem vidas.\n"
                + "Professor +15  /  Engenheiro +20  /  Astronauta +10");
        texto.setAlignment(Align.left);
        texto.setWrap(true);
        conteudo.add(texto).width(760).padBottom(28).row();
        conteudo.add(estilo.botao("VOLTAR", () -> { game.audio().selecionar(); principal(); })).size(300, 70);
    }

    public boolean emInstrucoes() { return instrucoes; }
    public Stage stage() { return stage; }
    @Override public void show() { Gdx.input.setInputProcessor(stage); }
    @Override public void render(float delta) {
        ScreenUtils.clear(0.005f, 0.015f, 0.035f, 1);
        stage.act(Math.min(delta, 0.1f));
        stage.getViewport().apply();
        stage.draw();
    }
    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void hide() { if (Gdx.input.getInputProcessor() == stage) Gdx.input.setInputProcessor(null); }
    @Override public void dispose() { stage.dispose(); }
}
