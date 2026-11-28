package br.unifor.missaomarte.remoto;

import java.time.Instant;
import br.unifor.missaomarte.model.Dificuldade;
import br.unifor.missaomarte.model.ModoJogo;

/** Uma partida como o servidor a guardou (histórico). */
public record PartidaRegistrada(long id, Dificuldade dificuldade, PartidaConcluida.Resultado resultado,
                                int pontuacao, long duracaoSegundos, int movimentos, Instant registradaEm,
                                ModoJogo modo, int setor) {
    public PartidaRegistrada(long id, Dificuldade dificuldade, PartidaConcluida.Resultado resultado,
                             int pontuacao, long duracaoSegundos, int movimentos, Instant registradaEm) {
        this(id, dificuldade, resultado, pontuacao, duracaoSegundos, movimentos, registradaEm, ModoJogo.CLASSICO, 0);
    }
}
