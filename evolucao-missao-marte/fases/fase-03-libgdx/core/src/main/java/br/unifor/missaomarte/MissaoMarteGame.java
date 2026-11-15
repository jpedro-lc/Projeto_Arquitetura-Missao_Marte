package br.unifor.missaomarte;

import java.util.Objects;
import com.badlogic.gdx.Game;
import br.unifor.missaomarte.presentation.ControladorPartida;
import br.unifor.missaomarte.presentation.assets.RecursosJogo;
import br.unifor.missaomarte.presentation.input.EntradaJogo;
import br.unifor.missaomarte.screen.GameScreen;
import br.unifor.missaomarte.service.JogoService;

/** Aplicação compartilhada. O backend chama o ciclo de vida herdado de Game. */
public final class MissaoMarteGame extends Game {
    private final JogoService jogo;
    private final EntradaJogo entrada;
    private RecursosJogo recursos;

    public MissaoMarteGame(JogoService jogo, EntradaJogo entrada) {
        this.jogo = Objects.requireNonNull(jogo);
        this.entrada = Objects.requireNonNull(entrada);
    }

    @Override
    public void create() {
        recursos = new RecursosJogo();
        setScreen(new GameScreen(new ControladorPartida(jogo, entrada), recursos));
        // Game.render() encaminha o desenho para a Screen ativa.
    }

    @Override
    public void dispose() {
        // super.dispose() esconde a tela, mas não chama Screen.dispose().
        super.dispose();
        if (getScreen() != null) getScreen().dispose();
        if (recursos != null) recursos.dispose();
    }
}
