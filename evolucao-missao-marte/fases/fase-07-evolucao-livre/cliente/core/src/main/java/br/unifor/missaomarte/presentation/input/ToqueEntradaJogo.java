package br.unifor.missaomarte.presentation.input;

import java.util.ArrayDeque;
import java.util.Objects;
import br.unifor.missaomarte.model.Acao;

/**
 * Adaptador de entrada por toque e gestos (Port and Adapter).
 * Cada toque ou deslize (swipe) enfileira uma única intenção discreta, preservando o jogo por turnos.
 */
public final class ToqueEntradaJogo implements EntradaJogo {
    public static final float LIMIAR_SWIPE = 28f;
    private final ArrayDeque<ComandoEntrada> fila = new ArrayDeque<>();
    private boolean pausaPendente;

    public void acionar(Acao acao) {
        Objects.requireNonNull(acao);
        if (acao != Acao.VAZIA) {
            fila.addLast(ComandoEntrada.acao(acao));
        }
    }

    public void reiniciar() {
        fila.addLast(new ComandoEntrada(Acao.VAZIA, true, false));
    }

    public void sair() {
        fila.addLast(new ComandoEntrada(Acao.VAZIA, false, true));
    }

    public void solicitarPausa() {
        pausaPendente = true;
    }

    /**
     * Converte um gesto de arraste/deslize (em coordenadas do Stage: +X direita, +Y cima)
     * em um movimento discreto na direção predominante, se superar o limiar mínimo.
     */
    public boolean deslizar(float deltaX, float deltaY) {
        if (Math.hypot(deltaX, deltaY) < LIMIAR_SWIPE) {
            return false;
        }
        if (Math.abs(deltaX) >= Math.abs(deltaY)) {
            acionar(deltaX > 0 ? Acao.DIREITA : Acao.ESQUERDA);
        } else {
            acionar(deltaY > 0 ? Acao.CIMA : Acao.BAIXO);
        }
        return true;
    }

    public void limpar() {
        fila.clear();
        pausaPendente = false;
    }

    @Override
    public ComandoEntrada ler() {
        ComandoEntrada proximo = fila.pollFirst();
        return proximo != null ? proximo : ComandoEntrada.nenhum();
    }

    @Override
    public boolean pausaSolicitada() {
        if (pausaPendente) {
            pausaPendente = false;
            return true;
        }
        return false;
    }
}
