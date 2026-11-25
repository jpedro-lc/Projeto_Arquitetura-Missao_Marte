package br.unifor.missaomarte.android;

import java.io.File;
import java.util.Random;
import android.os.Build;
import android.os.Bundle;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import br.unifor.missaomarte.MissaoMarteGame;
import br.unifor.missaomarte.R;
import br.unifor.missaomarte.presentation.RelogioPausavel;
import br.unifor.missaomarte.presentation.input.ControleFisicoEntradaJogo;
import br.unifor.missaomarte.presentation.rede.EnderecoServidor;
import br.unifor.missaomarte.presentation.rede.ServidorHttp;
import br.unifor.missaomarte.remoto.SessaoAtual;
import br.unifor.missaomarte.repository.RankingLocalRepository;
import br.unifor.missaomarte.repository.RankingSomenteConvidado;
import br.unifor.missaomarte.service.FabricaMissoes;
import br.unifor.missaomarte.service.JogoService;

/**
 * Ponto de composição exclusivo do Android.
 * Reutiliza integralmente o módulo :core (regras, telas, controles por toque e cliente HTTP).
 */
public final class AndroidLauncher extends AndroidApplication {
    private static final String ARQUIVO_RANKING = "ranking-solid-exercicio10.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        var configuracao = new AndroidApplicationConfiguration();
        configuracao.useAccelerometer = false;
        configuracao.useCompass = false;
        configuracao.useImmersiveMode = true;

        var sessao = new SessaoAtual();
        // No Android, a raiz do sistema é somente leitura; usamos o diretório interno da aplicação (getFilesDir()).
        var arquivoLocal = new File(getFilesDir(), ARQUIVO_RANKING);
        var ranking = new RankingSomenteConvidado(new RankingLocalRepository(arquivoLocal), () -> !sessao.logado());
        var relogio = new RelogioPausavel(System::currentTimeMillis);
        var jogo = new JogoService(ranking, new FabricaMissoes(), new Random(), relogio);

        // Endereço do build (-Pmissaomarte.servidor) ou, se vazio, automático: emulador -> 10.0.2.2, celular -> 127.0.0.1.
        var servidor = new ServidorHttp(EnderecoServidor.android(getString(R.string.servidor_url), emulador()));

        // MissaoMarteGame compõe ControleFisicoEntradaJogo (tecla Voltar e controle Bluetooth) com ToqueEntradaJogo.
        initialize(new MissaoMarteGame(jogo, new ControleFisicoEntradaJogo(), relogio, servidor, sessao), configuracao);
    }

    /** Heurística usual para o AVD do Android Studio (imagens goldfish/ranchu e builds "sdk"/"generic"). */
    static boolean emulador() {
        return Build.FINGERPRINT.startsWith("generic") || Build.FINGERPRINT.contains("emulator")
                || Build.HARDWARE.contains("goldfish") || Build.HARDWARE.contains("ranchu")
                || Build.PRODUCT.contains("sdk");
    }
}
