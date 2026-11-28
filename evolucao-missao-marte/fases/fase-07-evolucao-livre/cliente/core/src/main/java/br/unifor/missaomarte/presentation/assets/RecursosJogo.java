package br.unifor.missaomarte.presentation.assets;

import java.util.ArrayList;
import java.util.List;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.IntBinaryOperator;
import br.unifor.missaomarte.model.Acao;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Disposable;

/** Recursos da instância Game, carregados uma vez e compartilhados pelas telas. */
public final class RecursosJogo implements Disposable {
    private static final String SPACE = "sprites/spacerage_original/";
    public static final String NAVE = SPACE + "Player/player_b_m.png";
    public static final String INIMIGO = SPACE + "Enemies/enemy_1_r_m.png";
    public static final String FUNDO = SPACE + "BG.png";
    public static final String CONTROLES = "sprites/hud/controles_referencia.png";
    private static final String UI = "sprites/ui_spacerage/";
    public static final String HUD_SUPERIOR = UI + "hud/hud_top_background.png";
    public static final String HUD_INFERIOR = UI + "hud/hud_lower_background.png";
    public static final String RODAPE = UI + "footer/footer_background.png";
    public static final String PAINEL_MENU = UI + "menus/menu_panel.png";
    public static final String PAINEL_PAUSA = UI + "pause/pause_panel.png";
    public static final String BOTAO_NORMAL = UI + "menus/menu_button_normal.png";
    public static final String BOTAO_SOBRE = UI + "menus/menu_button_hover.png";
    public static final String BOTAO_PRESSIONADO = UI + "menus/menu_button_pressed.png";
    public static final String BASE_OCIOSA = UI + "base/base_idle.png";
    public static final String BASE_ATIVA = UI + "base/base_active.png";
    public static final String BASE_POUSO = UI + "base/base_landing_01.png";
    public static final String BASE_CONCLUIDA = UI + "base/base_complete.png";
    /** Seis variações: grande, média e pequena; a escolha é só aparência. */
    public static final String[] ASTEROIDES = {
        UI + "asteroids/asteroid_large_01.png", UI + "asteroids/asteroid_large_02.png",
        UI + "asteroids/asteroid_medium_01.png", UI + "asteroids/asteroid_medium_02.png",
        UI + "asteroids/asteroid_small_01.png", UI + "asteroids/asteroid_small_02.png"};
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
    private final Texture coracaoCheio = desenhar(DesenhoCoracao.LARGURA, DesenhoCoracao.ALTURA, (x, y) -> DesenhoCoracao.cor(x, y, true));
    private final Texture coracaoVazio = desenhar(DesenhoCoracao.LARGURA, DesenhoCoracao.ALTURA, (x, y) -> DesenhoCoracao.cor(x, y, false));
    private final Map<Acao, Texture> setas = new EnumMap<>(Acao.class);
    private final List<BitmapFont> fontes = new ArrayList<>();
    private final BitmapFont pequena;
    private final BitmapFont media;
    private final BitmapFont titulo;

