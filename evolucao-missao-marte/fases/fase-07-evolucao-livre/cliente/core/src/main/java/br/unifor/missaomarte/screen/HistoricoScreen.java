package br.unifor.missaomarte.screen;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import br.unifor.missaomarte.MissaoMarteGame;
import br.unifor.missaomarte.model.ModoJogo;
import br.unifor.missaomarte.presentation.HudDados;
import br.unifor.missaomarte.presentation.assets.RecursosJogo;
import br.unifor.missaomarte.remoto.PartidaConcluida;

/** Últimas 10 partidas do jogador logado, carregadas do servidor. */
public final class HistoricoScreen extends TelaCarregavel {
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    // Fase 7: MODO no lugar de MOVIMENTOS, para a tabela continuar cabendo no painel.
    static final String[] COLUNAS = {"DATA E HORA", "MODO", "RESULTADO", "DIFICULDADE", "PONTOS", "TEMPO"};

    public HistoricoScreen(MissaoMarteGame game, RecursosJogo recursos) {
        super(game, recursos, "MEU HISTÓRICO — ÚLTIMAS 10");
        carregar();
    }

    @Override protected void buscar() {
        game.servidor().carregarHistorico(game.sessao().sessao(), resposta -> {
            if (!aceitar(resposta)) return;
            var linhas = new ArrayList<String[]>();
            for (var p : resposta.valor()) {
                linhas.add(new String[] {formatarData(p.registradaEm(), ZoneId.systemDefault()), modo(p.modo(), p.setor()),
                        p.resultado() == PartidaConcluida.Resultado.VITORIA ? "VITÓRIA" : "DERROTA",
                        HudDados.nomeDificuldade(p.dificuldade()), Integer.toString(p.pontuacao()),
                        HudDados.tempo(p.duracaoSegundos())});
            }
            mostrar(linhas.isEmpty() ? estilo.texto("Você ainda não terminou nenhuma partida com esta conta.")
                    : tabela(COLUNAS, linhas, -1));
        });
    }

    /** "INFINITO S4" mostra o setor alcançado; os outros modos só o nome. */
    static String modo(ModoJogo modo, int setor) {
        return modo == ModoJogo.INFINITO ? "INFINITO S" + setor : NovaPartidaScreen.nomeModo(modo);
    }

    /** O servidor guarda UTC; o jogador vê no fuso do computador. */
    public static String formatarData(Instant instante, ZoneId fuso) {
        return DATA.format(instante.atZone(fuso));
    }
}
