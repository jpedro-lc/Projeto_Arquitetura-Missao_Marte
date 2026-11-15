package br.unifor.missaomarte.model;

/** Intenções do jogador, independentes de teclas, telas ou controles. */
public enum Acao {
    CIMA(0, 1), BAIXO(0, -1), ESQUERDA(-1, 0), DIREITA(1, 0),
    EMBARCAR(0, 0), ABORTAR(0, 0), INVALIDA(0, 0), VAZIA(0, 0);

    private final int dx;
    private final int dy;

    Acao(int dx, int dy) { this.dx = dx; this.dy = dy; }
    public int dx() { return dx; }
    public int dy() { return dy; }
    public boolean movimento() { return dx != 0 || dy != 0; }
}
