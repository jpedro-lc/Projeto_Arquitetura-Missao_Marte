package br.unifor.missaomarte.lwjgl3;

import static br.unifor.missaomarte.lwjgl3.VerificacaoDesktop.capturar;
import static br.unifor.missaomarte.lwjgl3.VerificacaoDesktop.clicar;
import static br.unifor.missaomarte.lwjgl3.VerificacaoDesktop.exigir;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import br.unifor.missaomarte.MissaoMarteGame;
import br.unifor.missaomarte.model.*;
import br.unifor.missaomarte.model.EstadoPartida.Posicao;
import br.unifor.missaomarte.model.EstadoPartida.Situacao;
import br.unifor.missaomarte.presentation.RelogioPausavel;
import br.unifor.missaomarte.presentation.input.ControleFisicoEntradaJogo;
import br.unifor.missaomarte.remoto.SessaoAtual;
import br.unifor.missaomarte.repository.RankingEntry;
import br.unifor.missaomarte.repository.RankingRepository;
import br.unifor.missaomarte.screen.GameScreen;
import br.unifor.missaomarte.screen.MenuScreen;
import br.unifor.missaomarte.screen.NovaPartidaScreen;
import br.unifor.missaomarte.service.FabricaMissoes;
import br.unifor.missaomarte.service.JogoService;
import br.unifor.missaomarte.service.TempoRealService;

/**
 * Janela real, relógio real: a partida em tempo real anda sozinha. Segura D até o passageiro
 * (embarque automático) e A de volta à base, conferindo contagem, inimigos, pontos e pausa.
 */
public final class VerificacaoTempoReal {
    private VerificacaoTempoReal() { }

    enum Etapa { MENU, NOVA, CONTAGEM, IDA, VOLTA, PAUSA, FIM }

