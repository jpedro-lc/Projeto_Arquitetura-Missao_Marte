package br.unifor.missaomarte.servidor.usuario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import br.unifor.missaomarte.servidor.ApiTeste;

class UsuarioApiTest extends ApiTeste {
    @Autowired UsuarioRepository usuarios;
    @Autowired PasswordEncoder senhas;

    @Test void cadastroDevolve201SemSenhaEGuardaSoOHash() throws Exception {
        String corpo = cadastrar("Ana_01", SENHA)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Ana_01"))
                .andReturn().getResponse().getContentAsString();
        assertThat(corpo).doesNotContain(SENHA).doesNotContain("senha").doesNotContain("$2a$");
        var salvo = usuarios.findByNomeNormalizado("ana_01").orElseThrow();
        assertThat(salvo.getSenhaHash()).startsWith("$2a$10$").isNotEqualTo(SENHA);
        assertThat(senhas.matches(SENHA, salvo.getSenhaHash())).isTrue();
    }

    @Test void mesmaSenhaGeraHashesDiferentes() throws Exception {
        cadastrar("bia", SENHA).andExpect(status().isCreated());
        cadastrar("caio", SENHA).andExpect(status().isCreated());
        assertThat(usuarios.findByNomeNormalizado("bia").orElseThrow().getSenhaHash())
                .isNotEqualTo(usuarios.findByNomeNormalizado("caio").orElseThrow().getSenhaHash());
    }

    @Test void nomeRepetidoComOutraCaixaDa409() throws Exception {
        cadastrar("Duda", SENHA).andExpect(status().isCreated());
        cadastrar("dUDA", SENHA)
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Este nome já está em uso."));
    }

    @Test void nomeInvalidoDa400ComOCampo() throws Exception {
        for (String nome : new String[] {"ab", "nome com espaco", "a".repeat(21), "ana!"}) {
            cadastrar(nome, SENHA).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.erros[*].campo", hasItem("nome")));
        }
    }

    @Test void senhaCurtaOuAusenteDa400() throws Exception {
        cadastrar("eva", "1234567").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", hasItem("senha")));
        postar("/api/usuarios", "{\"nome\":\"eva\"}").andExpect(status().isBadRequest());
    }

    @Test void senhaAcimaDe72BytesDa400() throws Exception {
        cadastrar("fabi", "a".repeat(72)).andExpect(status().isCreated());
        // 37 caracteres "ç" = 74 bytes em UTF-8: passa no tamanho em caracteres, mas não no limite do BCrypt.
        cadastrar("gil", "ç".repeat(37)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", hasItem("senhaAte72Bytes")));
    }

    @Test void requisicoesNaoExpoemASenhaNoToString() {
        assertThat(new CadastroRequisicao("ana", SENHA).toString()).doesNotContain(SENHA);
    }
}
