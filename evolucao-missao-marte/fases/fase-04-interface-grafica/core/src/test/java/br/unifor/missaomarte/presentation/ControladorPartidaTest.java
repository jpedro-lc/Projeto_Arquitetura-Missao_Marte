package br.unifor.missaomarte.presentation;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import br.unifor.missaomarte.model.*;
import br.unifor.missaomarte.model.EstadoPartida.Situacao;
import br.unifor.missaomarte.presentation.input.ComandoEntrada;
import br.unifor.missaomarte.repository.RankingEntry;
import br.unifor.missaomarte.repository.RankingRepository;
import br.unifor.missaomarte.service.FabricaMissoes;
import br.unifor.missaomarte.service.JogoService;

class ControladorPartidaTest {
    @Test
    void muitosFramesSemEntradaNaoAvancamTurnosMasAtualizamRelogio() {
        var c = new Cenario();
        var antes = c.tela.estado();
        c.tempo[0] = 7_000;
        for (int i = 0; i < 2_000; i++) assertFalse(c.tela.atualizar());
        var depois = c.tela.estado();
        assertAll(
                () -> assertEquals(antes.nave(), depois.nave()),
                () -> assertEquals(antes.inimigos(), depois.inimigos()),
                () -> assertEquals(antes.passageiros(), depois.passageiros()),
                () -> assertEquals(antes.pontos(), depois.pontos()),
                () -> assertEquals(0, depois.movimentos()),
                () -> assertEquals(7, depois.segundos()),
                () -> assertEquals(0, c.ranking.gravacoes));
    }

    @Test
    void umaIntencaoSeguidaDeFramesVaziosExecutaSomenteUmMovimento() {
        var c = new Cenario();
        c.comandos.add(ComandoEntrada.acao(Acao.DIREITA));
        for (int i = 0; i < 120; i++) c.tela.atualizar();
        assertEquals(1, c.tela.estado().nave().x());
        assertEquals(1, c.tela.estado().movimentos());
        assertEquals(19, c.tela.estado().pontos());
    }

    @Test
    void pausaNaoConsomeEntradaERetomadaPermiteUmaAcao() {
        var c = new Cenario();
        c.comandos.add(ComandoEntrada.acao(Acao.CIMA));
        c.tela.pausar();
        c.tela.atualizar();
        assertEquals(1, c.comandos.size());
        assertEquals(0, c.tela.estado().movimentos());
        c.tela.retomar();
        c.tela.atualizar();
        assertEquals(1, c.tela.estado().nave().y());
        assertEquals(1, c.tela.estado().movimentos());
    }

    @Test
    void reinicioSoAconteceDepoisDoEncerramentoESaidaNaoAbortaAMissao() {
        var c = new Cenario();
        c.comandos.add(new ComandoEntrada(Acao.VAZIA, true, false));
        c.tela.atualizar();
        assertEquals(1, c.criacoes);
        c.comandos.add(new ComandoEntrada(Acao.VAZIA, false, true));
        assertTrue(c.tela.atualizar());
        assertEquals(Situacao.EM_ANDAMENTO, c.tela.estado().situacao());
        c.comandos.add(ComandoEntrada.acao(Acao.ABORTAR));
        c.tela.atualizar();
        assertEquals(Situacao.ABORTADA, c.tela.estado().situacao());
        c.comandos.add(new ComandoEntrada(Acao.VAZIA, true, false));
        c.tela.atualizar();
        assertEquals(2, c.criacoes);
        assertEquals(Situacao.EM_ANDAMENTO, c.tela.estado().situacao());
        assertEquals(20, c.tela.estado().pontos());
        assertTrue(c.tela.eventos().isEmpty());
    }

    @Test
    void embarqueRetornoEVitoriaUsamRegrasESalvamUmaUnicaVez() {
        var c = new Cenario();
        for (Acao acao : new Acao[] {Acao.DIREITA, Acao.EMBARCAR, Acao.ESQUERDA}) {
            c.comandos.add(ComandoEntrada.acao(acao));
            c.tela.atualizar();
        }
        assertEquals(Situacao.VITORIA, c.tela.estado().situacao());
        assertEquals(33, c.tela.estado().pontos());
        assertEquals(1, c.ranking.gravacoes);
        for (int i = 0; i < 60; i++) c.tela.atualizar();
        assertEquals(1, c.ranking.gravacoes);
        assertEquals(Situacao.VITORIA, c.tela.estado().situacao());
    }

    private static final class Cenario {
        final ArrayDeque<ComandoEntrada> comandos = new ArrayDeque<>();
        final long[] tempo = {0};
        final RankingMemoria ranking = new RankingMemoria();
        final ControladorPartida tela;
        int criacoes;

        Cenario() {
            var fabrica = new FabricaMissoes() {
                @Override
                public Missao criar(Dificuldade dificuldade, int limite) {
                    criacoes++;
                    var missao = new Missao(new Nave("Teste", 0, 0, 5));
                    missao.adicionarPassageiro(new Professor("Ada", 1, 0));
                    missao.adicionarInimigo(new Inimigo(4, 4));
                    return missao;
                }
            };
            var jogo = new JogoService(ranking, fabrica, new Random(3), () -> tempo[0]);
            tela = new ControladorPartida(jogo,
                    () -> comandos.isEmpty() ? ComandoEntrada.nenhum() : comandos.removeFirst());
        }
    }

    private static final class RankingMemoria implements RankingRepository {
        int gravacoes;
        @Override public void salvar(String nome, int pontuacao) { gravacoes++; }
        @Override public void salvar(String nome, int pontos, Dificuldade dificuldade, int passageiros, long tempo) {
            gravacoes++;
        }
        @Override public List<RankingEntry> listar() { return List.of(); }
        @Override public void limpar() { }
    }
}
