package br.unifor.missaomarte.presentation.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import br.unifor.missaomarte.model.EstadoPartida;
import br.unifor.missaomarte.presentation.AnimadorPosicoes;
import br.unifor.missaomarte.presentation.EfeitosPartida;
import br.unifor.missaomarte.presentation.EstadoVisualBase;
import br.unifor.missaomarte.presentation.assets.RecursosJogo;

/** Converte coordenadas discretas em posições gráficas; não avança regras. */
public final class PartidaRenderer {
    public static final float LARGURA = 1280;
    public static final float ALTURA = 800;
    /** Tempo em que a nave pisca e a área treme após um impacto. */
    static final float DANO = 0.6f;
    private static final Color[] TONS_PASSAGEIRO = {
        Color.WHITE, new Color(1f, 0.72f, 0.45f, 1), new Color(0.6f, 1f, 0.65f, 1)};
    private static final float[] ESCALA_ASTEROIDE = {1f, 1f, 0.8f, 0.8f, 0.62f, 0.62f};
    private final Animation<TextureRegion> explosao;
    private final Animation<TextureRegion> propulsor;
    private final Animation<TextureRegion> astronauta;
    private final Animation<TextureRegion> resgate;
    private final TextureRegion nave;
    private final TextureRegion inimigo;
    private final TextureRegion[] asteroides = new TextureRegion[RecursosJogo.ASTEROIDES.length];
    private final TextureRegion[] base = new TextureRegion[EstadoVisualBase.Fase.values().length];
    private final Texture pixel;

    public PartidaRenderer(RecursosJogo recursos) {
        // Regiões criadas uma única vez: render() apenas desenha.
        explosao = animacao(recursos, 11, 0.08f, Animation.PlayMode.NORMAL, RecursosJogo::explosao);
        propulsor = animacao(recursos, 5, 0.08f, Animation.PlayMode.LOOP, RecursosJogo::propulsor);
        astronauta = animacao(recursos, 4, 0.18f, Animation.PlayMode.LOOP_PINGPONG, RecursosJogo::astronauta);
        resgate = animacao(recursos, 4, 0.12f, Animation.PlayMode.NORMAL, RecursosJogo::resgate);
        nave = new TextureRegion(recursos.textura(RecursosJogo.NAVE));
        inimigo = new TextureRegion(recursos.textura(RecursosJogo.INIMIGO));
        for (int i = 0; i < asteroides.length; i++) asteroides[i] = new TextureRegion(recursos.textura(RecursosJogo.ASTEROIDES[i]));
        base[EstadoVisualBase.Fase.OCIOSA.ordinal()] = new TextureRegion(recursos.textura(RecursosJogo.BASE_OCIOSA));
        base[EstadoVisualBase.Fase.ATIVA.ordinal()] = new TextureRegion(recursos.textura(RecursosJogo.BASE_ATIVA));
        base[EstadoVisualBase.Fase.POUSO.ordinal()] = new TextureRegion(recursos.textura(RecursosJogo.BASE_POUSO));
        base[EstadoVisualBase.Fase.CONCLUIDA.ordinal()] = new TextureRegion(recursos.textura(RecursosJogo.BASE_CONCLUIDA));
        pixel = recursos.pixel();
    }

    private static Animation<TextureRegion> animacao(RecursosJogo recursos, int quadros, float duracao,
                                                     Animation.PlayMode modo, java.util.function.IntFunction<String> caminho) {
        var frames = new Array<TextureRegion>(quadros);
        for (int i = 1; i <= quadros; i++) frames.add(new TextureRegion(recursos.textura(caminho.apply(i))));
        return new Animation<>(duracao, frames, modo);
    }

