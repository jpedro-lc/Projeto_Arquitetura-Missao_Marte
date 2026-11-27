package br.unifor.missaomarte.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import br.unifor.missaomarte.presentation.HudDados;

class HistoricoScreenTest {
    @Test void dataDoServidorEmUtcApareceNoFusoDoJogador() {
        var instante = Instant.parse("2026-11-22T13:05:59.999Z");
        assertEquals("22/11/2026 10:05", HistoricoScreen.formatarData(instante, ZoneId.of("America/Fortaleza")));
        assertEquals("22/11/2026 13:05", HistoricoScreen.formatarData(instante, ZoneId.of("UTC")));
    }

    @Test void tempoEmMinutosESegundos() {
        assertEquals("01:15", HudDados.tempo(75));
        assertEquals("00:00", HudDados.tempo(-3));
    }
}
