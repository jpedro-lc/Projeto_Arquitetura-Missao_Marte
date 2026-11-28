package br.unifor.missaomarte.presentation.assets;

import br.unifor.missaomarte.model.Acao;

/**
 * Seta do direcional na tela (Fase 7), desenhada por código como o coração. Um só padrão (para cima);
 * as outras direções saem girando as coordenadas. Sem LibGDX: devolve cores RGBA8888.
 */
public final class DesenhoSeta {
    /** O = contorno, X = preenchimento, . = transparente. Quadrado, para girar sem distorcer. */
    static final String[] PADRAO = {
            ".....O.....",
            "....OXO....",
            "...OXXXO...",
            "..OXXXXXO..",
            ".OXXXXXXXO.",
            "OXXXXXXXXXO",
            "OOOOXXXOOOO",
            "...OXXXO...",
            "...OXXXO...",
            "...OXXXO...",
            "...OOOOO...",
    };
    public static final int LADO = 11;

    private static final int CONTORNO = 0x0B1A24FF;
    private static final int RECHEIO = 0x7FE7F2FF;
    private static final int TRANSPARENTE = 0x00000000;

    private DesenhoSeta() { }

    /** Cor do pixel (x, y), y = 0 em cima, para a seta apontando na direção pedida. */
    public static int cor(int x, int y, Acao direcao) {
        int ultimo = LADO - 1;
        // Leva (x, y) da seta pedida para o pixel correspondente da seta para cima.
        int px;
        int py;
        switch (direcao) {
            case CIMA -> { px = x; py = y; }
            case BAIXO -> { px = x; py = ultimo - y; }
            case ESQUERDA -> { px = y; py = x; }
            case DIREITA -> { px = y; py = ultimo - x; }
            default -> throw new IllegalArgumentException("Seta só para movimentos: " + direcao);
        }
        return switch (PADRAO[py].charAt(px)) {
            case 'O' -> CONTORNO;
            case 'X' -> RECHEIO;
            default -> TRANSPARENTE;
        };
    }
}
