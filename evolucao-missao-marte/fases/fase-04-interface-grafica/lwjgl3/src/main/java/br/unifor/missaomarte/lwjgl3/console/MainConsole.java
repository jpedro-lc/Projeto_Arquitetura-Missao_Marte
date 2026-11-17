package br.unifor.missaomarte.lwjgl3.console;

import java.util.Scanner;
import br.unifor.missaomarte.lwjgl3.console.JogoConsole;
import br.unifor.missaomarte.lwjgl3.console.MapaRenderer;
import br.unifor.missaomarte.lwjgl3.repository.RankingArquivoRepository;
import br.unifor.missaomarte.repository.RankingRepository;
import br.unifor.missaomarte.service.JogoService;

/** Ponto de composição; o script inicia a JVM no diretório da fase. */
public class MainConsole {
    public static void main(String[] args) {
        RankingRepository ranking = new RankingArquivoRepository("ranking-solid-exercicio10.json");
        JogoService jogo = new JogoService(ranking);
        try (Scanner scanner = new Scanner(System.in)) {
            new JogoConsole(jogo, scanner, System.out, new MapaRenderer(System.out)).executar();
        }
    }
}
