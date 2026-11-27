package br.unifor.missaomarte.presentation.input;

import br.unifor.missaomarte.model.Acao;

/** A tela recebe intenções; futuros controles de toque podem usar este contrato. */
@FunctionalInterface
public interface EntradaJogo {
    ComandoEntrada ler();

    default boolean pausaSolicitada() { return false; }
    /** Movimento pressionado neste instante (tempo real); VAZIA quando nada está pressionado. */
    default Acao direcaoMantida() { return Acao.VAZIA; }
}
