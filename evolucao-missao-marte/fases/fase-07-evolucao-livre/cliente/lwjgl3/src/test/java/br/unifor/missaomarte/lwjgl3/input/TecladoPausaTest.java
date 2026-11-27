package br.unifor.missaomarte.lwjgl3.input;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import com.badlogic.gdx.Input.Keys;
import br.unifor.missaomarte.model.Acao;

class TecladoPausaTest {
    @Test void pSolicitaPausaSemProduzirAcaoDaMissao() {
        var entrada = new TecladoEntradaJogo(tecla -> tecla == Keys.P);
        assertTrue(entrada.pausaSolicitada());
        assertEquals(Acao.VAZIA, entrada.ler().acao());
        assertFalse(entrada.ler().sair());
    }
    @Test void espacoContinuaIndisponivelSemCriarTiroOuTurno() {
        var entrada = new TecladoEntradaJogo(tecla -> tecla == Keys.SPACE);
        assertFalse(entrada.pausaSolicitada());
        assertEquals(Acao.VAZIA, entrada.ler().acao());
    }
    @Test void escPermaneceIntencaoDeNavegacaoInterpretadaPelaTela() {
        var entrada = new TecladoEntradaJogo(tecla -> tecla == Keys.ESCAPE);
        assertTrue(entrada.ler().sair());
        assertEquals(Acao.VAZIA, entrada.ler().acao());
    }
}
