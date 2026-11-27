package br.unifor.missaomarte.servidor.erro;

/** Mesma mensagem para usuário inexistente e senha errada: não revela quais nomes existem. */
public class CredenciaisInvalidasException extends RuntimeException {
    public CredenciaisInvalidasException() { super("Usuário ou senha inválidos."); }
}
