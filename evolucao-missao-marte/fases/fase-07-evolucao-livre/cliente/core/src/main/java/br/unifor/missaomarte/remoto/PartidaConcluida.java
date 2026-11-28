package br.unifor.missaomarte.remoto;

import java.util.Objects;
import java.util.Optional;
import br.unifor.missaomarte.model.Dificuldade;
import br.unifor.missaomarte.model.EstadoPartida;
import br.unifor.missaomarte.model.ModoJogo;

/**
 * Dados enviados ao servidor quando uma partida termina em vitória ou derrota.
 * Fase 7: também o modo e, no Infinito, o setor alcançado (0 nos outros modos).
 */
public record PartidaConcluida(Dificuldade dificuldade, Resultado resultado, int pontuacao,
                               long duracaoSegundos, int movimentos, ModoJogo modo, int setor) {
    public enum Resultado { VITORIA, DERROTA }

    public PartidaConcluida {
        Objects.requireNonNull(dificuldade);
        Objects.requireNonNull(resultado);
        Objects.requireNonNull(modo);
        if ((modo == ModoJogo.INFINITO) != (setor > 0)) {
            throw new IllegalArgumentException("Setor só no modo Infinito, e a partir de 1");
        }
    }

    /** Contrato das Fases 5 e 6: partida do modo clássico. */
    public PartidaConcluida(Dificuldade dificuldade, Resultado resultado, int pontuacao, long duracaoSegundos, int movimentos) {
        this(dificuldade, resultado, pontuacao, duracaoSegundos, movimentos, ModoJogo.CLASSICO, 0);
    }

    /** Aborto não é enviado (vazio); partida em andamento é erro de uso. */
    public static Optional<PartidaConcluida> de(EstadoPartida estado) {
        return de(estado, ModoJogo.CLASSICO, 0);
    }

    public static Optional<PartidaConcluida> de(EstadoPartida estado, ModoJogo modo, int setor) {
        var resultado = switch (estado.situacao()) {
            case VITORIA -> Resultado.VITORIA;
            case DERROTA -> Resultado.DERROTA;
            case ABORTADA -> null;
            case EM_ANDAMENTO -> throw new IllegalArgumentException("A partida ainda não terminou");
        };
        if (resultado == null) return Optional.empty();
        return Optional.of(new PartidaConcluida(estado.dificuldade(), resultado, estado.pontos(),
                Math.max(0, estado.segundos()), estado.movimentos(), modo, modo == ModoJogo.INFINITO ? setor : 0));
    }
}
