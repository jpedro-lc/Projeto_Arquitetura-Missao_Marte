package br.unifor.missaomarte.lwjgl3;

import static br.unifor.missaomarte.lwjgl3.VerificacaoDesktop.capturar;
import static br.unifor.missaomarte.lwjgl3.VerificacaoDesktop.clicar;
import static br.unifor.missaomarte.lwjgl3.VerificacaoDesktop.exigir;
import static br.unifor.missaomarte.lwjgl3.VerificacaoDesktop.procurar;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import br.unifor.missaomarte.MissaoMarteGame;
import br.unifor.missaomarte.lwjgl3.input.TecladoEntradaJogo;
import br.unifor.missaomarte.model.*;
import br.unifor.missaomarte.model.EstadoPartida.Situacao;
import br.unifor.missaomarte.presentation.RelogioPausavel;
import br.unifor.missaomarte.presentation.rede.ServidorHttp;
import br.unifor.missaomarte.remoto.*;
import br.unifor.missaomarte.repository.RankingEntry;
import br.unifor.missaomarte.repository.RankingRepository;
import br.unifor.missaomarte.repository.RankingSomenteConvidado;
import br.unifor.missaomarte.screen.*;
import br.unifor.missaomarte.service.FabricaMissoes;
import br.unifor.missaomarte.service.JogoService;

/**
 * Ponta a ponta com o servidor REAL no ar: cadastro, login, envio de vitória e derrota, ranking,
 * histórico, senha errada, servidor desligado e reenvio com R. Gera as capturas da Fase 5.
 * Uso: gradlew :lwjgl3:smokeOnline (com o servidor em bootRun).
 */
public final class VerificacaoOnline {
    private record Passo(String nome, BooleanSupplier pronto, Runnable acao) { }

    private VerificacaoOnline() { }

