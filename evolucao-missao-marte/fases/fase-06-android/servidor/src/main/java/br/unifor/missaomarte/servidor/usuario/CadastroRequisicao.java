package br.unifor.missaomarte.servidor.usuario;

import java.nio.charset.StandardCharsets;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Corpo de POST /api/usuarios. */
public record CadastroRequisicao(
        @NotNull(message = "Informe o nome.")
        @Pattern(regexp = "[A-Za-z0-9_-]{3,20}", message = "O nome deve ter de 3 a 20 letras, números, _ ou -.")
        String nome,

        @NotNull(message = "Informe a senha.")
        @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres.")
        String senha) {

    /** O BCrypt lê só os 72 primeiros bytes; sem esta regra, o resto seria ignorado em silêncio. */
    @AssertTrue(message = "A senha pode ter no máximo 72 bytes.")
    public boolean isSenhaAte72Bytes() {
        return senha == null || senha.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    @Override public String toString() { return "CadastroRequisicao[nome=" + nome + ", senha=***]"; }
}
