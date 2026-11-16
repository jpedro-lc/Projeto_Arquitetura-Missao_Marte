package br.unifor.missaomarte.presentation.input;

/** A tela recebe intenções; futuros controles de toque podem usar este contrato. */
@FunctionalInterface
public interface EntradaJogo {
    ComandoEntrada ler();

    default boolean pausaSolicitada() { return false; }
}
