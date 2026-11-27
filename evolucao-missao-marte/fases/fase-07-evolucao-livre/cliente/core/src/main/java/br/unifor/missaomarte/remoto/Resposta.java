package br.unifor.missaomarte.remoto;

import java.util.Objects;

/** Valor OU mensagem de erro já pronta para o jogador; "não autorizado" indica sessão expirada. */
public final class Resposta<T> {
    private final T valor;
    private final String erro;
    private final boolean naoAutorizado;

    private Resposta(T valor, String erro, boolean naoAutorizado) {
        this.valor = valor;
        this.erro = erro;
        this.naoAutorizado = naoAutorizado;
    }

    public static <T> Resposta<T> sucesso(T valor) { return new Resposta<>(valor, null, false); }
    public static <T> Resposta<T> erro(String mensagem) { return new Resposta<>(null, Objects.requireNonNull(mensagem), false); }
    public static <T> Resposta<T> naoAutorizado(String mensagem) { return new Resposta<>(null, Objects.requireNonNull(mensagem), true); }

    public boolean ok() { return erro == null; }
    public T valor() {
        if (!ok()) throw new IllegalStateException("Resposta com erro não tem valor");
        return valor;
    }
    public String erro() { return erro; }
    public boolean naoAutorizado() { return naoAutorizado; }
}
