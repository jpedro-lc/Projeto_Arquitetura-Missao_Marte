package br.unifor.missaomarte.servidor.erro;

public class NomeJaExisteException extends RuntimeException {
    public NomeJaExisteException() { super("Este nome já está em uso."); }
}
