package br.unifor.missaomarte.remoto;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import br.unifor.missaomarte.model.Dificuldade;
import br.unifor.missaomarte.model.EstadoPartida;
import br.unifor.missaomarte.model.EstadoPartida.Situacao;
import br.unifor.missaomarte.repository.RankingEntry;
import br.unifor.missaomarte.repository.RankingRepository;
import br.unifor.missaomarte.repository.RankingSomenteConvidado;

class DadosDoServidorTest {
    private static EstadoPartida estado(Situacao situacao) {
        return new EstadoPartida("Ana", Dificuldade.FACIL, 5, 41, 9, 75, situacao,
                new EstadoPartida.NaveVista(0, 0, 3, 5, List.of()), List.of(), List.of(), List.of());
    }

    @Test void partidaConcluidaVemDoEstadoFinal() {
        var vitoria = PartidaConcluida.de(estado(Situacao.VITORIA)).orElseThrow();
        assertEquals(new PartidaConcluida(Dificuldade.FACIL, PartidaConcluida.Resultado.VITORIA, 41, 75, 9), vitoria);
        assertEquals(PartidaConcluida.Resultado.DERROTA, PartidaConcluida.de(estado(Situacao.DERROTA)).orElseThrow().resultado());
    }

    @Test void abortoNaoEEnviadoEPartidaEmAndamentoEErro() {
        assertTrue(PartidaConcluida.de(estado(Situacao.ABORTADA)).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> PartidaConcluida.de(estado(Situacao.EM_ANDAMENTO)));
    }

    @Test void sessaoNaoMostraOTokenNoToString() {
        var sessao = new Sessao("ana", "eyJ.segredo.assinatura");
        assertFalse(sessao.toString().contains("segredo"));
        assertTrue(sessao.toString().contains("ana"));
        var atual = new SessaoAtual();
        assertFalse(atual.logado());
        atual.entrar(sessao);
        assertTrue(atual.logado());
        atual.encerrar();
        assertNull(atual.sessao());
    }

    @Test void osTresTiposDeResposta() {
        var ok = Resposta.sucesso(5);
        assertTrue(ok.ok());
        assertEquals(5, ok.valor());
        var erro = Resposta.<Integer>erro("falhou");
        assertFalse(erro.ok());
        assertFalse(erro.naoAutorizado());
        assertThrows(IllegalStateException.class, erro::valor);
        var expirada = Resposta.<Integer>naoAutorizado("expirou");
        assertFalse(expirada.ok());
        assertTrue(expirada.naoAutorizado());
        assertEquals("expirou", expirada.erro());
    }

    @Test void rankingLocalSoGravaNoModoConvidado() {
        var gravados = new ArrayList<String>();
        var local = new RankingRepository() {
            @Override public void salvar(String nome, int pontos) { gravados.add(nome); }
            @Override public void salvar(String nome, int pontos, Dificuldade d, int p, long t) { gravados.add(nome); }
            @Override public List<RankingEntry> listar() { return List.of(); }
            @Override public void limpar() { gravados.clear(); }
        };
        var sessao = new SessaoAtual();
        var ranking = new RankingSomenteConvidado(local, () -> !sessao.logado());
        ranking.salvar("convidado", 30, Dificuldade.MEDIO, 1, 10);
        sessao.entrar(new Sessao("ana", "t"));
        ranking.salvar("ana", 40, Dificuldade.MEDIO, 1, 10);
        assertEquals(List.of("convidado"), gravados);
    }
}
