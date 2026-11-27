package br.unifor.missaomarte.presentation.input;

import java.util.Objects;
import br.unifor.missaomarte.model.Acao;

/** Intenção de apresentação: reiniciar/sair não são ações das regras da missão. */
public record ComandoEntrada(Acao acao, boolean reiniciar, boolean sair) {
    public ComandoEntrada {
        Objects.requireNonNull(acao);
    }

    public static ComandoEntrada acao(Acao acao) {
        return new ComandoEntrada(acao, false, false);
    }

    public static ComandoEntrada nenhum() {
        return acao(Acao.VAZIA);
    }
}
