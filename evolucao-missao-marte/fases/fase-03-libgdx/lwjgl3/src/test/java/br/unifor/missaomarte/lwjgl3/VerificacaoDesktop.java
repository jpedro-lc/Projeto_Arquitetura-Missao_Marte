package br.unifor.missaomarte.lwjgl3;

import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import br.unifor.missaomarte.MissaoMarteGame;
import br.unifor.missaomarte.lwjgl3.input.TecladoEntradaJogo;
import br.unifor.missaomarte.model.*;
import br.unifor.missaomarte.model.EstadoPartida.Situacao;
import br.unifor.missaomarte.repository.RankingEntry;
import br.unifor.missaomarte.repository.RankingRepository;
import br.unifor.missaomarte.service.FabricaMissoes;
import br.unifor.missaomarte.service.JogoService;

/** Janela/OpenGL reais, com entrada determinística injetada apenas nesta verificação. */
public final class VerificacaoDesktop {
    private VerificacaoDesktop() { }

    public static void main(String[] args) {
        Path saida = Path.of(args[0]).toAbsolutePath();
        var falha = new AtomicReference<Throwable>();
        var quadro = new AtomicInteger();
        var ranking = new RankingMemoria();
        var fabrica = new FabricaMissoes() {
            @Override
            public Missao criar(Dificuldade dificuldade, int limite) {
                var missao = new Missao(new Nave("Teste", 0, 0, 5));
                missao.adicionarPassageiro(new Professor("Ada", 1, 0));
                missao.adicionarAsteroide(new Asteroide(-3, -2));
                missao.adicionarInimigo(new Inimigo(4, 4));
                return missao;
            }
        };
        var jogo = new JogoService(ranking, fabrica, new Random(3), () -> quadro.get() * 16L);
        var teclado = new TecladoEntradaJogo(tecla -> tecla == switch (quadro.get()) {
            case 45 -> Keys.D;
            case 80 -> Keys.C;
            case 105 -> Keys.A;
            case 135, 185 -> Keys.R;
            case 160 -> Keys.Q;
            case 210 -> Keys.ESCAPE;
            default -> -1;
        });
        var game = new MissaoMarteGame(jogo, teclado);
        var listener = new ApplicationAdapter() {
            private EstadoPartida inicial;
            private boolean descartado;

            @Override
            public void create() {
                game.create();
                inicial = jogo.consultarEstado();
            }

            @Override
            public void render() {
                try {
                    int f = quadro.incrementAndGet();
                    game.render();
                    var estado = jogo.consultarEstado();
                    if (f == 30) {
                        exigir(estado.movimentos() == 0 && estado.pontos() == 20, "Frame virou turno");
                        exigir(estado.inimigos().equals(inicial.inimigos()), "Inimigo moveu sem entrada");
                        capturar(saida.resolve("01-inicial.png"));
                    }
                    if (f == 46) exigir(estado.movimentos() == 1 && estado.pontos() == 19, "Entrada duplicada");
                    if (f == 50) Gdx.graphics.setWindowedMode(820, 820);
                    if (f == 65) capturar(saida.resolve("02-quadrado.png"));
                    if (f == 90) Gdx.graphics.setWindowedMode(1200, 700);
                    if (f == 105) {
                        exigir(estado.situacao() == Situacao.VITORIA && estado.pontos() == 33, "Regra de vitória alterada");
                        exigir(ranking.gravacoes == 1, "Ranking não foi salvo uma única vez");
                    }
                    if (f == 120) {
                        exigir(ranking.gravacoes == 1, "Render repetiu gravação do ranking");
                        capturar(saida.resolve("03-vitoria.png"));
                    }
                    if (f == 136 || f == 186) exigir(estado.situacao() == Situacao.EM_ANDAMENTO
                            && estado.pontos() == 20 && estado.movimentos() == 0, "Reinício incorreto");
                    if (f == 161) exigir(estado.situacao() == Situacao.ABORTADA, "Aborto incorreto");
                    if (f >= 220) throw new AssertionError("Esc não encerrou a aplicação");
                } catch (Throwable erro) {
                    falha.set(erro);
                    Gdx.app.exit();
                }
            }

            @Override public void resize(int width, int height) { game.resize(width, height); }
            @Override public void pause() { game.pause(); }
            @Override public void resume() { game.resume(); }

            @Override
            public void dispose() {
                try {
                    game.dispose();
                    descartado = true;
                    exigir(quadro.get() >= 210, "Janela fechada antes de concluir o cenário");
                    exigir(ranking.gravacoes == 1, "Gravação inesperada no ranking");
                    exigir(Gdx.gl.glGetError() == 0, "Erro OpenGL ao renderizar/descartar");
                    System.out.println("SMOKE DESKTOP PASSOU: texturas, frames, ações, vitória, resize, reinício e dispose; quadros="
                            + quadro.get() + "; capturas=" + saida);
                } catch (Throwable erro) {
                    falha.compareAndSet(null, erro);
                }
                exigir(descartado, "Recursos não descartados");
            }
        };
        var configuracao = new Lwjgl3ApplicationConfiguration();
        configuracao.setTitle("Missão Marte | Verificação Fase 3");
        configuracao.setWindowedMode(1100, 760);
        configuracao.setForegroundFPS(60);
        configuracao.useVsync(true);
        new Lwjgl3Application(listener, configuracao);
        if (falha.get() != null) throw new AssertionError("Falha na verificação gráfica", falha.get());
    }

    private static void capturar(Path destino) {
        var arquivo = Gdx.files.absolute(destino.toString());
        arquivo.parent().mkdirs();
        Pixmap captura = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        try {
            PixmapIO.writePNG(arquivo, captura, -1, true);
        } finally {
            captura.dispose();
        }
    }

    private static void exigir(boolean condicao, String mensagem) {
        if (!condicao) throw new AssertionError(mensagem);
    }

    private static final class RankingMemoria implements RankingRepository {
        int gravacoes;
        @Override public void salvar(String nome, int pontos) { gravacoes++; }
        @Override public void salvar(String nome, int pontos, Dificuldade dificuldade, int passageiros, long tempo) {
            gravacoes++;
        }
        @Override public List<RankingEntry> listar() { return List.of(); }
        @Override public void limpar() { }
    }
}
