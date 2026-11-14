import java.util.ArrayList;
import java.util.List;

public class Nave {
    private String id;
    private int x;
    private int y;
    private int capacidade;
    private List<Passageiro> passageiros = new ArrayList<>();
    private int vidas = 3;

    public Nave(String id, int capacidade) {
        this.id = id;
        this.capacidade = capacidade;
        this.x = 0;
        this.y = 0;
    }

    public String getId() { return id; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getCapacidade() { return capacidade; }
    public List<Passageiro> getPassageiros() { return passageiros; }

    public void moveUp() { y--; }
    public void moveDown() { y++; }
    public void moveLeft() { x--; }
    public void moveRight() { x++; }

    public boolean embarcar(Passageiro p) {
        if (passageiros.size() < capacidade) {
            passageiros.add(p);
            return true;
        }
        return false;
    }

    public int getVidas() {
        return vidas;
    }

    public void perderVida() {
        vidas--;
    }

    /**
     * Move a nave de acordo com o comando (w/s/a/d), respeitando os limites da grade.
     */
    public void moverComLimites(char comando, int minX, int maxX, int minY, int maxY) {
        switch (comando) {
            case 'w':
                if (y > minY) {
                    moveUp();
                }
                break;
            case 's':
                if (y < maxY) {
                    moveDown();
                }
                break;
            case 'a':
                if (x > minX) {
                    moveLeft();
                }
                break;
            case 'd':
                if (x < maxX) {
                    moveRight();
                }
                break;
        }
    }
}