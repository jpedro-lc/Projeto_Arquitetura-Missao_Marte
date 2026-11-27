package br.unifor.missaomarte.servidor.auth;

import jakarta.validation.constraints.NotBlank;

/** Corpo de POST /api/auth/login. toString nunca expõe a senha. */
public record LoginRequisicao(
        @NotBlank(message = "Informe o nome.") String nome,
        @NotBlank(message = "Informe a senha.") String senha) {
    @Override public String toString() { return "LoginRequisicao[nome=" + nome + ", senha=***]"; }
}
