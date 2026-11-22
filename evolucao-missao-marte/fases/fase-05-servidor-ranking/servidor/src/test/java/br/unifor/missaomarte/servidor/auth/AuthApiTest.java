package br.unifor.missaomarte.servidor.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import br.unifor.missaomarte.servidor.ApiTeste;

class AuthApiTest extends ApiTeste {
    @Autowired JwtEncoder codificador;
    @Autowired JwtDecoder decodificador;

    @Test void loginDevolveTokenBearerSemSenha() throws Exception {
        cadastrar("Ana", SENHA);
        String corpo = login("ana", SENHA)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.expiraEm").isString())
                .andReturn().getResponse().getContentAsString();
        assertThat(corpo).doesNotContain(SENHA);
        String token = com.jayway.jsonpath.JsonPath.read(corpo, "$.token");
        var jwt = decodificador.decode(token);
        assertThat(jwt.getSubject()).matches("\\d+");
        assertThat(jwt.getClaims().toString()).doesNotContain(SENHA);
        assertThat(jwt.getExpiresAt()).isBetween(Instant.now().plusSeconds(7100), Instant.now().plusSeconds(7300));
    }

    @Test void senhaErradaEUsuarioInexistenteTemAMesmaResposta() throws Exception {
        cadastrar("bia", SENHA);
        String senhaErrada = login("bia", "outra-senha-9").andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();
        String inexistente = login("ninguem", SENHA).andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();
        assertThat(senhaErrada).isEqualTo(inexistente).contains("Usuário ou senha inválidos.");
    }

    @Test void rotaProtegidaSemTokenDa401() throws Exception {
        mvc.perform(get("/api/ranking")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/partidas/minhas")).andExpect(status().isUnauthorized());
    }

    @Test void tokenInvalidoDa401() throws Exception {
        mvc.perform(get("/api/ranking").header("Authorization", "Bearer abc.def.ghi")).andExpect(status().isUnauthorized());
        String valido = token("caio");
        String adulterado = valido.substring(0, valido.length() - 2) + (valido.endsWith("AA") ? "BB" : "AA");
        mvc.perform(get("/api/ranking").header("Authorization", "Bearer " + adulterado)).andExpect(status().isUnauthorized());
    }

    @Test void tokenExpiradoDa401() throws Exception {
        String valido = token("duda");
        String sub = decodificador.decode(valido).getSubject();
        // Além da tolerância padrão de 60 s do validador de tempo.
        var claims = JwtClaimsSet.builder().subject(sub)
                .issuedAt(Instant.now().minusSeconds(3 * 3600)).expiresAt(Instant.now().minusSeconds(3600)).build();
        String expirado = codificador.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
        mvc.perform(get("/api/ranking").header("Authorization", "Bearer " + expirado)).andExpect(status().isUnauthorized());
    }

    @Test void tokenValidoDa200() throws Exception {
        mvc.perform(get("/api/ranking").header("Authorization", "Bearer " + token("eva"))).andExpect(status().isOk());
    }

    @Test void loginNaoExpoeASenhaNoToString() {
        assertThat(new LoginRequisicao("ana", SENHA).toString()).doesNotContain(SENHA);
    }
}
