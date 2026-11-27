package br.unifor.missaomarte.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import br.unifor.missaomarte.MissaoMarteGame;
import br.unifor.missaomarte.presentation.EstiloInterface;
import br.unifor.missaomarte.presentation.assets.RecursosJogo;
import br.unifor.missaomarte.presentation.rendering.PartidaRenderer;

/** Base das telas de menu: fundo em tela cheia e um painel central (Template Method para o conteúdo). */
public abstract class TelaPainel extends ScreenAdapter {
    protected final MissaoMarteGame game;
    protected final RecursosJogo recursos;
    protected final EstiloInterface estilo;
    protected final Stage stage = new Stage(new ExtendViewport(PartidaRenderer.LARGURA, PartidaRenderer.ALTURA));
    protected final Table conteudo = new Table();

    protected TelaPainel(MissaoMarteGame game, RecursosJogo recursos, float largura) {
        this.game = game;
        this.recursos = recursos;
        this.estilo = new EstiloInterface(recursos);
        stage.addActor(new Actor() {
            @Override public void draw(Batch batch, float parentAlpha) {
                float l = getStage().getViewport().getWorldWidth();
                float a = getStage().getViewport().getWorldHeight();
                PartidaRenderer.fundo(batch, recursos, 0, 0, l, a);
                batch.setColor(0, 0.015f, 0.04f, 0.45f);
                batch.draw(recursos.pixel(), 0, 0, l, a);
                batch.setColor(Color.WHITE);
            }
        });
        var raiz = new Table();
        raiz.setFillParent(true);
        conteudo.setBackground(recursos.painelMenu());
        conteudo.pad(40, 56, 40, 56);
        raiz.add(conteudo).width(largura);
        stage.addActor(raiz);
    }

    protected TextButton botao(String texto, Runnable acao) {
        return estilo.botao(texto, () -> { game.audio().selecionar(); acao.run(); });
    }

    protected Label aviso(String texto, Color cor) {
        var label = estilo.texto(texto);
        label.setColor(cor);
        label.setName("aviso");
        return label;
    }

    /** Tabela simples de texto; a linha "destaque" (índice ou -1) aparece em laranja. */
    protected Table tabela(String[] cabecalho, java.util.List<String[]> linhas, int destaque) {
        var tabela = new Table();
        for (String coluna : cabecalho) {
            var label = estilo.texto(coluna);
            label.setColor(0.6f, 0.82f, 0.88f, 1);
            tabela.add(label).padRight(22).padBottom(10).left();
        }
        tabela.row();
        for (int i = 0; i < linhas.size(); i++) {
            for (String celula : linhas.get(i)) {
                var label = estilo.texto(celula);
                if (i == destaque) label.setColor(EstiloInterface.LARANJA);
                tabela.add(label).padRight(22).padBottom(6).left();
            }
            tabela.row();
        }
        return tabela;
    }

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
