package br.unifor.missaomarte.lwjgl3.input;

import java.util.Objects;
import java.util.function.IntPredicate;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import br.unifor.missaomarte.model.Acao;
import br.unifor.missaomarte.presentation.input.ComandoEntrada;
import br.unifor.missaomarte.presentation.input.EntradaJogo;

/** Consulta bordas de pressão: segurar uma tecla não avança um turno por frame. */
public final class TecladoEntradaJogo implements EntradaJogo {
    private final IntPredicate acabouDePressionar;

    public TecladoEntradaJogo() {
        this(codigo -> Gdx.input.isKeyJustPressed(codigo));
    }

    public TecladoEntradaJogo(IntPredicate acabouDePressionar) {
        this.acabouDePressionar = Objects.requireNonNull(acabouDePressionar);
    }

    @Override
    public ComandoEntrada ler() {
        // Uma única intenção por frame; teclas simultâneas seguem esta prioridade.
        if (pressionada(Keys.ESCAPE)) return new ComandoEntrada(Acao.VAZIA, false, true);
        if (pressionada(Keys.R)) return new ComandoEntrada(Acao.VAZIA, true, false);
        if (pressionada(Keys.W)) return ComandoEntrada.acao(Acao.CIMA);
        if (pressionada(Keys.S)) return ComandoEntrada.acao(Acao.BAIXO);
        if (pressionada(Keys.A)) return ComandoEntrada.acao(Acao.ESQUERDA);
        if (pressionada(Keys.D)) return ComandoEntrada.acao(Acao.DIREITA);
        if (pressionada(Keys.C)) return ComandoEntrada.acao(Acao.EMBARCAR);
        if (pressionada(Keys.Q)) return ComandoEntrada.acao(Acao.ABORTAR);
        return ComandoEntrada.nenhum();
    }

    private boolean pressionada(int codigo) { return acabouDePressionar.test(codigo); }

    @Override
    public boolean pausaSolicitada() { return pressionada(Keys.P); }
}
