package br.unifor.missaomarte.presentation.assets;

/**
 * Coração pixel-art das vidas (Fase 7), desenhado por código: nenhuma imagem de terceiros.
 * Sem LibGDX: devolve as cores RGBA8888 de cada pixel, para o RecursosJogo montar a textura
 * e para os testes conferirem o desenho.
 */
public final class DesenhoCoracao {
    /** O = contorno, X = preenchimento, H = brilho, . = transparente. */
    static final String[] PADRAO = {
            "..OOO...OOO..",
            ".OXXXO.OXXXO.",
            "OXHHXXOXXXXXO",
            "OXHXXXXXXXXXO",
            "OXXXXXXXXXXXO",
            "OXXXXXXXXXXXO",
            ".OXXXXXXXXXO.",
            "..OXXXXXXXO..",
            "...OXXXXXO...",
            "....OXXXO....",
            ".....OXO.....",
            "......O......",
    };
    public static final int LARGURA = 13;
    public static final int ALTURA = 12;

    // Cheio: vermelho-coral com brilho, combinando com o laranja do HUD. Vazio: só o contorno azul-acinzentado.
    private static final int CONTORNO_CHEIO = 0x3A0E14FF;
    private static final int RECHEIO_CHEIO = 0xE8453CFF;
    private static final int BRILHO_CHEIO = 0xFFB3A6FF;
    private static final int CONTORNO_VAZIO = 0x6F8FA3FF;
    private static final int RECHEIO_VAZIO = 0x14222D99;
    private static final int TRANSPARENTE = 0x00000000;

    private DesenhoCoracao() { }

    /** Cor RGBA8888 do pixel (x, y), com y = 0 na linha de cima (como no Pixmap). */
    public static int cor(int x, int y, boolean cheio) {
        return switch (PADRAO[y].charAt(x)) {
            case 'O' -> cheio ? CONTORNO_CHEIO : CONTORNO_VAZIO;
            case 'X' -> cheio ? RECHEIO_CHEIO : RECHEIO_VAZIO;
            case 'H' -> cheio ? BRILHO_CHEIO : RECHEIO_VAZIO;
            default -> TRANSPARENTE;
        };
    }
}
