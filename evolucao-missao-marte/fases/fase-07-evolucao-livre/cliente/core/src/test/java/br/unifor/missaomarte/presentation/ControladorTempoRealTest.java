package br.unifor.missaomarte.presentation;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import br.unifor.missaomarte.model.*;
import br.unifor.missaomarte.model.EstadoPartida.Situacao;
import br.unifor.missaomarte.presentation.input.ComandoEntrada;
import br.unifor.missaomarte.presentation.input.EntradaJogo;
import br.unifor.missaomarte.repository.RankingEntry;
import br.unifor.missaomarte.repository.RankingRepository;
import br.unifor.missaomarte.service.FabricaMissoes;
import br.unifor.missaomarte.service.RitmoTempoReal;
import br.unifor.missaomarte.service.TempoRealService;

/** O controlador repassa o tempo do quadro e a direção mantida para as regras do tempo real. */
class ControladorTempoRealTest {
    static final float QUADRO = 0.0625f;
    final ArrayDeque<ComandoEntrada> comandos = new ArrayDeque<>();
    Acao segurando = Acao.VAZIA;

    ControladorPartida controlador() {
        var fabrica = new FabricaMissoes() {
            @Override public Missao criar(Dificuldade dificuldade, int limite) {
                var missao = new Missao(new Nave("T", 0, 0, 4));
                missao.adicionarPassageiro(new Professor("Ada", 3, 3));
                return missao;
            }
        };
        var regras = new TempoRealService(new RankingRepository() {
            @Override public void salvar(String nome, int pontuacao) { }
            @Override public void salvar(String n, int p, Dificuldade d, int q, long t) { }
            @Override public List<RankingEntry> listar() { return List.of(); }
            @Override public void limpar() { }
        }, fabrica, new Random(1), d -> new RitmoTempoReal(0.25f, 0.5f, 0.5f, 1f, 1f));
        EntradaJogo entrada = new EntradaJogo() {
            @Override public ComandoEntrada ler() { return comandos.isEmpty() ? ComandoEntrada.nenhum() : comandos.removeFirst(); }
            @Override public Acao direcaoMantida() { return segurando; }
        };
        return new ControladorPartida(regras, entrada, "Ana", Dificuldade.FACIL, 5);
    }

    static void quadros(ControladorPartida c, int n) { for (int i = 0; i < n; i++) c.atualizar(QUADRO); }

    @Test void tempoDoQuadroFazAPartidaAndarSemNenhumaTecla() {
        var c = controlador();
        assertEquals(ModoJogo.TEMPO_REAL, c.modo());
        assertEquals(0.5f, c.contagemRestante());
        quadros(c, 8);
        assertEquals(0f, c.contagemRestante());
        quadros(c, 32);
        assertEquals(28, c.estado().pontos());
        assertEquals(2, c.estado().segundos());
    }

    @Test void segurarMoveVariasVezesEOToqueRapidoValeUmPasso() {
        var c = controlador();
        quadros(c, 8);
        segurando = Acao.DIREITA;
        quadros(c, 8);
        assertEquals(2, c.estado().nave().x());
        segurando = Acao.VAZIA;
        quadros(c, 4);
        comandos.add(ComandoEntrada.acao(Acao.CIMA));
        c.atualizar(QUADRO);
        assertEquals(1, c.estado().nave().y());
        assertEquals(3, c.estado().movimentos());
        assertEquals(3, c.consumirResultados().size());
    }

    @Test void pausadoOTempoNaoAndaEAbortarEncerra() {
        var c = controlador();
        quadros(c, 8);
        c.pausar();
        quadros(c, 40);
        assertEquals(30, c.estado().pontos());
        c.retomar();
        comandos.add(ComandoEntrada.acao(Acao.ABORTAR));
        c.atualizar(QUADRO);
        assertEquals(Situacao.ABORTADA, c.estado().situacao());
    }

    @Test void reiniciarDepoisDoFimVoltaAContagem() {
        var c = controlador();
        quadros(c, 8);
        comandos.add(ComandoEntrada.acao(Acao.ABORTAR));
        c.atualizar(QUADRO);
        comandos.add(new ComandoEntrada(Acao.VAZIA, true, false));
        c.atualizar(QUADRO);
        assertEquals(Situacao.EM_ANDAMENTO, c.estado().situacao());
        assertEquals(0.5f, c.contagemRestante());
        assertEquals(2, c.geracao());
    }
}
