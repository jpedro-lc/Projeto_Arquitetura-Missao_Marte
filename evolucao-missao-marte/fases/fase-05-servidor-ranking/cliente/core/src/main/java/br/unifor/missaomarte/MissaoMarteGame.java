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
import br.unifor.missaomarte.remoto.ServidorDoJogo;
import br.unifor.missaomarte.remoto.Sessao;
import br.unifor.missaomarte.remoto.SessaoAtual;
import br.unifor.missaomarte.screen.GameScreen;
import br.unifor.missaomarte.screen.HistoricoScreen;
import br.unifor.missaomarte.screen.LoginScreen;
import br.unifor.missaomarte.screen.MenuScreen;
import br.unifor.missaomarte.screen.NovaPartidaScreen;
import br.unifor.missaomarte.screen.RankingScreen;
import br.unifor.missaomarte.service.JogoService;

/** Composição da apresentação e navegação; regras, relógio, servidor e sessão vêm do launcher. */
public final class MissaoMarteGame extends Game {
    private final JogoService jogo;
    private final EntradaJogo entrada;
    private final RelogioPausavel relogio;
    private final ServidorDoJogo servidor;
    private final SessaoAtual sessao;
    private final List<Screen> descartePendente = new ArrayList<>();
    private RecursosJogo recursos;
    private AudioPartida audio;
    private ControladorPartida controlador;

    public MissaoMarteGame(JogoService jogo, EntradaJogo entrada, RelogioPausavel relogio,
                           ServidorDoJogo servidor, SessaoAtual sessao) {
        this.jogo = Objects.requireNonNull(jogo);
        this.entrada = Objects.requireNonNull(entrada);
        this.relogio = Objects.requireNonNull(relogio);
        this.servidor = Objects.requireNonNull(servidor);
        this.sessao = Objects.requireNonNull(sessao);
    }

    @Override public void create() {
        recursos = new RecursosJogo();
        audio = new AudioPartida(recursos);
        mostrarMenu();
    }

    /** Logado, o piloto é sempre o nome da conta; convidado usa o nome digitado. */
    public void iniciarPartida(String pilotoConvidado, Dificuldade dificuldade) {
        String piloto = sessao.logado() ? sessao.sessao().nome() : pilotoConvidado;
        int limite = controlador == null ? 5 : controlador.estado().limite();
        comecar(piloto, dificuldade, limite);
    }

    /** Reinício: mesma configuração da partida anterior. */
    public void iniciarPartida() {
        if (controlador == null) { iniciarPartida(null, Dificuldade.MEDIO); return; }
        var anterior = controlador.estado();
        comecar(anterior.piloto(), anterior.dificuldade(), anterior.limite());
    }

    private void comecar(String piloto, Dificuldade dificuldade, int limite) {
        relogio.retomar();
        controlador = new ControladorPartida(jogo, entrada, piloto, dificuldade, limite);
        trocarTela(new GameScreen(this, controlador, recursos));
        audio.iniciar();
    }

    public void mostrarMenu() { mostrarMenu(null); }

    public void mostrarMenu(String aviso) {
        if (controlador != null) controlador.abortar();
        relogio.retomar();
        audio.parar();
        trocarTela(new MenuScreen(this, recursos, aviso));
    }

    public void mostrarLogin() { trocarTela(new LoginScreen(this, recursos)); }
    public void mostrarNovaPartida() { trocarTela(new NovaPartidaScreen(this, recursos)); }
    public void mostrarRanking() { trocarTela(new RankingScreen(this, recursos)); }
    public void mostrarHistorico() { trocarTela(new HistoricoScreen(this, recursos)); }

    public void entrar(Sessao nova) {
        sessao.entrar(nova);
        mostrarMenu("Bem-vindo, " + nova.nome() + "!");
    }

    public void sairDaConta() {
        sessao.encerrar();
        mostrarMenu("Você saiu da conta. Modo convidado.");
    }

    /** Resposta 401 em rota protegida: descarta a sessão e volta ao modo convidado. */
    public void sessaoExpirada() { sessao.encerrar(); }

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
    public JogoService jogo() { return jogo; }
    public ServidorDoJogo servidor() { return servidor; }
    public SessaoAtual sessao() { return sessao; }

    @Override public void dispose() {
        super.dispose();
        if (getScreen() != null) getScreen().dispose();
        for (Screen tela : descartePendente) tela.dispose();
        descartePendente.clear();
        if (audio != null) audio.parar();
        if (recursos != null) recursos.dispose();
    }
}
