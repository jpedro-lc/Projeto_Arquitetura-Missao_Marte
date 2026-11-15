package br.unifor.missaomarte.lwjgl3.console;

import java.io.PrintStream;
import java.util.Locale;
import java.util.Objects;
import java.util.Scanner;
import br.unifor.missaomarte.model.Acao;
import br.unifor.missaomarte.model.Dificuldade;
import br.unifor.missaomarte.model.EstadoPartida;
import br.unifor.missaomarte.model.EstadoPartida.Situacao;
import br.unifor.missaomarte.model.ResultadoAcao.Evento;
import br.unifor.missaomarte.service.JogoService;

/** Adaptador do terminal: traduz teclas e apresenta fatos devolvidos pelo serviço. */
public class JogoConsole {
    private final JogoService jogo;
    private final Scanner scanner;
    private final PrintStream saida;
    private final MapaRenderer mapa;

    public JogoConsole(JogoService jogo, Scanner scanner, PrintStream saida, MapaRenderer mapa) {
        this.jogo = Objects.requireNonNull(jogo);
        this.scanner = Objects.requireNonNull(scanner);
        this.saida = Objects.requireNonNull(saida);
        this.mapa = Objects.requireNonNull(mapa);
    }

    public void executar() {
        saida.println("================================================================");
        saida.println("             MISSÃO MARTE UNIFOR - VERSÃO SOLID                  ");
        saida.println("================================================================");
        saida.println("  Pilote sua nave, salve os passageiros e desvie dos perigos!   ");
        saida.println("================================================================\n");
        while (true) {
            saida.println("\n--- MENU PRINCIPAL ---");
            saida.println("1. Iniciar Nova Missão");
            saida.println("2. Visualizar Ranking Top 5");
            saida.println("3. Resetar Ranking");
            saida.println("4. Sair");
            saida.println("----------------------");
            String opcao = lerLinha("Escolha uma opção: ", "1");
            if (opcao == null) return;
            switch (opcao.trim()) {
                case "1" -> { if (!jogarPartida()) return; }
                case "2" -> exibirRanking();
                case "3" -> {
                    jogo.limparRanking();
                    saida.println("Histórico de ranking removido.");
                }
                case "4" -> {
                    saida.println("\nObrigado por jogar a Missão Marte Unifor!");
                    return;
                }
                default -> saida.println("Opção inválida. Tente novamente.");
            }
        }
    }

    /** Retorna false se a entrada terminou, inclusive durante a configuração. */
    private boolean jogarPartida() {
        String piloto = lerLinha("\nDigite o nome do piloto: ", "Piloto Anônimo");
        if (piloto == null) return false;
        String valor = lerLinha("Escolha a Dificuldade (facil/medio/dificil): ", "medio");
        if (valor == null) return false;
        Dificuldade dificuldade = Dificuldade.deString(valor.trim());
        String tamanho = lerLinha("Tamanho do mapa (ex: 5): ", "5");
        if (tamanho == null) return false;
        int limite = 5;
        try {
            int numero = Integer.parseInt(tamanho);
            if (numero > 0) limite = numero;
        } catch (NumberFormatException e) {
            saida.println("Entrada inválida, usando tamanho padrão (5).");
        }
        saida.println("\nIniciando missão na dificuldade " + dificuldade + "...");
        if (lerLinha("Pressione Enter para decolar!", "") == null) return false;
        EstadoPartida estado = jogo.iniciar(piloto, dificuldade, limite);
        while (estado.situacao() == Situacao.EM_ANDAMENTO) {
            mapa.desenhar(estado);
            var nave = estado.nave();
            int aBordo = nave.passageiros().size();
            int restantes = estado.passageiros().size();
            saida.printf("Nave em (%d,%d) | Pontos: %d | Vidas: %d | A bordo: %d/%d | Restantes no mapa: %d | Total: %d%n",
                    nave.x(), nave.y(), estado.pontos(), nave.vidas(), aBordo, nave.capacidade(), restantes, aBordo + restantes);
            String entrada = lerLinha("Comando (w/s/a/d/c/q): ", "");
            if (entrada == null) return false;
            var resultado = jogo.executar(traduzir(entrada));
            estado = resultado.estado();
            for (Evento evento : resultado.eventos()) exibirEvento(evento);
            if (estado.situacao() == Situacao.VITORIA) exibirEstatisticas(estado);
        }
        return true;
    }

