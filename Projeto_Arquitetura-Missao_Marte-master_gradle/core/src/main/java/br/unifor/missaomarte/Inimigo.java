import java.util.Random;

public class Inimigo {
    private int x;
    private int y;

    public Inimigo(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    /**
     * Move o inimigo aleatoriamente em uma das 4 direções (cima, baixo, esquerda, direita)
     * respeitando os limites da grade do jogo.
     */
    public void mover(Random random, int minX, int maxX, int minY, int maxY) {
        int direcao = random.nextInt(4);
        int novoX = this.x;
        int novoY = this.y;

        switch (direcao) {
            case 0: // Cima (no sistema cartesiano de tela/grade do jogo)
                novoY--;
                break;
            case 1: // Baixo
                novoY++;
                break;
            case 2: // Esquerda
                novoX--;
                break;
            case 3: // Direita
                novoX++;
                break;
        }

        // Garante que o inimigo não saia dos limites do mapa
        if (novoX >= minX && novoX <= maxX && novoY >= minY && novoY <= maxY) {
            this.x = novoX;
            this.y = novoY;
        }
    }
}