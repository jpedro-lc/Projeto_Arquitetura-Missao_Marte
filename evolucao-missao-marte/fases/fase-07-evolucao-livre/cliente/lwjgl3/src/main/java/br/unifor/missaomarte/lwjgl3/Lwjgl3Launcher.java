package br.unifor.missaomarte.lwjgl3;

import java.util.Random;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import br.unifor.missaomarte.MissaoMarteGame;
import br.unifor.missaomarte.lwjgl3.input.TecladoEntradaJogo;
import br.unifor.missaomarte.lwjgl3.repository.RankingArquivoRepository;
import br.unifor.missaomarte.presentation.RelogioPausavel;
import br.unifor.missaomarte.presentation.rede.ServidorHttp;
import br.unifor.missaomarte.remoto.SessaoAtual;
import br.unifor.missaomarte.repository.RankingSomenteConvidado;
import br.unifor.missaomarte.service.FabricaMissoes;
import br.unifor.missaomarte.service.JogoService;

/** Ponto de composição exclusivo do desktop. */
public final class Lwjgl3Launcher {
    static final String SERVIDOR_PADRAO = "http://localhost:8080";

    private Lwjgl3Launcher() { }

    public static void main(String[] args) {
        var configuracao = new Lwjgl3ApplicationConfiguration();
        configuracao.setTitle("Missão Marte | Resgate Espacial");
        configuracao.setWindowedMode(1280, 800);
        configuracao.useVsync(true);
        configuracao.setForegroundFPS(60);
        var sessao = new SessaoAtual();
        // Convidado grava a vitória no arquivo local; logado, o resultado vai para o servidor.
        var ranking = new RankingSomenteConvidado(new RankingArquivoRepository("ranking-solid-exercicio10.json"),
                () -> !sessao.logado());
        var relogio = new RelogioPausavel(System::currentTimeMillis);
        var jogo = new JogoService(ranking, new FabricaMissoes(), new Random(), relogio);
        var servidor = new ServidorHttp(System.getProperty("missaomarte.servidor", SERVIDOR_PADRAO));
        new Lwjgl3Application(new MissaoMarteGame(jogo, new TecladoEntradaJogo(), relogio, servidor, sessao), configuracao);
    }
}
