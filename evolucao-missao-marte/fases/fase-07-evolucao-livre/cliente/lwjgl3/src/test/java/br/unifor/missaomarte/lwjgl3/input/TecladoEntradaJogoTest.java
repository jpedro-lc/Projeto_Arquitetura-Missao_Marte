package br.unifor.missaomarte.lwjgl3.input;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import com.badlogic.gdx.Input.Keys;
import br.unifor.missaomarte.model.Acao;

class TecladoEntradaJogoTest {
    static Stream<Arguments> teclas() {
        return Stream.of(Arguments.of(Keys.W, Acao.CIMA), Arguments.of(Keys.S, Acao.BAIXO),
                Arguments.of(Keys.A, Acao.ESQUERDA), Arguments.of(Keys.D, Acao.DIREITA),
                Arguments.of(Keys.C, Acao.EMBARCAR), Arguments.of(Keys.Q, Acao.ABORTAR),
                Arguments.of(Keys.DPAD_UP, Acao.CIMA), Arguments.of(Keys.DPAD_DOWN, Acao.BAIXO),
                Arguments.of(Keys.DPAD_LEFT, Acao.ESQUERDA), Arguments.of(Keys.DPAD_RIGHT, Acao.DIREITA),
                Arguments.of(Keys.BUTTON_A, Acao.EMBARCAR), Arguments.of(Keys.BUTTON_B, Acao.ABORTAR));
    }

    @ParameterizedTest
    @MethodSource("teclas")
    void teclaProduzIntencaoSemJanelaOuOpenGL(int codigo, Acao acao) {
        var teclado = new TecladoEntradaJogo(chave -> chave == codigo);
        assertEquals(acao, teclado.ler().acao());
        assertFalse(teclado.ler().reiniciar());
        assertFalse(teclado.ler().sair());
    }

    @Test
    void nenhumaPressaoNaoProduzTurno() {
        assertEquals(Acao.VAZIA, new TecladoEntradaJogo(chave -> false).ler().acao());
    }

    @Test
    void reiniciarESairSaoComandosDaAplicacao() {
        var reiniciar = new TecladoEntradaJogo(chave -> chave == Keys.R).ler();
        assertTrue(reiniciar.reiniciar());
        assertEquals(Acao.VAZIA, reiniciar.acao());
        var sair = new TecladoEntradaJogo(chave -> chave == Keys.ESCAPE).ler();
        assertTrue(sair.sair());
        assertEquals(Acao.VAZIA, sair.acao());
        var voltarAndroid = new TecladoEntradaJogo(chave -> chave == Keys.BACK).ler();
        assertTrue(voltarAndroid.sair());
        var pausaGamepad = new TecladoEntradaJogo(chave -> chave == Keys.BUTTON_START);
        assertTrue(pausaGamepad.pausaSolicitada());
    }

    @Test
    void teclasSimultaneasProduzemUmaIntencaoComPrioridadeParaSaida() {
        Set<Integer> pressionadas = Set.of(Keys.ESCAPE, Keys.R, Keys.W, Keys.C);
        var comando = new TecladoEntradaJogo(pressionadas::contains).ler();
        assertTrue(comando.sair());
        assertFalse(comando.reiniciar());
        assertEquals(Acao.VAZIA, comando.acao());
    }
}
