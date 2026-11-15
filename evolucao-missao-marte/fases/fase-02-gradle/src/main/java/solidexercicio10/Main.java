package solidexercicio10;

import java.util.Scanner;
import solidexercicio10.presentation.JogoConsole;
import solidexercicio10.presentation.MapaRenderer;
import solidexercicio10.repository.RankingArquivoRepository;
import solidexercicio10.repository.RankingRepository;
import solidexercicio10.service.JogoService;

/** Ponto de composição; o script inicia a JVM no diretório da fase. */
public class Main {
    public static void main(String[] args) {
        RankingRepository ranking = new RankingArquivoRepository("ranking-solid-exercicio10.json");
        JogoService jogo = new JogoService(ranking);
        try (Scanner scanner = new Scanner(System.in)) {
            new JogoConsole(jogo, scanner, System.out, new MapaRenderer(System.out)).executar();
        }
    }
}
