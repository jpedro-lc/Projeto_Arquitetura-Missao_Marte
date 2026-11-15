package br.unifor.missaomarte.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import br.unifor.missaomarte.MissaoMarteGame;
import br.unifor.missaomarte.lwjgl3.input.TecladoEntradaJogo;
import br.unifor.missaomarte.lwjgl3.repository.RankingArquivoRepository;
import br.unifor.missaomarte.service.JogoService;

/** Ponto de composição exclusivo do desktop. */
public final class Lwjgl3Launcher {
    private Lwjgl3Launcher() { }

    public static void main(String[] args) {
        var configuracao = new Lwjgl3ApplicationConfiguration();
        configuracao.setTitle("Missão Marte | Fase 3 - LibGDX");
        configuracao.setWindowedMode(1100, 760);
        configuracao.useVsync(true);
        configuracao.setForegroundFPS(60);
        var ranking = new RankingArquivoRepository("ranking-solid-exercicio10.json");
        var jogo = new JogoService(ranking);
        new Lwjgl3Application(new MissaoMarteGame(jogo, new TecladoEntradaJogo()), configuracao);
    }
}
