package br.unifor.missaomarte.remoto;

/** Callback de uma chamada ao servidor. */
@FunctionalInterface
public interface Retorno<T> {
    void receber(Resposta<T> resposta);
}