    private Acao traduzir(String entrada) {
        String comando = entrada.trim().toLowerCase(Locale.ROOT);
        if (comando.isEmpty()) return Acao.VAZIA;
        return switch (comando.charAt(0)) {
            case 'w' -> Acao.CIMA;
            case 's' -> Acao.BAIXO;
            case 'a' -> Acao.ESQUERDA;
            case 'd' -> Acao.DIREITA;
            case 'c' -> Acao.EMBARCAR;
            case 'q' -> Acao.ABORTAR;
            default -> Acao.INVALIDA;
        };
    }

    private void exibirEvento(Evento evento) {
        switch (evento.tipo()) {
            case SEM_PASSAGEIRO -> saida.println("Nenhum passageiro nesta posição.");
            case EMBARCOU -> saida.printf("Passageiro %s embarcado com sucesso! +%d pontos!%n", evento.nome(), evento.valor());
            case NAVE_CHEIA -> saida.println("Nave cheia! Não há espaço para mais passageiros.");
            case COMANDO_INVALIDO -> saida.println("Comando inválido.");
            case ABORTOU -> saida.println("Missão abortada pelo piloto.");
            case COLISAO -> saida.printf("Alerta! Colisão detectada! Vidas restantes: %d%n", evento.valor());
            case NAVE_DESTRUIDA -> saida.println("GAME OVER! A nave foi destruída.");
            case SEM_PONTOS -> saida.println("Combustível/Pontuação zerada! Missão perdida.");
            case RETORNAR_BASE -> saida.println("✨ ALERTA: Todos os passageiros resgatados! Retorne para a Plataforma de Pouso 'L' em (0,0) para completar a missão.");
            case VITORIA -> {
                saida.println("\n================================================================");
                saida.println("🚀 DECOLAGEM AUTORIZADA! Nave acoplada à plataforma em (0,0).");
                saida.println("Retornando à órbita marciana com todos os passageiros. Missão cumprida!");
                saida.println("================================================================\n");
            }
        }
    }

    private void exibirRanking() {
        var ranking = jogo.listarRanking();
        if (ranking.isEmpty()) {
            saida.println("Nenhum registro de ranking ainda.");
            return;
        }
        saida.println("\n=== TOP 5 DO RANKING ===");
        for (int i = 0; i < Math.min(5, ranking.size()); i++) {
            var entry = ranking.get(i);
            saida.printf("%d. %s | Pontos: %d | Dif.: %s | Passageiros: %d | %s | Tempo: %ds%n",
                    i + 1, entry.name, entry.score, entry.dificuldade, entry.passageirosColetados, entry.dataHora, entry.tempoJogo);
        }
    }

    private void exibirEstatisticas(EstadoPartida estado) {
        saida.println("\n=== ESTATÍSTICAS DA MISSÃO ===");
        saida.printf("Pontuação final: %d%n", estado.pontos());
        saida.printf("Movimentos realizados: %d%n", estado.movimentos());
        saida.printf("Tempo de missão: %d segundos%n", estado.segundos());
        saida.printf("Passageiros resgatados: %d%n", estado.nave().passageiros().size());
    }

    private String lerLinha(String mensagem, String padrao) {
        saida.print(mensagem);
        saida.flush();
        if (!scanner.hasNextLine()) return null;
        String entrada = scanner.nextLine();
        return entrada.isBlank() ? padrao : entrada;
    }
}
