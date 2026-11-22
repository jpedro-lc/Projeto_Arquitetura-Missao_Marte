package br.unifor.missaomarte.presentation;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Regras da tela Entrar/Criar conta, sem LibGDX: caracteres aceitos, limites, máscara e validação.
 * Repete a validação do servidor para avisar cedo, mas só o servidor é confiável.
 */
public final class FormularioLogin {
    public enum Modo { ENTRAR, CRIAR_CONTA }
    public enum Campo { NOME, SENHA }
    public static final int NOME_MIN = 3;
    public static final int NOME_MAX = 20;
    public static final int SENHA_MIN = 8;
    public static final int SENHA_MAX_BYTES = 72;

    private final StringBuilder nome = new StringBuilder();
    private final StringBuilder senha = new StringBuilder();
    private Modo modo = Modo.ENTRAR;
    private Campo campo = Campo.NOME;

    /** Retorna true se o caractere foi aceito no campo ativo. */
    public boolean digitar(char c) {
        if (campo == Campo.NOME) {
            if (nome.length() >= NOME_MAX || !caractereDeNome(c)) return false;
            nome.append(c);
            return true;
        }
        if (c < 32 || c == 127) return false;
        String nova = senha.toString() + c;
        if (nova.getBytes(StandardCharsets.UTF_8).length > SENHA_MAX_BYTES) return false;
        senha.append(c);
        return true;
    }

    public void apagar() {
        var alvo = campo == Campo.NOME ? nome : senha;
        if (alvo.length() > 0) alvo.setLength(alvo.length() - 1);
    }

    public void trocarCampo() { campo = campo == Campo.NOME ? Campo.SENHA : Campo.NOME; }
    public void alternarModo() { modo = modo == Modo.ENTRAR ? Modo.CRIAR_CONTA : Modo.ENTRAR; }

    public Modo modo() { return modo; }
    public Campo campo() { return campo; }
    public String nome() { return nome.toString(); }
    public String senha() { return senha.toString(); }
    public String senhaMascarada() { return "*".repeat(senha.length()); }

    /** Mensagem do primeiro problema, ou vazio quando pode enviar. */
    public Optional<String> validar() {
        if (nome.length() < NOME_MIN) return Optional.of("O nome deve ter de 3 a 20 letras, números, _ ou -.");
        if (senha.length() == 0) return Optional.of("Informe a senha.");
        if (modo == Modo.CRIAR_CONTA && senha.length() < SENHA_MIN) {
            return Optional.of("A senha deve ter pelo menos 8 caracteres.");
        }
        return Optional.empty();
    }

    static boolean caractereDeNome(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_' || c == '-';
    }

    @Override public String toString() { return "FormularioLogin[nome=" + nome + ", senha=***, modo=" + modo + "]"; }
}
