package br.unifor.missaomarte.servidor.ranking;

import java.time.Instant;
import br.unifor.missaomarte.servidor.partida.Dificuldade;
import br.unifor.missaomarte.servidor.partida.ModoJogo;

/** Uma linha do Top 10: só nome público e dados da melhor partida (no Infinito, com o setor). */
public record RankingResposta(int posicao, String nome, int pontuacao, Dificuldade dificuldade,
                              long duracaoSegundos, Instant registradaEm, ModoJogo modo, Integer setor) { }
