package br.unifor.missaomarte.remoto;

import java.util.Objects;
import java.util.Optional;
import br.unifor.missaomarte.model.Dificuldade;
import br.unifor.missaomarte.model.EstadoPartida;

/** Dados enviados ao servidor quando uma partida termina em vitória ou derrota. */
public record PartidaConcluida(Dificuldade dificuldade, Resultado resultado, int pontuacao,
                               long duracaoSegundos, int movimentos) {
    public enum Resultado { VITORIA, DERROTA }

    public PartidaConcluida {
        Objects.requireNonNull(dificuldade);
        Objects.requireNonNull(resultado);
    }

    /** Aborto não é enviado (vazio); partida em andamento é erro de uso. */
    public static Optional<PartidaConcluida> de(EstadoPartida estado) {
        var resultado = switch (estado.situacao()) {
            case VITORIA -> Resultado.VITORIA;
            case DERROTA -> Resultado.DERROTA;
            case ABORTADA -> null;
            case EM_ANDAMENTO -> throw new IllegalArgumentException("A partida ainda não terminou");
        };
        if (resultado == null) return Optional.empty();
        return Optional.of(new PartidaConcluida(estado.dificuldade(), resultado, estado.pontos(),
                Math.max(0, estado.segundos()), estado.movimentos()));
    }
}
