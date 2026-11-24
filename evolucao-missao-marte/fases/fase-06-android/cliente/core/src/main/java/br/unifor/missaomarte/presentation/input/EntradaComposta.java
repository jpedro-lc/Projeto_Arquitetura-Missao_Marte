package br.unifor.missaomarte.presentation.input;

import java.util.List;
import java.util.Objects;
import br.unifor.missaomarte.model.Acao;

/**
 * Padrão Composite sobre a porta EntradaJogo: permite combinar toque na tela,
 * teclado físico e controle Bluetooth sem alterar o ControladorPartida.
 */
public final class EntradaComposta implements EntradaJogo {
    private final List<EntradaJogo> fontes;

    public EntradaComposta(EntradaJogo... fontes) {
        this(List.of(fontes));
    }

    public EntradaComposta(List<EntradaJogo> fontes) {
        this.fontes = List.copyOf(Objects.requireNonNull(fontes));
        if (this.fontes.isEmpty()) {
            throw new IllegalArgumentException("Ao menos uma fonte de entrada é obrigatória.");
        }
    }

    @Override
    public ComandoEntrada ler() {
        for (EntradaJogo fonte : fontes) {
            ComandoEntrada comando = fonte.ler();
            if (comando.acao() != Acao.VAZIA || comando.reiniciar() || comando.sair()) {
                return comando;
            }
        }
        return ComandoEntrada.nenhum();
    }

    @Override
    public boolean pausaSolicitada() {
        boolean solicitada = false;
        for (EntradaJogo fonte : fontes) {
            // Consome o sinal de todas as fontes para não deixar pausa pendente acumulada.
            if (fonte.pausaSolicitada()) {
                solicitada = true;
            }
        }
        return solicitada;
    }
}
