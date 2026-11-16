package br.unifor.missaomarte.model;

public class Inimigo extends EntidadeMapa implements Movel {
    public Inimigo(int x, int y) {
        super(x, y);
    }

    @Override
    public void mover(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    /** Mantém cada eixo dentro do mapa, permitindo deslizar ao longo da borda. */
    public void moverComLimites(int dx, int dy, int minX, int maxX, int minY, int maxY) {
        this.x = Math.max(minX, Math.min(maxX, this.x + dx));
        this.y = Math.max(minY, Math.min(maxY, this.y + dy));
    }

    @Override
    public String getSimbolo() {
        return "X";
    }
}
