package solidexercicio10.model;

import java.util.List;

/** Fotografia imutável: a apresentação não recebe entidades mutáveis da missão. */
public record EstadoPartida(String piloto, Dificuldade dificuldade, int limite,
                            int pontos, int movimentos, long segundos, Situacao situacao,
                            NaveVista nave, List<PassageiroVista> passageiros,
                            List<Posicao> asteroides, List<Posicao> inimigos) {
    public enum Situacao { EM_ANDAMENTO, VITORIA, DERROTA, ABORTADA }
    public record Posicao(int x, int y) { }
    public record PassageiroVista(String nome, String tipo, int x, int y, int pontuacao) { }
    public record NaveVista(int x, int y, int vidas, int capacidade, List<PassageiroVista> passageiros) {
        public NaveVista { passageiros = List.copyOf(passageiros); }
    }

    public EstadoPartida {
        passageiros = List.copyOf(passageiros);
        asteroides = List.copyOf(asteroides);
        inimigos = List.copyOf(inimigos);
    }
}
