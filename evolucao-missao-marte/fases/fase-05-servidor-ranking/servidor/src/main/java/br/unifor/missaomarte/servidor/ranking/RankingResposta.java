package br.unifor.missaomarte.servidor.ranking;

import java.time.Instant;
import br.unifor.missaomarte.servidor.partida.Dificuldade;

/** Uma linha do Top 10: só nome público e dados da melhor vitória. */
public record RankingResposta(int posicao, String nome, int pontuacao, Dificuldade dificuldade,
                              long duracaoSegundos, Instant registradaEm) { }
