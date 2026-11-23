package br.unifor.missaomarte.lwjgl3;

import java.util.List;
import br.unifor.missaomarte.remoto.LinhaRanking;
import br.unifor.missaomarte.remoto.PartidaConcluida;
import br.unifor.missaomarte.remoto.PartidaRegistrada;
import br.unifor.missaomarte.remoto.Resposta;
import br.unifor.missaomarte.remoto.Retorno;
import br.unifor.missaomarte.remoto.ServidorDoJogo;
import br.unifor.missaomarte.remoto.Sessao;

/** Servidor falso para o smoke offline: o modo convidado não depende de rede. */
final class ServidorIndisponivel implements ServidorDoJogo {
    private static <T> void falhar(Retorno<T> retorno) { retorno.receber(Resposta.erro("Servidor indisponível.")); }
    @Override public void cadastrar(String nome, String senha, Retorno<String> retorno) { falhar(retorno); }
    @Override public void entrar(String nome, String senha, Retorno<Sessao> retorno) { falhar(retorno); }
    @Override public void enviarPartida(Sessao s, PartidaConcluida p, Retorno<PartidaRegistrada> retorno) { falhar(retorno); }
    @Override public void carregarRanking(Sessao s, Retorno<List<LinhaRanking>> retorno) { falhar(retorno); }
    @Override public void carregarHistorico(Sessao s, Retorno<List<PartidaRegistrada>> retorno) { falhar(retorno); }
}
