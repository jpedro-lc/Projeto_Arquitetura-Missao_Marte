package br.unifor.missaomarte.servidor.ranking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import br.unifor.missaomarte.servidor.ApiTeste;

class RankingApiTest extends ApiTeste {
    private ResultActions ranking(String token) throws Exception {
        return mvc.perform(get("/api/ranking").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
    }

    @Test void rankingVazio() throws Exception {
        ranking(token("ana")).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test void umaLinhaPorJogadorComAMelhorVitoria() throws Exception {
        String ana = token("Ana");
        registrar(ana, "VITORIA", 20, 50);
        registrar(ana, "VITORIA", 35, 60);
        registrar(ana, "VITORIA", 30, 10);
        String bia = token("Bia");
        registrar(bia, "VITORIA", 40, 70);
        ranking(ana)
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].posicao").value(1))
                .andExpect(jsonPath("$[0].nome").value("Bia"))
                .andExpect(jsonPath("$[1].nome").value("Ana"))
                .andExpect(jsonPath("$[1].pontuacao").value(35));
    }

    @Test void derrotasEPontuacaoNaoPositivaFicamDeFora() throws Exception {
        String ana = token("ana");
        registrar(ana, "DERROTA", 90, 10);
        registrar(ana, "VITORIA", 0, 10);
        registrar(token("bia"), "VITORIA", -5, 10);
        ranking(ana).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test void empateDesfeitoPorMenorDuracaoEDepoisPelaMaisAntiga() throws Exception {
        String lento = token("lento");
        registrar(lento, "VITORIA", 30, 90);
        String antigo = token("antigo");
        registrar(antigo, "VITORIA", 30, 40);
        String novo = token("novo");
        registrar(novo, "VITORIA", 30, 40);
        // Mesma pontuação e duração da própria conta: conta a mais antiga (só uma linha).
        registrar(novo, "VITORIA", 30, 40);
        ranking(lento)
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].nome").value("antigo"))
                .andExpect(jsonPath("$[1].nome").value("novo"))
                .andExpect(jsonPath("$[2].nome").value("lento"));
    }

    @Test void limiteDeDezLinhasSemDadosSensiveis() throws Exception {
        String ultimo = null;
        for (int i = 1; i <= 12; i++) {
            ultimo = token("jogador" + i);
            registrar(ultimo, "VITORIA", i * 10, 30);
        }
        String corpo = ranking(ultimo)
                .andExpect(jsonPath("$", hasSize(10)))
                .andExpect(jsonPath("$[0].nome").value("jogador12"))
                .andExpect(jsonPath("$[9].nome").value("jogador3"))
                .andReturn().getResponse().getContentAsString();
        assertThat(corpo).doesNotContain("senha").doesNotContain("$2a$").doesNotContain("Hash")
                .doesNotContain(SENHA).doesNotContain("\"id\"");
    }
}
