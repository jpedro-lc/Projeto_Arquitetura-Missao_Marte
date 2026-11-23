package br.unifor.missaomarte.screen;

import java.util.ArrayList;
import java.util.List;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import br.unifor.missaomarte.MissaoMarteGame;
import br.unifor.missaomarte.presentation.EstiloInterface;
import br.unifor.missaomarte.presentation.HudDados;
import br.unifor.missaomarte.presentation.assets.RecursosJogo;
import br.unifor.missaomarte.remoto.LinhaRanking;

/** Convidado: ranking local, síncrono (como na Fase 4). Logado: Top 10 global, assíncrono. */
public final class RankingScreen extends TelaCarregavel {
    static final String[] COLUNAS = {"#", "PILOTO", "PONTOS", "DIFICULDADE", "TEMPO"};

    public RankingScreen(MissaoMarteGame game, RecursosJogo recursos) {
        super(game, recursos, game.sessao().logado() ? "RANKING GLOBAL — TOP 10" : "RANKING LOCAL — TOP 10");
        carregar();
    }

    @Override protected void buscar() {
        if (!game.sessao().logado()) { local(); return; }
        game.servidor().carregarRanking(game.sessao().sessao(), resposta -> {
            if (aceitar(resposta)) global(resposta.valor());
        });
    }

    private void local() {
        var linhas = new ArrayList<String[]>();
        var entradas = game.jogo().listarRanking();
        for (int i = 0; i < Math.min(10, entradas.size()); i++) {
            var e = entradas.get(i);
            linhas.add(new String[] {Integer.toString(i + 1), e.name, Integer.toString(e.score),
                    e.dificuldade == null ? "-" : HudDados.nomeDificuldade(e.dificuldade), HudDados.tempo(e.tempoJogo)});
        }
        pronto();
        mostrar(linhas.isEmpty() ? estilo.texto("Nenhuma vitória registrada neste computador.") : tabela(COLUNAS, linhas, -1));
    }

    private void global(List<LinhaRanking> ranking) {
        String eu = game.sessao().sessao().nome();
        var linhas = new ArrayList<String[]>();
        int minha = -1;
        for (int i = 0; i < ranking.size(); i++) {
            var l = ranking.get(i);
            if (l.nome().equalsIgnoreCase(eu)) minha = i;
            linhas.add(new String[] {Integer.toString(l.posicao()), l.nome(), Integer.toString(l.pontuacao()),
                    HudDados.nomeDificuldade(l.dificuldade()), HudDados.tempo(l.duracaoSegundos())});
        }
        var corpo = new Table();
        corpo.add(linhas.isEmpty() ? estilo.texto("Ainda não há vitórias no ranking global.") : tabela(COLUNAS, linhas, minha))
                .padBottom(14).row();
        corpo.add(aviso(minha >= 0 ? "Você está em #" + (minha + 1) + " do Top 10."
                : "Você ainda não está no Top 10.", EstiloInterface.CIANO));
        mostrar(corpo);
    }
}
