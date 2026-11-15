package br.unifor.missaomarte.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import br.unifor.missaomarte.presentation.ControladorPartida;
import br.unifor.missaomarte.presentation.assets.RecursosJogo;
import br.unifor.missaomarte.presentation.rendering.PartidaRenderer;

/** Tela comum às plataformas; não cria janela, teclado ou repositório. */
public final class GameScreen extends ScreenAdapter {
    private final ControladorPartida controlador;
    private final FitViewport viewport = new FitViewport(PartidaRenderer.LARGURA, PartidaRenderer.ALTURA);
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont fonte = new BitmapFont();
    private final PartidaRenderer renderer;

    public GameScreen(ControladorPartida controlador, RecursosJogo recursos) {
        this.controlador = controlador;
        renderer = new PartidaRenderer(batch, fonte, recursos);
    }

    @Override
    public void show() { controlador.retomar(); }

    @Override
    public void render(float delta) {
        if (controlador.atualizar()) Gdx.app.exit();
        ScreenUtils.clear(0.01f, 0.015f, 0.035f, 1);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        renderer.desenhar(controlador.estado(), controlador.eventos());
    }

    @Override
    public void resize(int width, int height) { viewport.update(width, height, true); }

    @Override
    public void pause() { controlador.pausar(); }

    @Override
    public void resume() { controlador.retomar(); }

    @Override
    public void hide() { controlador.pausar(); }

    @Override
    public void dispose() {
        batch.dispose();
        fonte.dispose();
        // As texturas pertencem ao Game; esta tela apenas as utiliza.
    }
}
