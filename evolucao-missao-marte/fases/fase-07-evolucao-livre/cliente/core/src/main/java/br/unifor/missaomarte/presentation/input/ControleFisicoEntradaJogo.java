package br.unifor.missaomarte.presentation.input;

import java.util.Objects;
import java.util.function.IntPredicate;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import br.unifor.missaomarte.model.Acao;

/**
 * Entrada por teclado físico, tecla Voltar do Android e controle Bluetooth (D-Pad e botões A/B/Y/Start).
 * Consulta bordas de pressão: segurar um botão não avança um turno por frame.
 */
public final class ControleFisicoEntradaJogo implements EntradaJogo {
    private final IntPredicate acabouDePressionar;

    public ControleFisicoEntradaJogo() {
        this(codigo -> Gdx.input.isKeyJustPressed(codigo));
    }

    public ControleFisicoEntradaJogo(IntPredicate acabouDePressionar) {
        this.acabouDePressionar = Objects.requireNonNull(acabouDePressionar);
    }

    @Override
    public ComandoEntrada ler() {
        if (pressionada(Keys.ESCAPE) || pressionada(Keys.BACK)) return new ComandoEntrada(Acao.VAZIA, false, true);
        if (pressionada(Keys.R) || pressionada(Keys.BUTTON_Y)) return new ComandoEntrada(Acao.VAZIA, true, false);
        if (pressionada(Keys.W) || pressionada(Keys.DPAD_UP)) return ComandoEntrada.acao(Acao.CIMA);
        if (pressionada(Keys.S) || pressionada(Keys.DPAD_DOWN)) return ComandoEntrada.acao(Acao.BAIXO);
        if (pressionada(Keys.A) || pressionada(Keys.DPAD_LEFT)) return ComandoEntrada.acao(Acao.ESQUERDA);
        if (pressionada(Keys.D) || pressionada(Keys.DPAD_RIGHT)) return ComandoEntrada.acao(Acao.DIREITA);
        if (pressionada(Keys.C) || pressionada(Keys.BUTTON_A)) return ComandoEntrada.acao(Acao.EMBARCAR);
        if (pressionada(Keys.Q) || pressionada(Keys.BUTTON_B)) return ComandoEntrada.acao(Acao.ABORTAR);
        return ComandoEntrada.nenhum();
    }

    private boolean pressionada(int codigo) {
        return acabouDePressionar.test(codigo);
    }

    @Override
    public boolean pausaSolicitada() {
        return pressionada(Keys.P) || pressionada(Keys.BUTTON_START);
    }
}
