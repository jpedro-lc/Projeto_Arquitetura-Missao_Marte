package br.unifor.missaomarte.presentation;

import java.util.Objects;
import java.util.function.LongSupplier;

/** Adapta o relógio injetável existente, descontando pausas da interface. Java puro. */
public final class RelogioPausavel implements LongSupplier {
    private final LongSupplier origem;
    private long pausas;
    private long inicioPausa;
    private boolean pausado;

    public RelogioPausavel(LongSupplier origem) { this.origem = Objects.requireNonNull(origem); }
    @Override public long getAsLong() { return (pausado ? inicioPausa : origem.getAsLong()) - pausas; }
    public void pausar() {
        if (!pausado) { inicioPausa = origem.getAsLong(); pausado = true; }
    }
    public void retomar() {
        if (pausado) { pausas += origem.getAsLong() - inicioPausa; pausado = false; }
    }
}
