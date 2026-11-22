package br.unifor.missaomarte.remoto;

import java.time.Instant;
import br.unifor.missaomarte.model.Dificuldade;

/** Uma partida como o servidor a guardou (histórico). */
public record PartidaRegistrada(long id, Dificuldade dificuldade, PartidaConcluida.Resultado resultado,
                                int pontuacao, long duracaoSegundos, int movimentos, Instant registradaEm) { }