    public static void main(String[] args) {
        Path saida = Path.of(args[0]).toAbsolutePath();
        var falha = new AtomicReference<Throwable>();
        var vitorias = new int[1];
        RankingRepository ranking = new RankingRepository() {
            @Override public void salvar(String nome, int pontuacao) { vitorias[0]++; }
            @Override public void salvar(String n, int p, Dificuldade d, int q, long t) { vitorias[0]++; }
            @Override public List<RankingEntry> listar() { return List.of(); }
            @Override public void limpar() { }
        };
        var fabrica = new FabricaMissoes() {
            @Override public Missao criar(Dificuldade dificuldade, int limite) {
                var missao = new Missao(new Nave("Teste", 0, 0, 4));
                missao.adicionarPassageiro(new Professor("Ada", 3, 0));
                missao.adicionarAsteroide(new Asteroide(0, 3));
                missao.adicionarInimigo(new Inimigo(4, 4));
                missao.adicionarInimigo(new Inimigo(-4, 4));
                return missao;
            }
        };
        var regras = new TempoRealService(ranking, fabrica, new Random(7), br.unifor.missaomarte.service.RitmoTempoReal::de);
        var segurar = new AtomicReference<Integer>(-1);
        // Só "segurar": nenhuma tecla de borda, para provar que a direção mantida basta.
        var teclado = new ControleFisicoEntradaJogo(c -> false, c -> c == segurar.get());
        var relogio = new RelogioPausavel(System::currentTimeMillis);
        var classico = new JogoService(ranking, fabrica, new Random(3), relogio);
        var game = new MissaoMarteGame(classico, regras, teclado, relogio, new ServidorIndisponivel(), new SessaoAtual());
        var listener = new ApplicationAdapter() {
            Etapa etapa = Etapa.MENU;
            int quadrosNaEtapa;
            int quadro;
            List<Posicao> inimigosNaLargada;
            EstadoPartida congelado;
            boolean inimigosAndaram;

            void ir(Etapa proxima) { etapa = proxima; quadrosNaEtapa = 0; }

            @Override public void create() { game.create(); }
            @Override public void render() {
                try {
                    game.render();
                    quadro++;
                    quadrosNaEtapa++;
                    if (quadro > 60 * 40) throw new AssertionError("Tempo real não terminou em 40 s (etapa " + etapa + ")");
                    if (inimigosNaLargada != null && etapa != Etapa.PAUSA
                            && !regras.consultarEstado().inimigos().equals(inimigosNaLargada)) inimigosAndaram = true;
                    switch (etapa) {
                        case MENU -> {
                            if (quadrosNaEtapa == 5) clicar(((MenuScreen) game.getScreen()).stage(), "INICIAR JOGO");
                            if (game.getScreen() instanceof NovaPartidaScreen) ir(Etapa.NOVA);
                        }
                        case NOVA -> {
                            var nova = (NovaPartidaScreen) game.getScreen();
                            if (quadrosNaEtapa == 3) {
                                exigir(nova.modo() == ModoJogo.TEMPO_REAL, "Tempo real não é o padrão");
                                capturar(saida.resolve("01-nova-partida-modos.png"));
                                clicar(nova.stage(), "DECOLAR");
                            }
                            if (game.getScreen() instanceof GameScreen) ir(Etapa.CONTAGEM);
                        }
                        case CONTAGEM -> {
                            var estado = regras.consultarEstado();
                            segurar.set(Keys.D);
                            if (quadrosNaEtapa == 30) {
                                exigir(regras.contagemRestante() > 0, "Sem contagem de largada");
                                exigir(estado.nave().x() == 0 && estado.pontos() == 20, "Andou durante a contagem");
                                capturar(saida.resolve("02-contagem.png"));
                                inimigosNaLargada = estado.inimigos();
                            }
                            if (regras.contagemRestante() == 0) ir(Etapa.IDA);
                        }
                        case IDA -> {
                            var estado = regras.consultarEstado();
                            if (!estado.passageiros().isEmpty()) return;
                            segurar.set(-1);
                            exigir(estado.nave().passageiros().size() == 1, "Embarque automático falhou");
                            exigir(quadrosNaEtapa < 60, "Nave lenta demais: " + quadrosNaEtapa + " quadros para 3 casas");
                            capturar(saida.resolve("03-embarque-automatico.png"));
                            game.pausarPartida();
                            congelado = estado;
                            ir(Etapa.PAUSA);
                        }
                        case PAUSA -> {
                            if (quadrosNaEtapa == 60) {
                                var estado = regras.consultarEstado();
                                exigir(estado.equals(congelado), "Pausa deixou o tempo andar");
                                capturar(saida.resolve("04-pausa.png"));
                                game.continuarPartida();
                                ir(Etapa.VOLTA);
                            }
                        }
                        case VOLTA -> {
                            var estado = regras.consultarEstado();
                            if (quadrosNaEtapa == 90) {
                                exigir(estado.pontos() < 20 + 15, "Pontos não escoaram com o tempo");
                                capturar(saida.resolve("05-inimigos-andando.png"));
                            }
                            segurar.set(quadrosNaEtapa > 90 ? Keys.A : -1);
                            if (estado.situacao() == Situacao.VITORIA) {
                                segurar.set(-1);
                                ir(Etapa.FIM);
                            } else if (estado.situacao() != Situacao.EM_ANDAMENTO) {
                                throw new AssertionError("Partida terminou em " + estado.situacao());
                            }
                        }
                        case FIM -> {
                            if (quadrosNaEtapa == 30) {
                                exigir(vitorias[0] == 1, "Vitória não foi para o ranking local");
                                exigir(inimigosAndaram, "Inimigos não andaram sozinhos");
                                capturar(saida.resolve("06-vitoria.png"));
                                Gdx.app.exit();
                            }
                        }
                    }
                } catch (Throwable erro) { falha.set(erro); Gdx.app.exit(); }
            }
            @Override public void resize(int w, int h) { game.resize(w, h); }
            @Override public void dispose() {
                game.dispose();
                if (falha.get() == null && etapa != Etapa.FIM) falha.set(new AssertionError("Interrompida na etapa " + etapa));
                if (falha.get() == null) System.out.println("TEMPO REAL PASSOU: contagem, nave segurando D/A, embarque automático, pausa, inimigos sozinhos, pontos por segundo e vitória; capturas=" + saida);
            }
        };
        var config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Missão Marte | Verificação do tempo real");
        config.setWindowedMode(1280, 720);
        config.setForegroundFPS(60);
        config.useVsync(true);
        new Lwjgl3Application(listener, config);
        if (falha.get() != null) throw new AssertionError("Falha na verificação do tempo real", falha.get());
    }
}
