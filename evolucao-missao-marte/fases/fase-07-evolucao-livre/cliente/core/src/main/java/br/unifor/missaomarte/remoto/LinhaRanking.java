package br.unifor.missaomarte.remoto;

import br.unifor.missaomarte.model.Dificuldade;

/** Uma linha do Top 10 global. */
public record LinhaRanking(int posicao, String nome, int pontuacao, Dificuldade dificuldade, long duracaoSegundos) { }
