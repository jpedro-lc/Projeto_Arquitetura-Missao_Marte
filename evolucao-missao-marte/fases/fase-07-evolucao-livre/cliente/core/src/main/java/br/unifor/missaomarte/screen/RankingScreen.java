package br.unifor.missaomarte.screen;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import br.unifor.missaomarte.MissaoMarteGame;
import br.unifor.missaomarte.model.Dificuldade;
import br.unifor.missaomarte.model.ModoJogo;
import br.unifor.missaomarte.presentation.EstiloInterface;
import br.unifor.missaomarte.presentation.HudDados;
import br.unifor.missaomarte.presentation.assets.RecursosJogo;
import br.unifor.missaomarte.remoto.LinhaRanking;
import br.unifor.missaomarte.repository.RankingEntry;

/**
 * Fase 7: 9 tabelas (3 modos × 3 dificuldades), cada uma por pontos, escolhidas por abas.
 * Convidado: ranking local, síncrono. Logado: Top 10 global do servidor, assíncrono.
 */
public final class RankingScreen extends TelaCarregavel {
    static final String[] COLUNAS = {"#", "PILOTO", "PONTOS", "TEMPO"};
    static final String[] COLUNAS_INFINITO = {"#", "PILOTO", "PONTOS", "SETOR", "TEMPO"};
    private final Map<ModoJogo, TextButton> abasModo = new EnumMap<>(ModoJogo.class);
    private final Map<Dificuldade, TextButton> abasDificuldade = new EnumMap<>(Dificuldade.class);
    private ModoJogo modo = ModoJogo.TEMPO_REAL;
    private Dificuldade dificuldade = Dificuldade.MEDIO;

    public RankingScreen(MissaoMarteGame game, RecursosJogo recursos) {
        super(game, recursos, game.sessao().logado() ? "RANKING GLOBAL — TOP 10" : "RANKING LOCAL — TOP 10");
        var linhaModo = new Table();
        for (var m : ModoJogo.values()) {
            var aba = botao(NovaPartidaScreen.nomeModo(m), () -> escolher(m, dificuldade));
            aba.setName("aba-" + m.name().toLowerCase());
            abasModo.put(m, aba);
            linhaModo.add(aba).size(220, 46).padRight(8);
        }
        var linhaDificuldade = new Table();
        for (var d : Dificuldade.values()) {
            var aba = botao(HudDados.nomeDificuldade(d), () -> escolher(modo, d));
            aba.setName("aba-" + d.name().toLowerCase());
            abasDificuldade.put(d, aba);
            linhaDificuldade.add(aba).size(180, 42).padRight(8);
        }
        cabecalho.add(linhaModo).padBottom(6).row();
        cabecalho.add(linhaDificuldade).padBottom(12).row();
        stage.addListener(new InputListener() {
            @Override public boolean keyDown(InputEvent event, int tecla) {
                int m = modo.ordinal();
                int d = dificuldade.ordinal();
                switch (tecla) {
                    case Keys.LEFT -> escolher(modo, Dificuldade.values()[(d + 2) % 3]);
                    case Keys.RIGHT -> escolher(modo, Dificuldade.values()[(d + 1) % 3]);
                    case Keys.UP -> escolher(ModoJogo.values()[(m + 2) % 3], dificuldade);
                    case Keys.DOWN, Keys.TAB -> escolher(ModoJogo.values()[(m + 1) % 3], dificuldade);
                    default -> { return false; }
                }
                return true;
            }
        });
        destacar();
        carregar();
    }

    public ModoJogo modo() { return modo; }
    public Dificuldade dificuldade() { return dificuldade; }

    private void escolher(ModoJogo novoModo, Dificuldade novaDificuldade) {
        if (novoModo == modo && novaDificuldade == dificuldade) return;
        modo = novoModo;
        dificuldade = novaDificuldade;
        destacar();
        carregar();
    }

    private void destacar() {
        abasModo.forEach((m, aba) -> aba.getLabel().setColor(m == modo ? EstiloInterface.LARANJA : Color.WHITE));
        abasDificuldade.forEach((d, aba) -> aba.getLabel().setColor(d == dificuldade ? EstiloInterface.LARANJA : Color.WHITE));
    }

    @Override protected void buscar() {
        if (!game.sessao().logado()) { local(); return; }
        var pedido = modo;
        var pedida = dificuldade;
        game.servidor().carregarRanking(game.sessao().sessao(), modo, dificuldade, resposta -> {
            // Troca rápida de aba: descarta a resposta que chegou atrasada.
            if (pedido != modo || pedida != dificuldade) return;
            if (aceitar(resposta)) global(resposta.valor());
        });
    }

    /** Linhas locais deste modo e desta dificuldade, já na ordem de pontos do repositório. */
    static List<RankingEntry> filtrar(List<RankingEntry> entradas, ModoJogo modo, Dificuldade dificuldade) {
        return entradas.stream().filter(e -> e.modo == modo && e.dificuldade == dificuldade).limit(10).toList();
    }

    private void local() {
        var linhas = new ArrayList<String[]>();
        var entradas = filtrar(game.jogo().listarRanking(), modo, dificuldade);
        for (int i = 0; i < entradas.size(); i++) {
            var e = entradas.get(i);
            linhas.add(linha(i + 1, e.name, e.score, e.setor, e.tempoJogo));
        }
        pronto();
        mostrar(linhas.isEmpty() ? estilo.texto("Nenhum resultado neste computador para esta tabela.")
                : tabela(colunas(), linhas, -1));
    }

    private void global(List<LinhaRanking> ranking) {
        String eu = game.sessao().sessao().nome();
        var linhas = new ArrayList<String[]>();
        int minha = -1;
        for (int i = 0; i < ranking.size(); i++) {
            var l = ranking.get(i);
            if (l.nome().equalsIgnoreCase(eu)) minha = i;
            linhas.add(linha(l.posicao(), l.nome(), l.pontuacao(), l.setor(), l.duracaoSegundos()));
        }
        var corpo = new Table();
        corpo.add(linhas.isEmpty() ? estilo.texto("Ainda não há resultados nesta tabela.") : tabela(colunas(), linhas, minha))
                .padBottom(14).row();
        corpo.add(aviso(minha >= 0 ? "Você está em #" + (minha + 1) + " do Top 10."
                : "Você ainda não está no Top 10.", EstiloInterface.CIANO));
        mostrar(corpo);
    }

    private String[] colunas() { return modo == ModoJogo.INFINITO ? COLUNAS_INFINITO : COLUNAS; }

    private String[] linha(int posicao, String nome, int pontos, int setor, long segundos) {
        return modo == ModoJogo.INFINITO
                ? new String[] {Integer.toString(posicao), nome, Integer.toString(pontos), Integer.toString(setor), HudDados.tempo(segundos)}
                : new String[] {Integer.toString(posicao), nome, Integer.toString(pontos), HudDados.tempo(segundos)};
    }
}
