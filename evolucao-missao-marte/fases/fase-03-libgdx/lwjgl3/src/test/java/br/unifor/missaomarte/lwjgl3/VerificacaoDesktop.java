package br.unifor.missaomarte.lwjgl3;

import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import br.unifor.missaomarte.MissaoMarteGame;
import br.unifor.missaomarte.lwjgl3.input.TecladoEntradaJogo;
import br.unifor.missaomarte.model.*;
import br.unifor.missaomarte.model.EstadoPartida.Situacao;
import br.unifor.missaomarte.presentation.RelogioPausavel;
import br.unifor.missaomarte.presentation.assets.RecursosJogo;
import br.unifor.missaomarte.repository.RankingEntry;
import br.unifor.missaomarte.repository.RankingRepository;
import br.unifor.missaomarte.screen.GameScreen;
import br.unifor.missaomarte.screen.MenuScreen;
import br.unifor.missaomarte.service.FabricaMissoes;
import br.unifor.missaomarte.service.JogoService;

/** Integração gráfica real. Entrada determinística e cliques no Stage, sem tocar outros aplicativos. */
public final class VerificacaoDesktop {
    private VerificacaoDesktop() { }
    public static void main(String[] args) {
        Path saida = Path.of(args[0]).toAbsolutePath();
        var falha = new AtomicReference<Throwable>();
        var quadro = new AtomicInteger();
        var ranking = new RankingMemoria();
        var fabrica = new FabricaMissoes() {
            @Override public Missao criar(Dificuldade dificuldade, int limite) {
                var missao = new Missao(new Nave("Teste", 0, 0, 5));
                missao.adicionarPassageiro(new Professor("Ada", 1, 0));
                missao.adicionarAsteroide(new Asteroide(0, 1));
                missao.adicionarInimigo(new Inimigo(5, 5));
                return missao;
            }
        };
        var relogio = new RelogioPausavel(() -> quadro.get() * 100L);
        var jogo = new JogoService(ranking, fabrica, new Random(3), relogio);
        var teclado = new TecladoEntradaJogo(tecla -> tecla == switch (quadro.get()) {
            case 25 -> Keys.SPACE;
            case 35, 360 -> Keys.P;
            case 80, 120, 320 -> Keys.D;
            case 95, 215, 280 -> Keys.C;
            case 110 -> Keys.A;
            case 150 -> Keys.W;
            case 345 -> Keys.R;
            case 350 -> Keys.ESCAPE;
            case 375 -> Keys.Q;
            default -> -1;
        });
        var game = new MissaoMarteGame(jogo, teclado, relogio);
        var listener = new ApplicationAdapter() {
            private EstadoPartida pausa;
            private EstadoPartida vitoria;
            private EstadoPartida derrota;
            @Override public void create() { game.create(); }
            @Override public void render() {
                try {
                    int f = quadro.incrementAndGet();
                    game.render();
                    if (f >= 400) throw new AssertionError("Sair não encerrou aplicação");
                    if (f == 16) exigir(game.getScreen() instanceof GameScreen, "Iniciar não abriu a partida");
                    if (f == 2) { exigir(game.getScreen() instanceof MenuScreen, "Menu inicial ausente"); capturar(saida.resolve("01-menu.png")); }
                    if (f == 5) clicar(((MenuScreen) game.getScreen()).stage(), "INSTRUÇÕES");
                    if (f == 8) { exigir(((MenuScreen) game.getScreen()).emInstrucoes(), "Instruções ausentes"); capturar(saida.resolve("02-instrucoes.png")); }
                    if (f == 10) clicar(((MenuScreen) game.getScreen()).stage(), "VOLTAR");
                    if (f == 14) clicar(((MenuScreen) game.getScreen()).stage(), "INICIAR JOGO");
                    if (f < 15 || game.getScreen() instanceof MenuScreen) {
                        if (f == 370) exigir(jogo.consultarEstado().situacao() == Situacao.ABORTADA, "Menu não encerrou a missão");
                        if (f == 372) clicar(((MenuScreen) game.getScreen()).stage(), "INICIAR JOGO");
                        if (f == 390) clicar(((MenuScreen) game.getScreen()).stage(), "SAIR");
                        return;
                    }
                    var tela = (GameScreen) game.getScreen();
                    var estado = jogo.consultarEstado();
                    exigir(estado.inimigos().stream().allMatch(i -> Math.abs(i.x()) <= estado.limite()
                            && Math.abs(i.y()) <= estado.limite()), "Inimigo fora do mapa");
                    if (f == 16) exigir(estado.movimentos() == 0 && estado.pontos() == 20, "Frame virou turno");
                    if (f == 30) {
                        conferirHud(tela.stage(), "hud-pontos", "00020");
                        conferirHud(tela.stage(), "hud-passageiros", "0/1");
                        exigir(game.recursos().musica(RecursosJogo.MUSICA).isPlaying(), "Música não iniciou");
                        capturar(saida.resolve("03-jogo.png"));
                    }
                    if (f == 36) { exigir(tela.pausado(), "P não pausou"); pausa = estado; }
                    if (f == 45) capturar(saida.resolve("04-pausa.png"));
                    if (f == 50) Gdx.graphics.setWindowedMode(820, 820);
                    if (f == 69) {
                        exigir(estado.equals(pausa), "Pausa alterou estado ou cronômetro");
                        exigir(!game.recursos().musica(RecursosJogo.MUSICA).isPlaying(), "Música não pausou");
                        capturar(saida.resolve("05-quadrado.png"));
                    }
                    if (f == 70) clicar(tela.stage(), "CONTINUAR");
                    if (f == 71) exigir(!tela.pausado() && estado.movimentos() == 0, "Continuar recriou/avançou partida");
                    if (f == 81) exigir(estado.movimentos() == 1 && estado.pontos() == 19, "Movimento duplicado");
                    if (f == 85) Gdx.graphics.setWindowedMode(1280, 720);
                    if (f == 98) {
                        conferirHud(tela.stage(), "hud-passageiros", "1/1");
                        capturar(saida.resolve("06-resgate.png"));
                    }
                    if (f == 115) { exigir(estado.situacao() == Situacao.VITORIA && estado.pontos() == 33, "Vitória alterada"); vitoria = estado; }
                    if (f == 125) {
                        exigir(estado.equals(vitoria) && ranking.gravacoes == 1, "Comando após vitória alterou partida");
                        exigir(!game.recursos().musica(RecursosJogo.MUSICA).isPlaying(), "Música não parou na vitória");
                    }
                    if (f == 130) capturar(saida.resolve("07-vitoria.png"));
                    if (f == 140) clicar(tela.stage(), "REINICIAR");
                    if (f == 151) exigir(estado.nave().vidas() == 2 && tela.efeitosAtivos() == 1, "Colisão não gerou efeito único");
                    if (f == 185) capturar(saida.resolve("08-colisao.png"));
                    if (f == 210) exigir(tela.efeitosAtivos() == 0 && estado.movimentos() == 1, "Efeito repetiu/avançou turno");
                    if (f == 281) { exigir(estado.situacao() == Situacao.DERROTA, "Derrota por colisão ausente"); derrota = estado; }
                    if (f == 300) {
                        exigir(game.recursos().musica(RecursosJogo.DERROTA).isPlaying(), "Game Over não iniciou");
                        capturar(saida.resolve("09-derrota.png"));
                    }
                    if (f == 340) exigir(estado.equals(derrota) && tela.efeitosAtivos() == 0, "Comando após derrota alterou partida");
                    if (f == 346 || f == 356) exigir(estado.movimentos() == 0 && estado.pontos() == 20 && estado.nave().vidas() == 3, "Reinício incorreto");
                    if (f == 351) exigir(tela.pausado(), "ESC encerrou a aplicação em vez de abrir opções");
                    if (f == 355) clicar(tela.stage(), "REINICIAR PARTIDA");
                    if (f == 365) clicar(tela.stage(), "VOLTAR AO MENU");
                    if (f == 376) exigir(estado.situacao() == Situacao.ABORTADA && estado.movimentos() == 0, "Q não abortou imediatamente");
                    if (f == 380) game.pausarPartida();
                    if (f == 385) clicar(tela.stage(), "VOLTAR AO MENU");
                } catch (Throwable erro) { falha.set(erro); Gdx.app.exit(); }
            }
            @Override public void resize(int w, int h) { game.resize(w, h); }
            @Override public void pause() { game.pause(); }
            @Override public void resume() { game.resume(); }
            @Override public void dispose() {
                try {
                    game.dispose();
                    exigir(quadro.get() >= 390, "Verificação interrompida");
                    exigir(ranking.gravacoes == 1, "Ranking duplicado");
                    exigir(Gdx.gl.glGetError() == 0, "Erro OpenGL");
                    if (falha.get() == null) System.out.println("SMOKE PASSOU: menu, instruções, HUD, pausa/tempo, áudio, limites, efeitos, vitória, derrota, resize e dispose; capturas=" + saida);
                } catch (Throwable erro) { falha.compareAndSet(null, erro); }
            }
        };
        var config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Missão Marte | Verificação gráfica");
        config.setWindowedMode(1280, 800);
        config.setForegroundFPS(60);
        config.useVsync(true);
        new Lwjgl3Application(listener, config);
        if (falha.get() != null) throw new AssertionError("Falha na verificação gráfica", falha.get());
    }
    private static void clicar(Stage stage, String nome) {
        Actor botao = procurar(stage.getRoot(), nome);
        exigir(botao != null, "Botão não encontrado: " + nome);
        Vector2 ponto = botao.localToStageCoordinates(new Vector2(botao.getWidth() / 2, botao.getHeight() / 2));
        stage.stageToScreenCoordinates(ponto);
        stage.touchDown(Math.round(ponto.x), Math.round(ponto.y), 0, 0);
        stage.touchUp(Math.round(ponto.x), Math.round(ponto.y), 0, 0);
    }
    private static Actor procurar(Actor ator, String nome) {
        if (!ator.isVisible()) return null;
        if (nome.equals(ator.getName())) return ator;
        if (ator instanceof Group grupo) for (Actor filho : grupo.getChildren()) {
            Actor achado = procurar(filho, nome);
            if (achado != null) return achado;
        }
        return null;
    }
    private static void conferirHud(Stage stage, String nome, String esperado) {
        Label valor = (Label) procurar(stage.getRoot(), nome);
        exigir(valor != null && esperado.contentEquals(valor.getText()), "HUD incorreto: " + nome);
    }
    private static void capturar(Path destino) {
        var arquivo = Gdx.files.absolute(destino.toString());
        arquivo.parent().mkdirs();
        Pixmap captura = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        try { PixmapIO.writePNG(arquivo, captura, -1, true); } finally { captura.dispose(); }
    }
    private static void exigir(boolean condicao, String mensagem) { if (!condicao) throw new AssertionError(mensagem); }
    private static final class RankingMemoria implements RankingRepository {
        int gravacoes;
        @Override public void salvar(String nome, int pontos) { gravacoes++; }
        @Override public void salvar(String nome, int pontos, Dificuldade dificuldade, int passageiros, long tempo) { gravacoes++; }
        @Override public List<RankingEntry> listar() { return List.of(); }
        @Override public void limpar() { }
    }
}
