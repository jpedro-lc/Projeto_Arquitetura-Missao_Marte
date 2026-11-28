package br.unifor.missaomarte.lwjgl3.input;

import java.util.function.IntPredicate;
import com.badlogic.gdx.Gdx;
import br.unifor.missaomarte.model.Acao;
import br.unifor.missaomarte.presentation.input.ComandoEntrada;
import br.unifor.missaomarte.presentation.input.ControleFisicoEntradaJogo;
import br.unifor.missaomarte.presentation.input.EntradaJogo;

/** Consulta bordas de pressão no desktop (teclado WASD, setas/D-Pad e controle Bluetooth). */
public final class TecladoEntradaJogo implements EntradaJogo {
    private final ControleFisicoEntradaJogo delegado;

    public TecladoEntradaJogo() {
        this.delegado = new ControleFisicoEntradaJogo(codigo -> Gdx.input.isKeyJustPressed(codigo),
                codigo -> Gdx.input.isKeyPressed(codigo));
    }

    public TecladoEntradaJogo(IntPredicate acabouDePressionar) {
        this.delegado = new ControleFisicoEntradaJogo(acabouDePressionar);
    }

    @Override
    public ComandoEntrada ler() {
        return delegado.ler();
    }

    @Override
    public Acao direcaoMantida() {
        return delegado.direcaoMantida();
    }

    @Override
    public boolean pausaSolicitada() {
        return delegado.pausaSolicitada();
    }
}
