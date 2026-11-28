package br.unifor.missaomarte.remoto;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.badlogic.gdx.utils.JsonReader;
import br.unifor.missaomarte.model.Dificuldade;
import br.unifor.missaomarte.model.EstadoPartida;
import br.unifor.missaomarte.model.EstadoPartida.NaveVista;
import br.unifor.missaomarte.model.EstadoPartida.Situacao;
import br.unifor.missaomarte.model.ModoJogo;
import br.unifor.missaomarte.presentation.rede.JsonDoServidor;
import br.unifor.missaomarte.remoto.PartidaConcluida.Resultado;
import br.unifor.missaomarte.repository.RankingLocalRepository;

/** Fase 7: o modo (e o setor do Infinito) viajam até o servidor e ao ranking local sem quebrar o contrato antigo. */
class ModoNoServidorTest {
    static EstadoPartida terminada(Situacao situacao) {
        return new EstadoPartida("Ana", Dificuldade.MEDIO, 5, 42, 9, 61, situacao,
                new NaveVista(0, 0, 0, 4, List.of()), List.of(), List.of(), List.of());
    }

    @Test void partidaConcluidaLevaModoESetorSoNoInfinito() {
        var infinito = PartidaConcluida.de(terminada(Situacao.DERROTA), ModoJogo.INFINITO, 4).orElseThrow();
        assertEquals(ModoJogo.INFINITO, infinito.modo());
        assertEquals(4, infinito.setor());
        var tempoReal = PartidaConcluida.de(terminada(Situacao.VITORIA), ModoJogo.TEMPO_REAL, 7).orElseThrow();
        assertEquals(0, tempoReal.setor(), "setor fora do Infinito é descartado");
        assertEquals(ModoJogo.CLASSICO, PartidaConcluida.de(terminada(Situacao.VITORIA)).orElseThrow().modo());
        assertTrue(PartidaConcluida.de(terminada(Situacao.ABORTADA), ModoJogo.INFINITO, 3).isEmpty());
        assertThrows(IllegalArgumentException.class,
                () -> new PartidaConcluida(Dificuldade.FACIL, Resultado.DERROTA, 1, 1, 1, ModoJogo.INFINITO, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new PartidaConcluida(Dificuldade.FACIL, Resultado.DERROTA, 1, 1, 1, ModoJogo.CLASSICO, 2));
    }

    @Test void jsonDoEnvioSoTemSetorNoInfinito() {
        var infinito = new JsonReader().parse(JsonDoServidor.partida(
                new PartidaConcluida(Dificuldade.FACIL, Resultado.DERROTA, 80, 90, 30, ModoJogo.INFINITO, 5)));
        assertEquals("INFINITO", infinito.getString("modo"));
        assertEquals(5, infinito.getInt("setor"));
        var classico = new JsonReader().parse(JsonDoServidor.partida(
                new PartidaConcluida(Dificuldade.FACIL, Resultado.VITORIA, 20, 30, 10)));
        assertEquals("CLASSICO", classico.getString("modo"));
        assertFalse(classico.has("setor"));
    }

    @Test void rankingEHistoricoLeemModoESetorETambemOFormatoAntigo() {
        var linhas = JsonDoServidor.lerRanking("""
                [{"posicao":1,"nome":"ana","pontuacao":90,"dificuldade":"FACIL","duracaoSegundos":80,
                  "registradaEm":"2026-11-27T10:00:00Z","modo":"INFINITO","setor":4},
                 {"posicao":2,"nome":"bia","pontuacao":30,"dificuldade":"FACIL","duracaoSegundos":20,
                  "registradaEm":"2026-11-27T10:00:00Z","modo":"TEMPO_REAL","setor":null},
                 {"posicao":3,"nome":"caio","pontuacao":10,"dificuldade":"FACIL","duracaoSegundos":20}]""");
        assertEquals(new LinhaRanking(1, "ana", 90, Dificuldade.FACIL, 80, ModoJogo.INFINITO, 4), linhas.get(0));
        assertEquals(ModoJogo.TEMPO_REAL, linhas.get(1).modo());
        assertEquals(0, linhas.get(1).setor());
        assertEquals(ModoJogo.CLASSICO, linhas.get(2).modo());
        var historico = JsonDoServidor.lerHistorico("""
                [{"id":3,"dificuldade":"DIFICIL","resultado":"DERROTA","pontuacao":12,"duracaoSegundos":40,
                  "movimentos":20,"registradaEm":"2026-11-27T10:00:00Z","modo":"INFINITO","setor":2}]""");
        assertEquals(ModoJogo.INFINITO, historico.get(0).modo());
        assertEquals(2, historico.get(0).setor());
    }

    @Test void rankingLocalGuardaModoESetorELeArquivoAntigo(@TempDir Path dir) throws Exception {
        var arquivo = dir.resolve("ranking.json");
        Files.writeString(arquivo, "Velho|50|MEDIO|1|01/01/2026 10:00|30\n");
        var repo = new RankingLocalRepository(arquivo);
        repo.salvar("Ana", 70, Dificuldade.FACIL, 6, 95, ModoJogo.INFINITO, 3);
        repo.salvar("Bia", 40, Dificuldade.FACIL, 4, 25, ModoJogo.TEMPO_REAL, 0);
        var lista = repo.listar();
        assertEquals(List.of("Ana", "Velho", "Bia"), lista.stream().map(e -> e.name).toList());
        assertEquals(ModoJogo.INFINITO, lista.get(0).modo);
        assertEquals(3, lista.get(0).setor);
        assertEquals(ModoJogo.CLASSICO, lista.get(1).modo, "linha antiga, sem modo, é do clássico");
        assertEquals(ModoJogo.TEMPO_REAL, lista.get(2).modo);
    }
}
