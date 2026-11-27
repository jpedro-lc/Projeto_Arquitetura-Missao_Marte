package br.unifor.missaomarte.presentation;

import br.unifor.missaomarte.model.EstadoPartida;
import br.unifor.missaomarte.model.EstadoPartida.Situacao;

/** State da plataforma: só interpreta a situação já decidida pelo core, nunca a altera. */
public final class EstadoVisualBase {
    public enum Fase { OCIOSA, ATIVA, POUSO, CONCLUIDA }
    public static final float DURACAO_POUSO = 0.8f;
    private float tempoVitoria = -1;

    public Fase atualizar(EstadoPartida estado, float delta) {
        if (estado.situacao() == Situacao.VITORIA) {
            tempoVitoria = tempoVitoria < 0 ? 0 : tempoVitoria + Math.max(0, delta);
            return tempoVitoria < DURACAO_POUSO ? Fase.POUSO : Fase.CONCLUIDA;
        }
        tempoVitoria = -1;
        boolean todosABordo = estado.passageiros().isEmpty() && !estado.nave().passageiros().isEmpty();
        return estado.situacao() == Situacao.EM_ANDAMENTO && todosABordo ? Fase.ATIVA : Fase.OCIOSA;
    }
}
