package br.unifor.missaomarte.servidor.nuvem;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import br.unifor.missaomarte.servidor.ApiTeste;

class SaudeApiTest extends ApiTeste {
    @Test void saudeEPublicaERespondeOk() throws Exception {
        mvc.perform(get("/api/saude")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ok"));
    }

    @Test void liberarASaudeNaoLiberaOutrosMetodos() throws Exception {
        mvc.perform(post("/api/saude")).andExpect(status().isUnauthorized());
    }
}
