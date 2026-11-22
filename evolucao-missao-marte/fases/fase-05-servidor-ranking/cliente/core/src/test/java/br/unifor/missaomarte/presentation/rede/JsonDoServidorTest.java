package br.unifor.missaomarte.presentation.rede;

import static org.junit.jupiter.api.Assertions.*;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import com.badlogic.gdx.utils.JsonReader;
import br.unifor.missaomarte.model.Dificuldade;
import br.unifor.missaomarte.remoto.PartidaConcluida;
import br.unifor.missaomarte.remoto.PartidaConcluida.Resultado;

class JsonDoServidorTest {
    @Test void credenciaisComAspasEBarrasNaoQuebramOJson() {
        String senha = "a\"b\\c,\"x\":1}";
        var lido = new JsonReader().parse(JsonDoServidor.credenciais("ana_1", senha));
        assertEquals("ana_1", lido.getString("nome"));
        assertEquals(senha, lido.getString("senha"));
        assertEquals(2, lido.size, "nenhum campo extra injetado pela senha");
    }

    @Test void partidaViraJsonComNumerosSemAspasInclusiveNegativos() {
        String json = JsonDoServidor.partida(new PartidaConcluida(Dificuldade.DIFICIL, Resultado.DERROTA, -4, 75, 12));
        assertTrue(json.contains("\"pontuacao\":-4"), json);
        assertTrue(json.contains("\"duracaoSegundos\":75"), json);
        var lido = new JsonReader().parse(json);
        assertEquals("DIFICIL", lido.getString("dificuldade"));
        assertEquals("DERROTA", lido.getString("resultado"));
        assertEquals(12, lido.getInt("movimentos"));
    }

    @Test void leTokenEFalhaSeFaltar() {
        assertEquals("abc.def.ghi", JsonDoServidor.lerToken("{\"token\":\"abc.def.ghi\",\"tipo\":\"Bearer\"}"));
        assertThrows(IllegalArgumentException.class, () -> JsonDoServidor.lerToken("{\"tipo\":\"Bearer\"}"));
        assertThrows(IllegalArgumentException.class, () -> JsonDoServidor.lerToken("{\"token\":\"\"}"));
        assertThrows(RuntimeException.class, () -> JsonDoServidor.lerToken("[]"));
    }

    @Test void leRankingEHistoricoInclusiveListasVazias() {
        var ranking = JsonDoServidor.lerRanking("[{\"posicao\":1,\"nome\":\"Bia\",\"pontuacao\":40,"
                + "\"dificuldade\":\"FACIL\",\"duracaoSegundos\":70,\"registradaEm\":\"2026-11-22T10:00:00Z\"}]");
        assertEquals(1, ranking.size());
        assertEquals("Bia", ranking.get(0).nome());
        assertEquals(Dificuldade.FACIL, ranking.get(0).dificuldade());
        assertTrue(JsonDoServidor.lerRanking("[]").isEmpty());

        var historico = JsonDoServidor.lerHistorico("[{\"id\":7,\"dificuldade\":\"MEDIO\",\"resultado\":\"VITORIA\","
                + "\"pontuacao\":33,\"duracaoSegundos\":42,\"movimentos\":9,\"registradaEm\":\"2026-11-22T13:05:01.123456Z\"}]");
        assertEquals(7, historico.get(0).id());
        assertEquals(Resultado.VITORIA, historico.get(0).resultado());
        assertEquals(Instant.parse("2026-11-22T13:05:01.123456Z"), historico.get(0).registradaEm());
        assertTrue(JsonDoServidor.lerHistorico("[]").isEmpty());
        assertThrows(RuntimeException.class, () -> JsonDoServidor.lerHistorico("{}"));
    }

    @Test void mensagemDeErroVemDoProblemDetail() {
        assertEquals("O nome deve ter de 3 a 20 letras, números, _ ou -.", JsonDoServidor.mensagemDeErro(400,
                "{\"title\":\"Dados inválidos\",\"detail\":\"Um ou mais campos são inválidos.\","
                        + "\"erros\":[{\"campo\":\"nome\",\"mensagem\":\"O nome deve ter de 3 a 20 letras, números, _ ou -.\"}]}"));
        assertEquals("Usuário ou senha inválidos.",
                JsonDoServidor.mensagemDeErro(401, "{\"status\":401,\"detail\":\"Usuário ou senha inválidos.\"}"));
    }

    @Test void semProblemDetailUsaTextoPorCodigoSemVazarOCorpo() {
        String html = "<html><body>Stacktrace: java.lang.NullPointerException at segredo.Interno</body></html>";
        assertEquals("O servidor encontrou um erro. Tente mais tarde.", JsonDoServidor.mensagemDeErro(500, html));
        assertEquals("O servidor encontrou um erro. Tente mais tarde.", JsonDoServidor.mensagemDeErro(503, ""));
        assertEquals("Não autorizado. Entre novamente.", JsonDoServidor.mensagemDeErro(401, null));
        assertEquals("Este nome já está em uso.", JsonDoServidor.mensagemDeErro(409, "{}"));
        assertEquals("Erro inesperado do servidor (código 418).", JsonDoServidor.mensagemDeErro(418, html));
        assertFalse(JsonDoServidor.mensagemDeErro(404, html).contains("Stacktrace"));
    }
}