    public void desenhar(Batch batch, EstadoPartida estado, EfeitosPartida efeitos, AnimadorPosicoes posicoes,
                         EstadoVisualBase.Fase fase, float x, float y, float largura, float altura, float tempoVisual) {
        float dano = tempoDeDano(efeitos);
        // Tremor curto e decrescente: apenas deslocamento da câmera visual.
        float tremor = dano < 0 ? 0 : 5 * (1 - dano / DANO) * MathUtils.sin(dano * 70);
        batch.setColor(0.015f, 0.025f, 0.065f, 0.25f);
        batch.draw(pixel, x, y, largura, altura);
        batch.setColor(Color.WHITE);
        int lado = estado.limite() * 2 + 1;
        float passoX = (largura - 80) / lado;
        float passoY = (altura - 50) / lado;
        float tamanho = Math.min(62, Math.min(passoX, passoY) * 1.2f);
        float origemX = x + largura / 2 + tremor;
        float origemY = y + altura / 2 + tremor * 0.6f;

        desenharImagem(batch, base[fase.ordinal()], origemX, origemY, tamanho * 1.45f);
        if (fase == EstadoVisualBase.Fase.POUSO) {
            batch.setColor(0.3f, 0.95f, 1f, 0.35f + 0.25f * MathUtils.sin(tempoVisual * 12));
            desenharImagem(batch, base[EstadoVisualBase.Fase.ATIVA.ordinal()], origemX, origemY, tamanho * 1.6f);
            batch.setColor(Color.WHITE);
        }
        for (var a : estado.asteroides()) {
            int variacao = Math.floorMod(a.x() * 31 + a.y() * 17, asteroides.length);
            desenharImagem(batch, asteroides[variacao], origemX + a.x() * passoX, origemY + a.y() * passoY,
                    tamanho * ESCALA_ASTEROIDE[variacao]);
        }
        for (var p : estado.passageiros()) {
            batch.setColor(TONS_PASSAGEIRO[switch (p.tipo()) { case "Engenheiro" -> 1; case "Astronauta" -> 2; default -> 0; }]);
            // Fase deslocada pela posição para os astronautas não piscarem em sincronia.
            desenharImagem(batch, astronauta.getKeyFrame(tempoVisual + defasagem(p.x(), p.y())),
                    origemX + p.x() * passoX, origemY + p.y() * passoY, tamanho * 0.85f);
        }
        batch.setColor(Color.WHITE);
        for (int i = 0; i < estado.inimigos().size(); i++) {
            desenharImagem(batch, inimigo, origemX + posicoes.inimigoX(i) * passoX,
                    origemY + posicoes.inimigoY(i) * passoY, tamanho);
        }
        if (estado.nave().vidas() > 0 && !(dano >= 0 && ((int) (dano * 16)) % 2 == 1)) {
            float nx = origemX + posicoes.naveX() * passoX;
            float ny = origemY + posicoes.naveY() * passoY;
            desenharImagem(batch, propulsor.getKeyFrame(tempoVisual), nx, ny - tamanho * 0.2f, tamanho);
            desenharImagem(batch, nave, nx, ny, tamanho * 1.1f);
        }
        for (var efeito : efeitos.ativos()) {
            float ex = origemX + efeito.x() * passoX;
            float ey = origemY + efeito.y() * passoY;
            if (efeito.tipo() == EfeitosPartida.Tipo.EXPLOSAO) {
                desenharImagem(batch, explosao.getKeyFrame(efeito.idade()), ex, ey, tamanho * 2);
            } else {
                batch.setColor(1, 1, 1, 1 - efeito.idade() / efeito.duracao());
                desenharImagem(batch, resgate.getKeyFrame(efeito.idade()), ex, ey + efeito.idade() * tamanho, tamanho * 1.2f);
                batch.setColor(Color.WHITE);
            }
        }
    }

    /** Sempre >= 0: Animation.getKeyFrame com tempo negativo devolve índice -1 em LOOP_PINGPONG. */
    static float defasagem(int x, int y) { return Math.floorMod(x + y, 8) * 0.13f; }

    /** Idade da explosão mais recente dentro da janela de dano, ou -1. */
    static float tempoDeDano(EfeitosPartida efeitos) {
        float menor = -1;
        for (var efeito : efeitos.ativos()) {
            if (efeito.tipo() == EfeitosPartida.Tipo.EXPLOSAO && efeito.idade() < DANO
                    && (menor < 0 || efeito.idade() < menor)) menor = efeito.idade();
        }
        return menor;
    }

    private static void desenharImagem(Batch batch, TextureRegion imagem, float x, float y, float tamanho) {
        float escala = tamanho / Math.max(imagem.getRegionWidth(), imagem.getRegionHeight());
        float largura = imagem.getRegionWidth() * escala;
        float altura = imagem.getRegionHeight() * escala;
        batch.draw(imagem, x - largura / 2, y - altura / 2, largura, altura);
    }

    /** Cobre a área inteira recortando a imagem proporcionalmente (sem esticar). */
    public static void fundo(Batch batch, RecursosJogo recursos, float x, float y, float largura, float altura) {
        Texture imagem = recursos.textura(RecursosJogo.FUNDO);
        float aspectoImagem = (float) imagem.getWidth() / imagem.getHeight();
        float aspectoTela = largura / altura;
        float u = Math.min(1, aspectoTela / aspectoImagem);
        float v = Math.min(1, aspectoImagem / aspectoTela);
        batch.setColor(Color.WHITE);
        batch.draw(imagem, x, y, largura, altura, (1 - u) / 2, (1 + v) / 2, (1 + u) / 2, (1 - v) / 2);
    }
}
