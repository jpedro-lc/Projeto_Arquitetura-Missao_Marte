package br.unifor.missaomarte.service;

import java.util.List;
import java.util.Objects;
import br.unifor.missaomarte.model.Acao;
import br.unifor.missaomarte.model.Dificuldade;
import br.unifor.missaomarte.model.EstadoPartida;
import br.unifor.missaomarte.model.ModoJogo;
import br.unifor.missaomarte.model.ResultadoAcao;

/** Adapter: o JogoService das Fases 1 a 6, sem alteração, atrás do contrato de modos. O tempo não move nada. */
public final class RegrasClassicas implements RegrasDePartida {
    private final JogoService jogo;

    public RegrasClassicas(JogoService jogo) { this.jogo = Objects.requireNonNull(jogo); }

    @Override public ModoJogo modo() { return ModoJogo.CLASSICO; }
    @Override public EstadoPartida iniciar(String piloto, Dificuldade dificuldade, int limite) {
        return jogo.iniciar(piloto, dificuldade, limite);
    }
    @Override public ResultadoAcao executar(Acao acao) { return jogo.executar(acao); }
    @Override public ResultadoAcao avancar(float segundos, Acao direcaoMantida) {
        return new ResultadoAcao(jogo.consultarEstado(), List.of());
    }
    @Override public EstadoPartida consultarEstado() { return jogo.consultarEstado(); }
}
