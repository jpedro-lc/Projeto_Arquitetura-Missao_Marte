package br.unifor.missaomarte.presentation.assets;

import java.util.ArrayList;
import java.util.List;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Disposable;

/** Recursos da instância Game, carregados uma vez e compartilhados pelas telas. */
public final class RecursosJogo implements Disposable {
    private static final String SPACE = "sprites/spacerage_original/";
    public static final String NAVE = SPACE + "Player/player_b_m.png";
    public static final String INIMIGO = SPACE + "Enemies/enemy_1_r_m.png";
    public static final String ASTEROIDE = "sprites/asteroides/asteroide_cinza_03.png";
    public static final String FUNDO = SPACE + "BG.png";
    public static final String PAINEL = "sprites/hud/painel_area_jogo_referencia.png";
    public static final String CONTROLES = "sprites/hud/controles_referencia.png";
    public static final String PROFESSOR = "sprites/passageiros/passageiro_branco_azul_parado.png";
    public static final String ENGENHEIRO = "sprites/passageiros/passageiro_laranja_parado.png";
    public static final String ASTRONAUTA = "sprites/passageiros/passageiro_verde_parado.png";
    public static final String RESGATADO = "sprites/passageiros/passageiro_branco_azul_resgatado.png";
    public static final String MUSICA = "sounds/musicas/musica_fundo.mp3";
    public static final String DERROTA = "sounds/efeitos/derrota.mp3";
    public static final String IMPACTO_ASTEROIDE = "sounds/efeitos/colisao_asteroide.ogg";
    public static final String IMPACTO_INIMIGO = "sounds/efeitos/colisao_inimigo.ogg";
    public static final String RESGATE = "sounds/efeitos/resgate.ogg";
    public static final String VITORIA = "sounds/efeitos/vitoria.ogg";
    public static final String MENU = "sounds/efeitos/menu_selecionar.ogg";
    public static final String ERRO = "sounds/efeitos/erro_acao.ogg";
    private final AssetManager gerenciador = new AssetManager();
    private final Texture pixel;
    private final List<BitmapFont> fontes = new ArrayList<>();
    private final BitmapFont pequena;
    private final BitmapFont media;
    private final BitmapFont titulo;
    private final NinePatch moldura;

    public RecursosJogo() {
        for (String caminho : new String[] {NAVE, INIMIGO, ASTEROIDE, FUNDO, PAINEL, CONTROLES, PROFESSOR, ENGENHEIRO, ASTRONAUTA, RESGATADO}) {
            gerenciador.load(caminho, Texture.class);
        }
        for (int i = 1; i <= 11; i++) gerenciador.load(explosao(i), Texture.class);
        for (int i = 1; i <= 5; i++) gerenciador.load(propulsor(i), Texture.class);
        for (String caminho : new String[] {IMPACTO_ASTEROIDE, IMPACTO_INIMIGO, RESGATE, VITORIA, MENU, ERRO}) {
            gerenciador.load(caminho, Sound.class);
        }
        gerenciador.load(MUSICA, Music.class);
        // Game Over tem 7,68s; Music evita carregar seu PCM inteiro como efeito curto.
        gerenciador.load(DERROTA, Music.class);
        gerenciador.finishLoading();
        textura(FUNDO).setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixel = criarPixel(Color.WHITE);
        Texture painel = textura(PAINEL);
        // Somente bordas: o centro com o texto fixo "ÁREA DO JOGO" não é utilizado.
        moldura = new NinePatch(new TextureRegion(painel, 22, 12, 56, 36),
                new TextureRegion(painel, 280, 12, 80, 36), new TextureRegion(painel, 652, 12, 56, 36),
                new TextureRegion(painel, 22, 80, 56, 120), new TextureRegion(painel, 300, 80, 1, 1),
                new TextureRegion(painel, 652, 80, 56, 120), new TextureRegion(painel, 22, 234, 56, 36),
                new TextureRegion(painel, 280, 234, 80, 36), new TextureRegion(painel, 652, 234, 56, 36));
        moldura.scale(0.55f, 0.55f);
        var gerador = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Audiowide-Regular.ttf"));
        try {
            pequena = fonte(gerador, 18);
            media = fonte(gerador, 26);
            titulo = fonte(gerador, 58);
        } finally { gerador.dispose(); }
    }

    private BitmapFont fonte(FreeTypeFontGenerator gerador, int tamanho) {
        var parametros = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parametros.size = tamanho;
        parametros.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "—•";
        parametros.minFilter = Texture.TextureFilter.Linear;
        parametros.magFilter = Texture.TextureFilter.Linear;
        var fonte = gerador.generateFont(parametros);
        fontes.add(fonte);
        return fonte;
    }

    private static Texture criarPixel(Color cor) {
        Pixmap mapa = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        try {
            mapa.setColor(cor);
            mapa.fill();
            return new Texture(mapa);
        } finally { mapa.dispose(); }
    }

    public static String explosao(int frame) { return SPACE + "Explosions/explosion_1_" + (frame < 10 ? "0" : "") + frame + ".png"; }
    public static String propulsor(int frame) { return SPACE + "FX/exhaust_0" + frame + ".png"; }
    public Texture textura(String caminho) { return gerenciador.get(caminho, Texture.class); }
    public Sound som(String caminho) { return gerenciador.get(caminho, Sound.class); }
    public Music musica(String caminho) { return gerenciador.get(caminho, Music.class); }
    public Texture pixel() { return pixel; }
    public BitmapFont pequena() { return pequena; }
    public BitmapFont media() { return media; }
    public BitmapFont titulo() { return titulo; }
    public NinePatchDrawable painel() {
        var detalhe = new TextureRegion(textura(PAINEL), 143, 12, 80, 26);
        return new NinePatchDrawable(moldura) {
            @Override public void draw(Batch batch, float x, float y, float width, float height) {
                super.draw(batch, x, y, width, height);
                batch.draw(detalhe, x + width * 0.16f, y + height - 14, 46, 12);
            }
        };
    }

    @Override public void dispose() {
        for (BitmapFont fonte : fontes) fonte.dispose();
        pixel.dispose();
        gerenciador.dispose();
    }
}
