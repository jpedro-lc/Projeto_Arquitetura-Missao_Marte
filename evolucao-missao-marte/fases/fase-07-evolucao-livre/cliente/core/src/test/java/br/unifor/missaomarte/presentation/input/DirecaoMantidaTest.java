package br.unifor.missaomarte.presentation.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.Set;
import org.junit.jupiter.api.Test;
import com.badlogic.gdx.Input.Keys;
import br.unifor.missaomarte.model.Acao;

/** Tempo real: cada fonte informa o que está pressionado agora, sem janela nem OpenGL. */
class DirecaoMantidaTest {
    @Test void toqueGuardaOBotaoSeguradoAteSoltarOuLimpar() {
        var toque = new ToqueEntradaJogo();
        assertEquals(Acao.VAZIA, toque.direcaoMantida());
        toque.manter(Acao.CIMA);
        assertEquals(Acao.CIMA, toque.direcaoMantida());
        toque.manter(Acao.EMBARCAR);
        assertEquals(Acao.VAZIA, toque.direcaoMantida());
        toque.manter(Acao.ESQUERDA);
        toque.limpar();
        assertEquals(Acao.VAZIA, toque.direcaoMantida());
    }

    @Test void tecladoEControleUsamATeclaPressionadaNaoABorda() {
        var segurando = new ControleFisicoEntradaJogo(codigo -> false, Set.of(Keys.D)::contains);
        assertEquals(Acao.DIREITA, segurando.direcaoMantida());
        assertEquals(Acao.VAZIA, segurando.ler().acao());
        assertEquals(Acao.CIMA, new ControleFisicoEntradaJogo(c -> false, Set.of(Keys.DPAD_UP)::contains).direcaoMantida());
        assertEquals(Acao.VAZIA, new ControleFisicoEntradaJogo(c -> true).direcaoMantida());
    }

    @Test void compostaDevolveAPrimeiraFonteComMovimento() {
        var toque = new ToqueEntradaJogo();
        var teclado = new ControleFisicoEntradaJogo(c -> false, Set.of(Keys.S)::contains);
        var composta = new EntradaComposta(toque, teclado);
        assertEquals(Acao.BAIXO, composta.direcaoMantida());
        toque.manter(Acao.ESQUERDA);
        assertEquals(Acao.ESQUERDA, composta.direcaoMantida());
        assertEquals(Acao.VAZIA, new EntradaComposta(new ToqueEntradaJogo()).direcaoMantida());
    }
}
