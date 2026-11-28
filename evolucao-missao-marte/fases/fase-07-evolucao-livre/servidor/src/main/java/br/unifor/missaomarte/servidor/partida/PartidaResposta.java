package br.unifor.missaomarte.servidor.partida;

import java.time.Instant;

public record PartidaResposta(Long id, Dificuldade dificuldade, Resultado resultado, int pontuacao,
                              long duracaoSegundos, int movimentos, Instant registradaEm,
                              ModoJogo modo, Integer setor) {
    static PartidaResposta de(Partida p) {
        return new PartidaResposta(p.getId(), p.getDificuldade(), p.getResultado(), p.getPontuacao(),
                p.getDuracaoSegundos(), p.getMovimentos(), p.getRegistradaEm(), p.getModo(), p.getSetor());
    }
}
