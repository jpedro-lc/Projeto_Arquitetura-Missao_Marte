package br.unifor.missaomarte.presentation.rede;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ServidorHttpTest {
    @Test void sucessoConverteOCorpo() {
        var r = ServidorHttp.interpretar(200, "{\"token\":\"t\"}", false, JsonDoServidor::lerToken);
        assertTrue(r.ok());
        assertEquals("t", r.valor());
    }

    @Test void corpoInvalidoEmSucessoViraErroLegivel() {
        var r = ServidorHttp.interpretar(200, "<html>", false, JsonDoServidor::lerToken);
        assertFalse(r.ok());
        assertEquals("Resposta inválida do servidor.", r.erro());
    }

    @Test void status401EmRotaProtegidaSignificaSessaoExpirada() {
        var r = ServidorHttp.interpretar(401, "", true, JsonDoServidor::lerRanking);
        assertTrue(r.naoAutorizado());
        assertEquals(ServidorHttp.SESSAO_EXPIRADA, r.erro());
    }

    @Test void status401NoLoginMostraAMensagemGenericaDoServidor() {
        var r = ServidorHttp.interpretar(401, "{\"detail\":\"Usuário ou senha inválidos.\"}", false, JsonDoServidor::lerToken);
        assertFalse(r.naoAutorizado());
        assertEquals("Usuário ou senha inválidos.", r.erro());
    }

    @Test void erroDoServidorNaoDerrubaNemVazaDetalhes() {
        var r = ServidorHttp.interpretar(500, "java.sql.SQLException: tabela X", true, JsonDoServidor::lerRanking);
        assertFalse(r.ok());
        assertFalse(r.naoAutorizado());
        assertFalse(r.erro().contains("SQLException"));
    }
}
