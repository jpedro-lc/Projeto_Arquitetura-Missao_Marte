package br.unifor.missaomarte.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import br.unifor.missaomarte.model.Dificuldade;
import br.unifor.missaomarte.model.ModoJogo;
import br.unifor.missaomarte.repository.RankingEntry;

/** Cada aba do ranking local mostra só o seu modo e a sua dificuldade, no máximo 10 linhas. */
class RankingFiltroTest {
    static RankingEntry entrada(String nome, int pontos, Dificuldade d, ModoJogo m) {
        return new RankingEntry(nome, pontos, d, 1, "27/11/2026 10:00", 30, m, m == ModoJogo.INFINITO ? 2 : 0);
    }

    @Test void filtraPorModoEDificuldadeMantendoAOrdem() {
        var todas = List.of(entrada("a", 90, Dificuldade.FACIL, ModoJogo.INFINITO),
                entrada("b", 80, Dificuldade.FACIL, ModoJogo.TEMPO_REAL),
                entrada("c", 70, Dificuldade.MEDIO, ModoJogo.INFINITO),
                entrada("d", 60, Dificuldade.FACIL, ModoJogo.INFINITO));
        assertEquals(List.of("a", "d"),
                RankingScreen.filtrar(todas, ModoJogo.INFINITO, Dificuldade.FACIL).stream().map(e -> e.name).toList());
        assertEquals(List.of("b"),
                RankingScreen.filtrar(todas, ModoJogo.TEMPO_REAL, Dificuldade.FACIL).stream().map(e -> e.name).toList());
        assertEquals(List.of(), RankingScreen.filtrar(todas, ModoJogo.CLASSICO, Dificuldade.DIFICIL));
    }

    @Test void limitaADezLinhas() {
        var muitas = new ArrayList<RankingEntry>();
        for (int i = 0; i < 15; i++) muitas.add(entrada("p" + i, 100 - i, Dificuldade.MEDIO, ModoJogo.CLASSICO));
        assertEquals(10, RankingScreen.filtrar(muitas, ModoJogo.CLASSICO, Dificuldade.MEDIO).size());
    }

    @Test void historicoMostraOSetorDoInfinito() {
        assertEquals("INFINITO S4", HistoricoScreen.modo(ModoJogo.INFINITO, 4));
        assertEquals("TEMPO REAL", HistoricoScreen.modo(ModoJogo.TEMPO_REAL, 0));
        assertEquals("CLÁSSICO", HistoricoScreen.modo(ModoJogo.CLASSICO, 0));
    }
}