    public static void main(String[] args) {
        Path saida = Path.of(args[0]).toAbsolutePath();
        var real = new ServidorHttp(args[1]);
        var desligado = new ServidorHttp("http://127.0.0.1:9");
        var servidor = new Trocavel(real);
        var falha = new AtomicReference<Throwable>();
        var tecla = new AtomicInteger(-1);
        var sessao = new SessaoAtual();
        var gravacoesLocais = new AtomicInteger();
        var local = new RankingRepository() {
            @Override public void salvar(String n, int p) { gravacoesLocais.incrementAndGet(); }
            @Override public void salvar(String n, int p, Dificuldade d, int ps, long t) { gravacoesLocais.incrementAndGet(); }
            @Override public List<RankingEntry> listar() { return List.of(); }
            @Override public void limpar() { }
        };
        var fabrica = new FabricaMissoes() {
            @Override public Missao criar(Dificuldade dificuldade, int limite) {
                var missao = new Missao(new Nave("Teste", 0, 0, 5));
                missao.adicionarPassageiro(new Professor("Ada", 1, 0));
                missao.adicionarAsteroide(new Asteroide(0, 1));
                return missao;
            }
        };
        var relogio = new RelogioPausavel(System::currentTimeMillis);
        var jogo = new JogoService(new RankingSomenteConvidado(local, () -> !sessao.logado()), fabrica, new Random(3), relogio);
        var game = new MissaoMarteGame(jogo, new TecladoEntradaJogo(codigo -> codigo == tecla.get()), relogio, servidor, sessao);
        String sufixo = Long.toString(System.currentTimeMillis() % 1_000_000);
        String ana = "ana_" + sufixo;
        String bia = "bia_" + sufixo;
        String senha = "senha-segura-1";
        var rankingFinal = new AtomicReference<List<LinhaRanking>>();

        var passos = new ArrayList<Passo>();
        // Atalhos de leitura do estado da janela.
        java.util.function.Supplier<Stage> palco = () -> game.getScreen() instanceof GameScreen g ? g.stage()
                : ((TelaPainel) game.getScreen()).stage();
        java.util.function.Function<String, String> texto = nome -> {
            var ator = procurar(palco.get().getRoot(), nome);
            return ator instanceof Label l ? l.getText().toString() : "";
        };
        BooleanSupplier menu = () -> game.getScreen() instanceof MenuScreen;
        BooleanSupplier login = () -> game.getScreen() instanceof LoginScreen;
        BooleanSupplier nova = () -> game.getScreen() instanceof NovaPartidaScreen;
        BooleanSupplier partida = () -> game.getScreen() instanceof GameScreen;
        java.util.function.Consumer<String> digitar = t -> { for (char c : t.toCharArray()) ((LoginScreen) game.getScreen()).digitar(c); };
        java.util.function.BiConsumer<String, Path> foto = (n, p) -> passos.add(new Passo("captura " + n, () -> true, () -> capturar(p)));
        java.util.function.Consumer<String> clique = nome -> passos.add(new Passo("clicar " + nome, () -> true, () -> clicar(palco.get(), nome)));
        java.util.function.Consumer<int[]> teclas = codigos -> {
            for (int codigo : codigos) {
                passos.add(new Passo("tecla " + codigo, () -> true, () -> tecla.set(codigo)));
                passos.add(new Passo("solta", () -> true, () -> tecla.set(-1)));
            }
        };
        BooleanSupplier enviado = () -> partida.getAsBoolean() && ((GameScreen) game.getScreen()).envio() == GameScreen.Envio.ENVIADO;

        passos.add(new Passo("menu convidado", menu, () -> capturar(saida.resolve("01-menu-convidado.png"))));
        // Cadastro de Ana (entra automaticamente).
        clique.accept("ENTRAR OU CRIAR CONTA");
        passos.add(new Passo("criar conta", login, () -> {
            ((LoginScreen) game.getScreen()).tecla(Keys.F2);
            digitar.accept(ana + "\t" + senha);
        }));
        foto.accept("cadastro", saida.resolve("02-cadastro-senha-mascarada.png"));
        passos.add(new Passo("confirmar cadastro", () -> true, () -> digitar.accept("\r")));
        passos.add(new Passo("menu logado", () -> menu.getAsBoolean() && sessao.logado(), () -> {
            exigir(texto.apply("modo").contains(ana), "Menu não mostra o piloto");
            capturar(saida.resolve("03-menu-logado.png"));
        }));
        // Vitória logada: nome vem da conta, resultado vai ao servidor.
        clique.accept("INICIAR JOGO");
        passos.add(new Passo("nova partida", nova, () -> capturar(saida.resolve("04-nova-partida-logado.png"))));
        clique.accept("DECOLAR");
        passos.add(new Passo("partida", partida, () -> exigir(jogo.consultarEstado().piloto().equals(ana), "Piloto não é a conta")));
        teclas.accept(new int[] {Keys.D, Keys.C, Keys.A});
        passos.add(new Passo("vitória enviada", () -> enviado.getAsBoolean() && texto.apply("status-envio").contains("Top 10"), () -> {
            exigir(jogo.consultarEstado().situacao() == Situacao.VITORIA, "Vitória esperada");
            exigir(gravacoesLocais.get() == 0, "Logado não pode gravar no ranking local");
            capturar(saida.resolve("05-vitoria-enviada.png"));
        }));
        clique.accept("VOLTAR AO MENU");
        passos.add(new Passo("menu", menu, () -> clicar(palco.get(), "RANKING GLOBAL")));
        passos.add(new Passo("ranking", () -> game.getScreen() instanceof RankingScreen r && r.estado() == TelaCarregavel.Estado.PRONTO,
                () -> capturar(saida.resolve("06-ranking-global.png"))));
        clique.accept("VOLTAR");
        // Derrota logada também vai para o histórico (W colide; C repete a colisão).
        passos.add(new Passo("menu", menu, () -> clicar(palco.get(), "INICIAR JOGO")));
        passos.add(new Passo("nova", nova, () -> clicar(palco.get(), "DECOLAR")));
        passos.add(new Passo("partida", partida, () -> { }));
        teclas.accept(new int[] {Keys.W, Keys.C, Keys.C});
        passos.add(new Passo("derrota enviada", enviado, () -> {
            exigir(jogo.consultarEstado().situacao() == Situacao.DERROTA, "Derrota esperada");
            capturar(saida.resolve("07-derrota-enviada.png"));
        }));
        clique.accept("VOLTAR AO MENU");
        passos.add(new Passo("menu", menu, () -> clicar(palco.get(), "MEU HISTÓRICO")));
        passos.add(new Passo("histórico", () -> game.getScreen() instanceof HistoricoScreen h && h.estado() == TelaCarregavel.Estado.PRONTO,
                () -> capturar(saida.resolve("08-historico.png"))));
        clique.accept("VOLTAR");
        // Servidor cai durante a partida: falha visível, R reenvia quando ele volta.
        passos.add(new Passo("menu", menu, () -> clicar(palco.get(), "INICIAR JOGO")));
        passos.add(new Passo("nova", nova, () -> clicar(palco.get(), "DECOLAR")));
        passos.add(new Passo("partida", partida, () -> servidor.alvo = desligado));
        teclas.accept(new int[] {Keys.D, Keys.C, Keys.A});
        passos.add(new Passo("falha de envio", () -> partida.getAsBoolean()
                && ((GameScreen) game.getScreen()).envio() == GameScreen.Envio.FALHOU, () -> {
            exigir(texto.apply("status-envio").contains("Não foi possível conectar"), "Mensagem de conexão ausente");
            capturar(saida.resolve("09-falha-envio.png"));
            servidor.alvo = real;
        }));
        teclas.accept(new int[] {Keys.R});
        passos.add(new Passo("reenvio", enviado, () -> {
            exigir(jogo.consultarEstado().movimentos() == 2, "R reiniciou a partida em vez de reenviar");
            capturar(saida.resolve("10-reenvio-com-r.png"));
        }));
        clique.accept("VOLTAR AO MENU");
        // Sair da conta, senha errada, servidor desligado no login.
        passos.add(new Passo("menu", menu, () -> clicar(palco.get(), "SAIR DA CONTA")));
        passos.add(new Passo("convidado", () -> menu.getAsBoolean() && !sessao.logado(), () -> {
            exigir(texto.apply("modo").contains("CONVIDADO"), "Menu não voltou ao convidado");
            capturar(saida.resolve("11-saiu-da-conta.png"));
            clicar(palco.get(), "ENTRAR OU CRIAR CONTA");
        }));
        passos.add(new Passo("senha errada", login, () -> digitar.accept(ana + "\tsenha-errada-9\r")));
        passos.add(new Passo("mensagem genérica", () -> login.getAsBoolean() && texto.apply("login-status").contains("inválidos"),
                () -> capturar(saida.resolve("12-senha-errada.png"))));
        passos.add(new Passo("servidor desligado", () -> true, () -> {
            servidor.alvo = desligado;
            var l = (LoginScreen) game.getScreen();
            for (int i = 0; i < 20; i++) l.digitar('\b');
            l.digitar('\t');
            for (int i = 0; i < 20; i++) l.digitar('\b');
            digitar.accept(ana + "\t" + senha + "\r");
        }));
        passos.add(new Passo("sem conexão", () -> login.getAsBoolean() && texto.apply("login-status").contains("conectar"), () -> {
            capturar(saida.resolve("13-servidor-desligado.png"));
            ((LoginScreen) game.getScreen()).tecla(Keys.ESCAPE);
        }));
        // Convidado continua jogando sem servidor e grava no ranking local.
        passos.add(new Passo("menu", menu, () -> clicar(palco.get(), "INICIAR JOGO")));
        passos.add(new Passo("nova", nova, () -> clicar(palco.get(), "DECOLAR")));
        passos.add(new Passo("partida convidado", partida, () -> { }));
        teclas.accept(new int[] {Keys.D, Keys.C, Keys.A});
        passos.add(new Passo("vitória convidado", () -> jogo.consultarEstado().situacao() == Situacao.VITORIA, () -> {
            exigir(gravacoesLocais.get() == 1, "Convidado deve gravar no ranking local");
            exigir(((GameScreen) game.getScreen()).envio() == GameScreen.Envio.NENHUM, "Convidado não envia ao servidor");
            servidor.alvo = real;
        }));
        clique.accept("VOLTAR AO MENU");
        // Segundo jogador com pontuação maior (FÁCIL começa com 30 pontos).
        passos.add(new Passo("menu", menu, () -> clicar(palco.get(), "ENTRAR OU CRIAR CONTA")));
        passos.add(new Passo("conta bia", login, () -> {
            ((LoginScreen) game.getScreen()).tecla(Keys.F2);
            digitar.accept(bia + "\t" + senha + "\r");
        }));
        passos.add(new Passo("menu bia", () -> menu.getAsBoolean() && sessao.logado(), () -> clicar(palco.get(), "INICIAR JOGO")));
        passos.add(new Passo("fácil", nova, () -> { clicar(palco.get(), "FÁCIL"); clicar(palco.get(), "DECOLAR"); }));
        passos.add(new Passo("partida bia", partida, () -> { }));
        teclas.accept(new int[] {Keys.D, Keys.C, Keys.A});
        passos.add(new Passo("vitória bia", enviado, () -> clicar(palco.get(), "VOLTAR AO MENU")));
        passos.add(new Passo("menu", menu, () -> {
            clicar(palco.get(), "RANKING GLOBAL");
            real.carregarRanking(sessao.sessao(), r -> rankingFinal.set(r.ok() ? r.valor() : List.of()));
        }));
        passos.add(new Passo("ranking com dois", () -> rankingFinal.get() != null && game.getScreen() instanceof RankingScreen r
                && r.estado() == TelaCarregavel.Estado.PRONTO, () -> {
            var nomes = rankingFinal.get().stream().map(LinhaRanking::nome).toList();
            exigir(nomes.stream().filter(ana::equals).count() == 1, "Ana deve aparecer uma vez: " + nomes);
            exigir(nomes.stream().filter(bia::equals).count() == 1, "Bia deve aparecer uma vez: " + nomes);
            exigir(nomes.indexOf(bia) < nomes.indexOf(ana), "Bia (43) deve ficar acima de Ana (33)");
            capturar(saida.resolve("14-ranking-dois-jogadores.png"));
        }));
        clique.accept("VOLTAR");
        passos.add(new Passo("sair", menu, () -> clicar(palco.get(), "SAIR")));

        var indice = new AtomicInteger();
        var esperando = new AtomicInteger();
        var listener = new ApplicationAdapter() {
            @Override public void create() { game.create(); }
            @Override public void render() {
                try {
                    game.render();
                    if (indice.get() >= passos.size()) return;
                    var passo = passos.get(indice.get());
                    if (passo.pronto().getAsBoolean()) {
                        passo.acao().run();
                        indice.incrementAndGet();
                        esperando.set(0);
                    } else if (esperando.incrementAndGet() > 900) {
                        throw new AssertionError("Tempo esgotado em: " + passo.nome());
                    }
                } catch (Throwable erro) { falha.set(erro); Gdx.app.exit(); }
            }
            @Override public void resize(int w, int h) { game.resize(w, h); }
            @Override public void dispose() {
                game.dispose();
                if (falha.get() == null && indice.get() < passos.size()) falha.set(new AssertionError("Roteiro interrompido"));
                if (falha.get() == null) System.out.println("SMOKE ONLINE PASSOU: " + passos.size() + " passos; contas "
                        + ana + " e " + bia + "; capturas=" + saida);
            }
        };
        var config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Missão Marte | Verificação ponta a ponta");
        config.setWindowedMode(1280, 800);
        config.setForegroundFPS(60);
        config.useVsync(true);
        new Lwjgl3Application(listener, config);
        if (falha.get() != null) throw new AssertionError("Falha na verificação ponta a ponta", falha.get());
    }

    /** Permite "desligar" o servidor no meio do roteiro sem fechar a janela. */
    private static final class Trocavel implements ServidorDoJogo {
        volatile ServidorDoJogo alvo;
        Trocavel(ServidorDoJogo alvo) { this.alvo = alvo; }
        @Override public void cadastrar(String n, String s, Retorno<String> r) { alvo.cadastrar(n, s, r); }
        @Override public void entrar(String n, String s, Retorno<Sessao> r) { alvo.entrar(n, s, r); }
        @Override public void enviarPartida(Sessao s, PartidaConcluida p, Retorno<PartidaRegistrada> r) { alvo.enviarPartida(s, p, r); }
        @Override public void carregarRanking(Sessao s, Retorno<List<LinhaRanking>> r) { alvo.carregarRanking(s, r); }
        @Override public void carregarHistorico(Sessao s, Retorno<List<PartidaRegistrada>> r) { alvo.carregarHistorico(s, r); }
    }
}
