package br.unifor.missaomarte.remoto;

import br.unifor.missaomarte.model.Dificuldade;
import br.unifor.missaomarte.model.ModoJogo;

import java.util.List;

/**
 * Porta (Port and Adapter): o que o jogo precisa do servidor, sem saber que é HTTP.
 * Tudo é assíncrono: o resultado chega depois, pelo Retorno, na thread de desenho.
 */
public interface ServidorDoJogo {
    void cadastrar(String nome, String senha, Retorno<String> retorno);
    void entrar(String nome, String senha, Retorno<Sessao> retorno);
    void enviarPartida(Sessao sessao, PartidaConcluida partida, Retorno<PartidaRegistrada> retorno);
    void carregarRanking(Sessao sessao, Retorno<List<LinhaRanking>> retorno);
    /** Fase 7: uma das 9 tabelas (modo × dificuldade). Quem não sabe filtrar devolve o ranking geral. */
    default void carregarRanking(Sessao sessao, ModoJogo modo, Dificuldade dificuldade, Retorno<List<LinhaRanking>> retorno) {
        carregarRanking(sessao, retorno);
    }
    void carregarHistorico(Sessao sessao, Retorno<List<PartidaRegistrada>> retorno);

    /** Aviso sem resposta para o servidor gratuito da nuvem sair do modo de espera antes do login. */
    default void acordar() { }
}
