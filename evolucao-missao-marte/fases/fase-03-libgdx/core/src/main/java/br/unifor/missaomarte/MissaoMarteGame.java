package br.unifor.missaomarte;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import br.unifor.missaomarte.model.Dificuldade;
import br.unifor.missaomarte.presentation.ControladorPartida;
import br.unifor.missaomarte.presentation.RelogioPausavel;
import br.unifor.missaomarte.presentation.assets.RecursosJogo;
import br.unifor.missaomarte.presentation.audio.AudioPartida;
import br.unifor.missaomarte.presentation.input.EntradaJogo;
import br.unifor.missaomarte.screen.GameScreen;
import br.unifor.missaomarte.screen.MenuScreen;
import br.unifor.missaomarte.service.JogoService;

/** Composição da apresentação; regras e relógio são fornecidos pelo launcher. */
public final class MissaoMarteGame extends Game {
    private final JogoService jogo;
    private final EntradaJogo entrada;
    private final RelogioPausavel relogio;
    private final List<Screen> descartePendente = new ArrayList<>();
    private RecursosJogo recursos;
    private AudioPartida audio;
    private ControladorPartida controlador;

    public MissaoMarteGame(JogoService jogo, EntradaJogo entrada, RelogioPausavel relogio) {
        this.jogo = Objects.requireNonNull(jogo);
        this.entrada = Objects.requireNonNull(entrada);
        this.relogio = Objects.requireNonNull(relogio);
    }

    @Override public void create() {
        recursos = new RecursosJogo();
        audio = new AudioPartida(recursos);
        mostrarMenu();
    }

    public void iniciarPartida() {
        relogio.retomar();
        String piloto = controlador == null ? null : controlador.estado().piloto();
        Dificuldade dificuldade = controlador == null ? Dificuldade.MEDIO : controlador.estado().dificuldade();
        int limite = controlador == null ? 5 : controlador.estado().limite();
        controlador = new ControladorPartida(jogo, entrada, piloto, dificuldade, limite);
        trocarTela(new GameScreen(this, controlador, recursos));
        audio.iniciar();
    }

    public void mostrarMenu() {
        if (controlador != null) controlador.abortar();
        relogio.retomar();
        audio.parar();
        trocarTela(new MenuScreen(this, recursos));
    }

    public void pausarPartida() {
        if (getScreen() instanceof GameScreen tela && !tela.pausado()) {
            controlador.pausar();
            relogio.pausar();
            audio.pausar();
            tela.exibirPausa(true);
        }
    }

    public void continuarPartida() {
        if (getScreen() instanceof GameScreen tela && tela.pausado()) {
            relogio.retomar();
            controlador.retomar();
            audio.retomar();
            tela.exibirPausa(false);
        }
    }

    private void trocarTela(Screen nova) {
        Screen anterior = getScreen();
        setScreen(nova);
        if (anterior != null) descartePendente.add(anterior);
    }

    @Override public void render() {
        for (Screen tela : descartePendente) tela.dispose();
        descartePendente.clear();
        super.render();
    }

    public AudioPartida audio() { return audio; }
    public EntradaJogo entrada() { return entrada; }
    public RecursosJogo recursos() { return recursos; }

    @Override public void dispose() {
        super.dispose();
        if (getScreen() != null) getScreen().dispose();
        for (Screen tela : descartePendente) tela.dispose();
        descartePendente.clear();
        if (audio != null) audio.parar();
        if (recursos != null) recursos.dispose();
    }
}
