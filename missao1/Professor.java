package missao1;

public class Professor extends Passageiro {
    public Professor(String nome, int x, int y) {
        super(nome, "missao1.Professor", x, y);
    }

    @Override
    public int getPontuacao() {
        return 10;
    }
}