package br.unifor.missaomarte.lwjgl3;

import static br.unifor.missaomarte.lwjgl3.VerificacaoDesktop.capturar;
import static br.unifor.missaomarte.lwjgl3.VerificacaoDesktop.clicar;
import static br.unifor.missaomarte.lwjgl3.VerificacaoDesktop.exigir;
import static br.unifor.missaomarte.lwjgl3.VerificacaoDesktop.procurar;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import br.unifor.missaomarte.MissaoMarteGame;
import br.unifor.missaomarte.model.*;
import br.unifor.missaomarte.model.EstadoPartida.Situacao;
import br.unifor.missaomarte.presentation.RelogioPausavel;
import br.unifor.missaomarte.presentation.input.ControleFisicoEntradaJogo;
import br.unifor.missaomarte.remoto.SessaoAtual;
import br.unifor.missaomarte.repository.RankingLocalRepository;
import br.unifor.missaomarte.screen.GameScreen;
import br.unifor.missaomarte.screen.MenuScreen;
import br.unifor.missaomarte.screen.NovaPartidaScreen;
import br.unifor.missaomarte.screen.RankingScreen;
import br.unifor.missaomarte.screen.TelaCarregavel;
import br.unifor.missaomarte.service.FabricaMissoes;
import br.unifor.missaomarte.service.InfinitoService;
import br.unifor.missaomarte.service.JogoService;
import br.unifor.missaomarte.service.TempoRealService;

/**
 * Janela e relógio reais, modo convidado: conclui o setor 1 do Infinito, vê o setor 2 começar,
 * para sobre um asteroide até a corrida acabar e confere a corrida na aba Infinito do ranking local.
 */
public final class VerificacaoInfinito {
    private VerificacaoInfinito() { }

    enum Etapa { MENU, NOVA, SETOR1, SETOR2, FIM, RANKING, PRONTO }

