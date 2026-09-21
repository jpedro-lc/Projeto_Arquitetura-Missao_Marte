package missao1;

public class Engenheiro extends Passageiro {
    public Engenheiro(String nome, int x, int y) {
        super(nome, "missao1.Engenheiro", x, y);
    }

    @Override
    public int getPontuacao() {
        return 15;
    }
}