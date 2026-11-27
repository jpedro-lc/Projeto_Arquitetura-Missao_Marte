package br.unifor.missaomarte.servidor.partida;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import br.unifor.missaomarte.servidor.ApiTeste;

class PartidaApiTest extends ApiTeste {
    @Test void registraPartidaDoDonoDoToken() throws Exception {
        postar("/api/partidas", partida("VITORIA", 33, 42), token("ana"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.resultado").value("VITORIA"))
                .andExpect(jsonPath("$.pontuacao").value(33))
                .andExpect(jsonPath("$.registradaEm").isString());
    }

    @Test void usuarioInformadoNoCorpoEIgnorado() throws Exception {
        String ana = token("ana");
        String bia = token("bia");
        String comUsuario = "{\"usuario\":\"bia\",\"usuarioId\":1," + partida("VITORIA", 50, 10).substring(1);
        postar("/api/partidas", comUsuario, ana).andExpect(status().isCreated());
        mvc.perform(get("/api/partidas/minhas").header("Authorization", "Bearer " + bia))
                .andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/api/partidas/minhas").header("Authorization", "Bearer " + ana))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test void enumInvalidoDa400() throws Exception {
        String token = token("ana");
        postar("/api/partidas", partida("ABORTADA", 10, 10), token).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Corpo da requisição inválido."));
        postar("/api/partidas", partida("VITORIA", 10, 10).replace("MEDIO", "IMPOSSIVEL"), token)
                .andExpect(status().isBadRequest());
    }

    @Test void valoresForaDaFaixaOuAusentesDao400() throws Exception {
        String token = token("ana");
        postar("/api/partidas", partida("VITORIA", 100_001, 10), token).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", hasItem("pontuacao")));
        postar("/api/partidas", partida("VITORIA", 10, 86_401), token).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", hasItem("duracaoSegundos")));
        postar("/api/partidas", partida("VITORIA", 10, -1), token).andExpect(status().isBadRequest());
        postar("/api/partidas", partida("VITORIA", 10, 10).replace("12", "-1"), token).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", hasItem("movimentos")));
        postar("/api/partidas", "{\"dificuldade\":\"MEDIO\",\"resultado\":\"DERROTA\",\"duracaoSegundos\":5,\"movimentos\":1}", token)
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erros[*].campo", hasItem("pontuacao")));
        postar("/api/partidas", "{nao e json", token).andExpect(status().isBadRequest());
    }

    @Test void historicoSoDoDonoDoMaisRecenteParaOMaisAntigoEPaginado() throws Exception {
        String ana = token("ana");
        registrar(ana, "VITORIA", 10, 30);
        registrar(ana, "DERROTA", -3, 20);
        registrar(ana, "VITORIA", 25, 40);
        registrar(token("bia"), "VITORIA", 99, 5);
        mvc.perform(get("/api/partidas/minhas").header("Authorization", "Bearer " + ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].pontuacao").value(25))
                .andExpect(jsonPath("$[1].resultado").value("DERROTA"))
                .andExpect(jsonPath("$[2].pontuacao").value(10));
        mvc.perform(get("/api/partidas/minhas?pagina=1&tamanho=2").header("Authorization", "Bearer " + ana))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].pontuacao").value(10));
    }

    @Test void tamanhoDePaginaAcimaDe50Da400() throws Exception {
        String ana = token("ana");
        mvc.perform(get("/api/partidas/minhas?tamanho=51").header("Authorization", "Bearer " + ana))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erros[*].campo", hasItem("tamanho")));
        mvc.perform(get("/api/partidas/minhas?pagina=-1").header("Authorization", "Bearer " + ana))
                .andExpect(status().isBadRequest());
    }
}
