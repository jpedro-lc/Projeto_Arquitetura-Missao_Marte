package br.unifor.missaomarte.repository;

import br.unifor.missaomarte.model.Dificuldade;
import br.unifor.missaomarte.model.ModoJogo;

public class RankingEntry {
    public final String name;
    public final int score;
    public final Dificuldade dificuldade;
    public final int passageirosColetados;
    public final String dataHora;
    public final long tempoJogo;
    /** Fase 7: entradas antigas (sem modo no arquivo) são do clássico; setor só no Infinito. */
    public final ModoJogo modo;
    public final int setor;

    public RankingEntry(String name, int score, Dificuldade dificuldade, int passageirosColetados, String dataHora, long tempoJogo) {
        this(name, score, dificuldade, passageirosColetados, dataHora, tempoJogo, ModoJogo.CLASSICO, 0);
    }

    public RankingEntry(String name, int score, Dificuldade dificuldade, int passageirosColetados, String dataHora,
                        long tempoJogo, ModoJogo modo, int setor) {
        this.modo = modo == null ? ModoJogo.CLASSICO : modo;
        this.setor = setor;
        this.name = name;
        this.score = score;
        this.dificuldade = dificuldade;
        this.passageirosColetados = passageirosColetados;
        this.dataHora = dataHora;
        this.tempoJogo = tempoJogo;
    }
}