package br.unifor.missaomarte.servidor.seguranca;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import br.unifor.missaomarte.servidor.ApiTeste;

class DocumentacaoApiTest extends ApiTeste {
    @Test void descricaoOpenApiEPublicaEDeclaraBearer() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(content().string(containsString("\"bearer\"")))
                .andExpect(content().string(containsString("/api/ranking")));
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }

    @Test void liberarADocumentacaoNaoLiberaAApi() throws Exception {
        mvc.perform(get("/api/ranking")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/partidas/minhas")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/partidas")).andExpect(status().isUnauthorized());
        mvc.perform(post("/v3/api-docs")).andExpect(status().isUnauthorized());
    }
}