    public static void main(String[] args) throws Exception {
        Path saida = Path.of(args[0]).toAbsolutePath();
        var falha = new AtomicReference<Throwable>();
        var local = new RankingLocalRepository(Files.createTempDirectory("infinito").resolve("ranking.json"));
        // Passageiro em (2,0); asteroide em (0,1); inimigos longe, nos cantos de baixo.
        var fabrica = new FabricaMissoes() {
            @Override public Missao criarSetor(Dificuldade d, int limite, int asteroides, int inimigos, int vidas) {
                var missao = new Missao(new Nave("Teste", 0, 0, 4, vidas));
                missao.adicionarPassageiro(new Professor("Ada", 2, 0));
                missao.adicionarAsteroide(new Asteroide(0, 1));
                missao.adicionarInimigo(new Inimigo(-limite, -limite));
                missao.adicionarInimigo(new Inimigo(limite, -limite));
                return missao;
            }
        };
        var parados = new Random() { @Override public int nextInt(int limite) { return 1; } };
        var infinito = new InfinitoService(local, fabrica, parados);
        var segurar = new AtomicReference<Integer>(-1);
        var teclado = new ControleFisicoEntradaJogo(c -> false, c -> c == segurar.get());
        var relogio = new RelogioPausavel(System::currentTimeMillis);
        var jogo = new JogoService(local, new FabricaMissoes(), new Random(3), relogio);
        var game = new MissaoMarteGame(jogo, new TempoRealService(local), infinito, teclado, relogio,
                new ServidorIndisponivel(), new SessaoAtual());
        var listener = new ApplicationAdapter() {
            Etapa etapa = Etapa.MENU;
            int quadrosNaEtapa;
            int quadro;

            void ir(Etapa proxima) { etapa = proxima; quadrosNaEtapa = 0; }

            @Override public void create() { game.create(); }
            @Override public void render() {
                try {
                    game.render();
                    quadro++;
                    quadrosNaEtapa++;
                    if (quadro > 60 * 45) throw new AssertionError("Infinito não terminou em 45 s (etapa " + etapa + ")");
                    switch (etapa) {
                        case MENU -> {
                            if (quadrosNaEtapa == 5) clicar(((MenuScreen) game.getScreen()).stage(), "INICIAR JOGO");
                            if (game.getScreen() instanceof NovaPartidaScreen) ir(Etapa.NOVA);
                        }
                        case NOVA -> {
                            var nova = (NovaPartidaScreen) game.getScreen();
                            if (quadrosNaEtapa == 3) clicar(nova.stage(), "modo-infinito");
                            if (quadrosNaEtapa == 6) {
                                exigir(nova.modo() == ModoJogo.INFINITO, "Aba Infinito não selecionou o modo");
                                capturar(saida.resolve("01-nova-partida-infinito.png"));
                                clicar(nova.stage(), "DECOLAR");
                            }
                            if (game.getScreen() instanceof GameScreen) ir(Etapa.SETOR1);
                        }
                        case SETOR1 -> {
                            var tela = (GameScreen) game.getScreen();
                            if (quadrosNaEtapa == 20) {
                                exigir(texto(tela, "setor-infinito").equals("SETOR 1"), "HUD sem o setor 1");
                                capturar(saida.resolve("02-setor-1.png"));
                            }
                            if (infinito.setor() == 2) { segurar.set(-1); ir(Etapa.SETOR2); return; }
                            if (infinito.contagemRestante() > 0) return;
                            var estado = infinito.consultarEstado();
                            segurar.set(estado.passageiros().isEmpty() ? Keys.A : Keys.D);
                        }
                        case SETOR2 -> {
                            var tela = (GameScreen) game.getScreen();
                            if (quadrosNaEtapa == 30) {
                                exigir(texto(tela, "setor-infinito").equals("SETOR 2"), "HUD sem o setor 2");
                                exigir(infinito.contagemRestante() > 0, "Setor 2 sem contagem");
                                exigir(infinito.consultarEstado().pontos() > 30, "Pontos não acumularam");
                                capturar(saida.resolve("03-setor-2-contagem.png"));
                            }
                            // Sobe para o asteroide e fica lá: a cada 1,5 s de proteção perde uma vida.
                            if (infinito.contagemRestante() == 0) segurar.set(infinito.consultarEstado().nave().y() < 1 ? Keys.W : -1);
                            if (infinito.consultarEstado().situacao() == Situacao.DERROTA) { segurar.set(-1); ir(Etapa.FIM); }
                        }
                        case FIM -> {
                            if (quadrosNaEtapa == 30) {
                                var tela = (GameScreen) game.getScreen();
                                exigir(local.listar().size() == 1 && local.listar().get(0).modo == ModoJogo.INFINITO
                                        && local.listar().get(0).setor == 2, "Corrida não foi para o ranking local");
                                capturar(saida.resolve("04-fim-da-corrida.png"));
                                clicar(tela.stage(), "VOLTAR AO MENU");
                            }
                            if (quadrosNaEtapa == 40) {
                                clicar(((MenuScreen) game.getScreen()).stage(), "RANKING LOCAL");
                                ir(Etapa.RANKING);
                            }
                        }
                        case RANKING -> {
                            if (!(game.getScreen() instanceof RankingScreen ranking)) return;
                            if (quadrosNaEtapa == 5) {
                                clicar(ranking.stage(), "aba-infinito");
                            }
                            if (quadrosNaEtapa == 15) {
                                exigir(ranking.modo() == ModoJogo.INFINITO && ranking.dificuldade() == Dificuldade.MEDIO, "Abas não trocaram");
                                exigir(ranking.estado() == TelaCarregavel.Estado.PRONTO, "Ranking não carregou");
                                capturar(saida.resolve("05-ranking-local-infinito.png"));
                                ir(Etapa.PRONTO);
                                Gdx.app.exit();
                            }
                        }
                        case PRONTO -> { }
                    }
                } catch (Throwable erro) { falha.set(erro); Gdx.app.exit(); }
            }
            String texto(GameScreen tela, String nome) {
                var ator = procurar(tela.stage().getRoot(), nome);
                return ator instanceof Label l ? l.getText().toString() : "";
            }
            @Override public void resize(int w, int h) { game.resize(w, h); }
            @Override public void dispose() {
                game.dispose();
                if (falha.get() == null && etapa != Etapa.PRONTO) falha.set(new AssertionError("Interrompida na etapa " + etapa));
                if (falha.get() == null) System.out.println("INFINITO PASSOU: setor 1 concluído, setor 2 mais carregado, corrida encerrada e salva com o setor, aba Infinito do ranking; capturas=" + saida);
            }
        };
        var config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Missão Marte | Verificação do modo Infinito");
        config.setWindowedMode(1280, 720);
        config.setForegroundFPS(60);
        config.useVsync(true);
        new Lwjgl3Application(listener, config);
        if (falha.get() != null) throw new AssertionError("Falha na verificação do Infinito", falha.get());
    }
}
