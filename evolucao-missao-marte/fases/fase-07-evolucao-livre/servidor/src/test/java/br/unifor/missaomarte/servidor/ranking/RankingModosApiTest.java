package br.unifor.missaomarte.servidor.ranking;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;
import br.unifor.missaomarte.servidor.ApiTeste;
import br.unifor.missaomarte.servidor.partida.PartidaRepository;

/** Fase 7: 3 modos × 3 dificuldades, cada tabela por pontos; sem parâmetros, o ranking antigo. */
class RankingModosApiTest extends ApiTeste {
    @Autowired PartidaRepository partidas;
    @Autowired org.springframework.jdbc.core.JdbcTemplate jdbc;

    private static String partida(String modo, String dificuldade, String resultado, int pontos, Integer setor) {
        return "{\"dificuldade\":\"" + dificuldade + "\",\"resultado\":\"" + resultado + "\",\"pontuacao\":" + pontos
                + ",\"duracaoSegundos\":30,\"movimentos\":12" + (modo == null ? "" : ",\"modo\":\"" + modo + "\"")
                + (setor == null ? "" : ",\"setor\":" + setor) + "}";
    }

    private void enviar(String token, String modo, String dificuldade, String resultado, int pontos, Integer setor) throws Exception {
        postar("/api/partidas", partida(modo, dificuldade, resultado, pontos, setor), token).andExpect(status().isCreated());
    }

    private ResultActions tabela(String token, String modo, String dificuldade) throws Exception {
        return mvc.perform(get("/api/ranking").param("modo", modo).param("dificuldade", dificuldade)
                .header("Authorization", "Bearer " + token));
    }

    @Test void cadaModoEDificuldadeTemSuaTabela() throws Exception {
        String ana = token("ana");
        enviar(ana, "TEMPO_REAL", "MEDIO", "VITORIA", 40, null);
        enviar(ana, "CLASSICO", "MEDIO", "VITORIA", 25, null);
        enviar(ana, "TEMPO_REAL", "DIFICIL", "VITORIA", 60, null);
        tabela(ana, "TEMPO_REAL", "MEDIO").andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].pontuacao").value(40))
                .andExpect(jsonPath("$[0].modo").value("TEMPO_REAL"));
        tabela(ana, "CLASSICO", "MEDIO").andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].pontuacao").value(25));
        tabela(ana, "TEMPO_REAL", "DIFICIL").andExpect(jsonPath("$[0].pontuacao").value(60));
        tabela(ana, "INFINITO", "MEDIO").andExpect(jsonPath("$", hasSize(0)));
    }

    @Test void infinitoContaDerrotasOrdenaPorPontosEMostraOSetor() throws Exception {
        String ana = token("ana");
        String bia = token("bia");
        enviar(ana, "INFINITO", "FACIL", "DERROTA", 90, 4);
        enviar(ana, "INFINITO", "FACIL", "DERROTA", 70, 6);
        enviar(bia, "INFINITO", "FACIL", "DERROTA", 120, 3);
        tabela(ana, "INFINITO", "FACIL").andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nome").value("bia")).andExpect(jsonPath("$[0].setor").value(3))
                .andExpect(jsonPath("$[1].nome").value("ana")).andExpect(jsonPath("$[1].pontuacao").value(90))
                .andExpect(jsonPath("$[1].setor").value(4));
    }

    @Test void derrotaNaoEntraNoRankingDoTempoReal() throws Exception {
        String ana = token("ana");
        enviar(ana, "TEMPO_REAL", "FACIL", "DERROTA", 50, null);
        tabela(ana, "TEMPO_REAL", "FACIL").andExpect(jsonPath("$", hasSize(0)));
    }

    @Test void semModoValeClassicoEORankingAntigoContinua() throws Exception {
        String ana = token("ana");
        postar("/api/partidas", partida(null, "MEDIO", "VITORIA", 33, null), ana)
                .andExpect(status().isCreated()).andExpect(jsonPath("$.modo").value("CLASSICO"))
                .andExpect(jsonPath("$.setor").doesNotExist());
        tabela(ana, "CLASSICO", "MEDIO").andExpect(jsonPath("$[0].pontuacao").value(33));
        mvc.perform(get("/api/ranking").header("Authorization", "Bearer " + ana))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].pontuacao").value(33));
    }

    @Test void setorSoNoInfinitoEObrigatorioNele() throws Exception {
        String ana = token("ana");
        postar("/api/partidas", partida("INFINITO", "FACIL", "DERROTA", 10, null), ana).andExpect(status().isBadRequest());
        postar("/api/partidas", partida("TEMPO_REAL", "FACIL", "VITORIA", 10, 2), ana).andExpect(status().isBadRequest());
        postar("/api/partidas", partida("INFINITO", "FACIL", "DERROTA", 10, 0), ana).andExpect(status().isBadRequest());
        postar("/api/partidas", partida("ARCADE", "FACIL", "DERROTA", 10, null), ana).andExpect(status().isBadRequest());
    }

    @Test void filtroIncompletoOuInvalidoDa400() throws Exception {
        String ana = token("ana");
        mvc.perform(get("/api/ranking").param("modo", "INFINITO").header("Authorization", "Bearer " + ana))
                .andExpect(status().isBadRequest());
        tabela(ana, "ARCADE", "FACIL").andExpect(status().isBadRequest());
    }

    @Test void historicoMostraModoESetor() throws Exception {
        String ana = token("ana");
        enviar(ana, "INFINITO", "DIFICIL", "DERROTA", 80, 5);
        mvc.perform(get("/api/partidas/minhas").header("Authorization", "Bearer " + ana))
                .andExpect(jsonPath("$[0].modo").value("INFINITO")).andExpect(jsonPath("$[0].setor").value(5));
    }

    @Test void partidasSemModoViramClassico() throws Exception {
        String ana = token("ana");
        enviar(ana, "TEMPO_REAL", "MEDIO", "VITORIA", 50, null);
        enviar(ana, null, "MEDIO", "VITORIA", 20, null);
        // Simula uma linha gravada antes da Fase 7, quando a coluna não existia.
        jdbc.update("update partida set modo = null where pontuacao = 20");
        org.assertj.core.api.Assertions.assertThat(partidas.marcarAntigasComoClassico()).isEqualTo(1);
        tabela(ana, "CLASSICO", "MEDIO").andExpect(jsonPath("$[0].pontuacao").value(20));
        tabela(ana, "TEMPO_REAL", "MEDIO").andExpect(jsonPath("$[0].pontuacao").value(50));
    }
}
