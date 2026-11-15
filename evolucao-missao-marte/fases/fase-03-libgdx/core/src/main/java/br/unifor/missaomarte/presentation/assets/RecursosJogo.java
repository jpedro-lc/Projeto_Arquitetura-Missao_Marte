package br.unifor.missaomarte.presentation.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;

/** Dono das quatro texturas fornecidas e de um pixel para painéis/grade. */
public final class RecursosJogo implements Disposable {
    public static final String NAVE = "images/nave.png";
    public static final String INIMIGO = "images/inimigo.png";
    public static final String ASTEROIDE = "images/asteroide.png";
    public static final String FUNDO = "images/fundo.png";

    private final AssetManager gerenciador = new AssetManager();
    private final Texture pixel;

    public RecursosJogo() {
        for (String caminho : new String[] {NAVE, INIMIGO, ASTEROIDE, FUNDO}) {
            gerenciador.load(caminho, Texture.class);
        }
        // Só quatro imagens pequenas: carga inicial única, nunca dentro de render().
        gerenciador.finishLoading();
        gerenciador.get(FUNDO, Texture.class).setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        gerenciador.get(ASTEROIDE, Texture.class).setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        Pixmap mapa = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        try {
            mapa.setColor(1, 1, 1, 1);
            mapa.fill();
            pixel = new Texture(mapa);
        } finally {
            mapa.dispose();
        }
    }

    public Texture textura(String caminho) { return gerenciador.get(caminho, Texture.class); }
    public Texture pixel() { return pixel; }

    @Override
    public void dispose() {
        pixel.dispose();
        gerenciador.dispose();
    }
}
