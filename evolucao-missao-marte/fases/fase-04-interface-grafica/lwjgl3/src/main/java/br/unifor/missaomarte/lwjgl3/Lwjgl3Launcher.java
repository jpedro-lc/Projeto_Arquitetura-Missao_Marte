package br.unifor.missaomarte.lwjgl3;

import java.util.Random;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import br.unifor.missaomarte.MissaoMarteGame;
import br.unifor.missaomarte.lwjgl3.input.TecladoEntradaJogo;
import br.unifor.missaomarte.lwjgl3.repository.RankingArquivoRepository;
import br.unifor.missaomarte.service.JogoService;
import br.unifor.missaomarte.service.FabricaMissoes;
import br.unifor.missaomarte.presentation.RelogioPausavel;

/** Ponto de composição exclusivo do desktop. */
public final class Lwjgl3Launcher {
    private Lwjgl3Launcher() { }

    public static void main(String[] args) {
        var configuracao = new Lwjgl3ApplicationConfiguration();
        configuracao.setTitle("Missão Marte | Resgate Espacial");
        configuracao.setWindowedMode(1280, 800);
        configuracao.useVsync(true);
        configuracao.setForegroundFPS(60);
        var ranking = new RankingArquivoRepository("ranking-solid-exercicio10.json");
        var relogio = new RelogioPausavel(System::currentTimeMillis);
        var jogo = new JogoService(ranking, new FabricaMissoes(), new Random(), relogio);
        new Lwjgl3Application(new MissaoMarteGame(jogo, new TecladoEntradaJogo(), relogio), configuracao);
    }
}
