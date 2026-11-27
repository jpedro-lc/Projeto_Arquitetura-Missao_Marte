package br.unifor.missaomarte.lwjgl3.console;

import java.io.PrintStream;
import java.util.Objects;
import br.unifor.missaomarte.model.EstadoPartida;

/** Renderiza exclusivamente os dados de leitura recebidos do serviço. */
public class MapaRenderer {
    private final PrintStream saida;
    public MapaRenderer(PrintStream saida) { this.saida = Objects.requireNonNull(saida); }

    public void desenhar(EstadoPartida estado) {
        int minX = -estado.limite(), maxX = estado.limite();
        int minY = -estado.limite(), maxY = estado.limite();
        int score = estado.pontos();
        String pilotoNome = estado.piloto();
        saida.println();
        saida.printf("Mapa da Missão | Pontos: %d | Piloto: %s%n", score, pilotoNome);
        saida.print("    ");
        for (int x = minX; x <= maxX; x++) {
            saida.printf(" %2d", x);
        }
        saida.println();
        saida.print("    ");
        for (int x = minX; x <= maxX; x++) {
            saida.print(" __");
        }
        saida.println();

        for (int y = maxY; y >= minY; y--) {
            saida.printf("%3d|", y);
            for (int x = minX; x <= maxX; x++) {
                char symbol = '.';
                if (estado.nave().x() == x && estado.nave().y() == y) {
                    symbol = '@';
                } else if (x == 0 && y == 0) {
                    symbol = 'L';
                } else {
                    for (var passageiro : estado.passageiros()) {
                        if (passageiro.x() == x && passageiro.y() == y) {
                            if (passageiro.tipo().equals("Engenheiro")) {
                                symbol = 'E';
                            } else if (passageiro.tipo().equals("Astronauta")) {
                                symbol = 'T';
                            } else {
                                symbol = 'P';
                            }
                            break;
                        }
                    }
                    if (symbol == '.') {
                        for (var asteroide : estado.asteroides()) {
                            if (asteroide.x() == x && asteroide.y() == y) {
                                symbol = '#';
                                break;
                            }
                        }
                    }
                    if (symbol == '.') {
                        for (var inimigo : estado.inimigos()) {
                            if (inimigo.x() == x && inimigo.y() == y) {
                                symbol = 'X';
                                break;
                            }
                        }
                    }
                }
                saida.printf(" %2c", symbol);
            }
            saida.println();
        }

        saida.println("Legenda: @=Nave, L=Plataforma, P=Professor, E=Engenheiro, T=Astronauta, #=Asteroide, X=Inimigo, .=Vazio");
        saida.println("Comandos: w/s/a/d (mover), c (embarcar), q (sair)");
    }
}