    public RecursosJogo() {
        List<String> pixelArt = new ArrayList<>(List.of(NAVE, INIMIGO, HUD_SUPERIOR, HUD_INFERIOR, RODAPE,
                PAINEL_MENU, PAINEL_PAUSA, BOTAO_NORMAL, BOTAO_SOBRE, BOTAO_PRESSIONADO,
                BASE_OCIOSA, BASE_ATIVA, BASE_POUSO, BASE_CONCLUIDA));
        pixelArt.addAll(List.of(ASTEROIDES));
        for (int i = 1; i <= 4; i++) { pixelArt.add(astronauta(i)); pixelArt.add(resgate(i)); }
        for (int i = 1; i <= 11; i++) pixelArt.add(explosao(i));
        for (int i = 1; i <= 5; i++) pixelArt.add(propulsor(i));
        for (String caminho : pixelArt) gerenciador.load(caminho, Texture.class);
        gerenciador.load(FUNDO, Texture.class);
        gerenciador.load(CONTROLES, Texture.class);
        for (String caminho : new String[] {IMPACTO_ASTEROIDE, IMPACTO_INIMIGO, RESGATE, VITORIA, MENU, ERRO}) {
            gerenciador.load(caminho, Sound.class);
        }
        gerenciador.load(MUSICA, Music.class);
        // Game Over tem 7,68s; Music evita carregar seu PCM inteiro como efeito curto.
        gerenciador.load(DERROTA, Music.class);
        gerenciador.finishLoading();
        textura(FUNDO).setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        // Pixel art: Nearest evita borrar contornos ao ampliar sprites pequenas.
        for (String caminho : pixelArt) textura(caminho).setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        pixel = criarPixel(Color.WHITE);
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

    /** Desenhos por código da Fase 7 (corações, setas); o filtro Nearest padrão mantém os pixels nítidos ao ampliar. */
    private static Texture desenhar(int largura, int altura, IntBinaryOperator cor) {
        Pixmap mapa = new Pixmap(largura, altura, Pixmap.Format.RGBA8888);
        try {
            mapa.setBlending(Pixmap.Blending.None);
            for (int y = 0; y < altura; y++) {
                for (int x = 0; x < largura; x++) mapa.drawPixel(x, y, cor.applyAsInt(x, y));
            }
            return new Texture(mapa);
        } finally { mapa.dispose(); }
    }

    public static String explosao(int frame) { return SPACE + "Explosions/explosion_1_" + (frame < 10 ? "0" : "") + frame + ".png"; }
    public static String propulsor(int frame) { return SPACE + "FX/exhaust_0" + frame + ".png"; }
    public static String astronauta(int frame) { return UI + "astronauts/astronaut_idle_0" + frame + ".png"; }
    public static String resgate(int frame) { return UI + "astronauts/astronaut_rescue_0" + frame + ".png"; }
    public Texture textura(String caminho) { return gerenciador.get(caminho, Texture.class); }
    public Sound som(String caminho) { return gerenciador.get(caminho, Sound.class); }
    public Music musica(String caminho) { return gerenciador.get(caminho, Music.class); }
    public Texture pixel() { return pixel; }
    public Texture coracao(boolean cheio) { return cheio ? coracaoCheio : coracaoVazio; }
    /** Seta do direcional na tela; criada na primeira vez que a partida pede. */
    public Texture seta(Acao direcao) {
        return setas.computeIfAbsent(direcao, d -> desenhar(DesenhoSeta.LADO, DesenhoSeta.LADO, (x, y) -> DesenhoSeta.cor(x, y, d)));
    }
    public BitmapFont pequena() { return pequena; }
    public BitmapFont media() { return media; }
    public BitmapFont titulo() { return titulo; }
    /** Nine-slice: cantos e espessura das bordas preservados; só o miolo estica. */
    public NinePatchDrawable moldura(String caminho, int esquerda, int direita, int cima, int baixo) {
        return new NinePatchDrawable(new NinePatch(textura(caminho), esquerda, direita, cima, baixo));
    }
    public NinePatchDrawable painelHud() { return moldura(HUD_SUPERIOR, 24, 24, 14, 14); }
    public NinePatchDrawable painelHudInferior() { return moldura(HUD_INFERIOR, 24, 24, 14, 14); }
    public NinePatchDrawable painelRodape() { return moldura(RODAPE, 40, 40, 14, 14); }
    public NinePatchDrawable painelMenu() { return moldura(PAINEL_MENU, 30, 30, 30, 30); }
    public NinePatchDrawable painelPausa() { return moldura(PAINEL_PAUSA, 30, 30, 30, 30); }
    public NinePatchDrawable botao(String caminho) { return moldura(caminho, 10, 10, 10, 10); }

    @Override public void dispose() {
        for (BitmapFont fonte : fontes) fonte.dispose();
        pixel.dispose();
        coracaoCheio.dispose();
        coracaoVazio.dispose();
        for (Texture seta : setas.values()) seta.dispose();
        gerenciador.dispose();
    }
}
