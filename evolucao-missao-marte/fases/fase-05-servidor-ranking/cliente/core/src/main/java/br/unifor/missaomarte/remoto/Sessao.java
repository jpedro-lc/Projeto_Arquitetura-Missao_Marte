package br.unifor.missaomarte.remoto;

import java.util.Objects;

/** Conta logada. Fica só na memória; o toString nunca mostra o token. */
public record Sessao(String nome, String token) {
    public Sessao {
        Objects.requireNonNull(nome);
        Objects.requireNonNull(token);
    }

    @Override public String toString() { return "Sessao[nome=" + nome + ", token=***]"; }
}
