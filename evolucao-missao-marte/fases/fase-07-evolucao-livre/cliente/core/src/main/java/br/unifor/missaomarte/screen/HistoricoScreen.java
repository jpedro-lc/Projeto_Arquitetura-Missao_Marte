package br.unifor.missaomarte.screen;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import br.unifor.missaomarte.MissaoMarteGame;
import br.unifor.missaomarte.presentation.HudDados;
import br.unifor.missaomarte.presentation.assets.RecursosJogo;
import br.unifor.missaomarte.remoto.PartidaConcluida;

/** Últimas 10 partidas do jogador logado, carregadas do servidor. */
public final class HistoricoScreen extends TelaCarregavel {
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    static final String[] COLUNAS = {"DATA E HORA", "RESULTADO", "DIFICULDADE", "PONTOS", "TEMPO", "MOVIMENTOS"};

    public HistoricoScreen(MissaoMarteGame game, RecursosJogo recursos) {
        super(game, recursos, "MEU HISTÓRICO — ÚLTIMAS 10");
        carregar();
    }

    @Override protected void buscar() {
        game.servidor().carregarHistorico(game.sessao().sessao(), resposta -> {
            if (!aceitar(resposta)) return;
            var linhas = new ArrayList<String[]>();
            for (var p : resposta.valor()) {
                linhas.add(new String[] {formatarData(p.registradaEm(), ZoneId.systemDefault()),
                        p.resultado() == PartidaConcluida.Resultado.VITORIA ? "VITÓRIA" : "DERROTA",
                        HudDados.nomeDificuldade(p.dificuldade()), Integer.toString(p.pontuacao()),
                        HudDados.tempo(p.duracaoSegundos()), Integer.toString(p.movimentos())});
            }
            mostrar(linhas.isEmpty() ? estilo.texto("Você ainda não terminou nenhuma partida com esta conta.")
                    : tabela(COLUNAS, linhas, -1));
        });
    }

    /** O servidor guarda UTC; o jogador vê no fuso do computador. */
    public static String formatarData(Instant instante, ZoneId fuso) {
        return DATA.format(instante.atZone(fuso));
    }
}
