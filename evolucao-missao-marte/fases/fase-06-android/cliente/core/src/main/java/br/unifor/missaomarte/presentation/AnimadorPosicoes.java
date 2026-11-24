package br.unifor.missaomarte.presentation;

import java.util.List;
import com.badlogic.gdx.math.Interpolation;
import br.unifor.missaomarte.model.EstadoPartida;
import br.unifor.missaomarte.model.EstadoPartida.Posicao;

/**
 * Movimento apenas visual: desliza nave e inimigos da célula anterior até a atual.
 * O estado lógico já mudou quando a animação começa; nenhum frame executa turno.
 */
public final class AnimadorPosicoes {
    public static final float DURACAO = 0.15f;
    private final Trajeto nave = new Trajeto();
    private Trajeto[] inimigos = new Trajeto[0];

    /** Posiciona tudo no destino, sem animação (início ou reinício de partida). */
    public void fixar(EstadoPartida estado) {
        nave.fixar(estado.nave().x(), estado.nave().y());
        inimigos = new Trajeto[estado.inimigos().size()];
        for (int i = 0; i < inimigos.length; i++) {
            inimigos[i] = new Trajeto();
            inimigos[i].fixar(estado.inimigos().get(i).x(), estado.inimigos().get(i).y());
        }
    }

    /** Chamado a cada frame com a fotografia atual; inicia trajetos quando o destino muda. */
    public void acompanhar(EstadoPartida estado) {
        List<Posicao> atuais = estado.inimigos();
        if (atuais.size() != inimigos.length) { fixar(estado); return; }
        nave.destino(estado.nave().x(), estado.nave().y());
        for (int i = 0; i < inimigos.length; i++) inimigos[i].destino(atuais.get(i).x(), atuais.get(i).y());
    }

    public void atualizar(float delta) {
        nave.avancar(delta);
        for (Trajeto t : inimigos) t.avancar(delta);
    }

    public float naveX() { return nave.x(); }
    public float naveY() { return nave.y(); }
    public float inimigoX(int i) { return inimigos[i].x(); }
    public float inimigoY(int i) { return inimigos[i].y(); }
    public boolean emMovimento() { return nave.tempo < DURACAO; }

    private static final class Trajeto {
        private float origemX, origemY;
        private int destinoX, destinoY;
        private float tempo = DURACAO;

        void fixar(int x, int y) { destinoX = x; destinoY = y; origemX = x; origemY = y; tempo = DURACAO; }

        void destino(int x, int y) {
            if (x == destinoX && y == destinoY) return;
            // Parte da posição desenhada agora: uma tecla rápida "corta" a animação anterior.
            origemX = x();
            origemY = y();
            destinoX = x;
            destinoY = y;
            tempo = 0;
        }

        void avancar(float delta) { tempo = Math.min(DURACAO, tempo + Math.max(0, delta)); }
        private float alfa() { return Interpolation.smooth.apply(tempo / DURACAO); }
        float x() { return origemX + (destinoX - origemX) * alfa(); }
        float y() { return origemY + (destinoY - origemY) * alfa(); }
    }
}
