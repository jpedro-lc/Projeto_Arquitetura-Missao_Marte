package br.unifor.missaomarte.remoto;

import br.unifor.missaomarte.model.Dificuldade;
import br.unifor.missaomarte.model.ModoJogo;

/** Uma linha do Top 10 global; setor só no Infinito (0 nos outros modos). */
public record LinhaRanking(int posicao, String nome, int pontuacao, Dificuldade dificuldade, long duracaoSegundos,
                           ModoJogo modo, int setor) {
    public LinhaRanking(int posicao, String nome, int pontuacao, Dificuldade dificuldade, long duracaoSegundos) {
        this(posicao, nome, pontuacao, dificuldade, duracaoSegundos, ModoJogo.CLASSICO, 0);
    }
}
