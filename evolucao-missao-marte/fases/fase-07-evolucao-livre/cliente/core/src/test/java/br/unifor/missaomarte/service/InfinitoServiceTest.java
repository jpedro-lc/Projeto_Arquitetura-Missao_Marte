package br.unifor.missaomarte.service;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import br.unifor.missaomarte.model.*;
import br.unifor.missaomarte.model.EstadoPartida.Situacao;
import br.unifor.missaomarte.model.ResultadoAcao.TipoEvento;
import br.unifor.missaomarte.repository.RankingEntry;
import br.unifor.missaomarte.repository.RankingRepository;

/** Corrida do Infinito com tempo simulado (1/16 s) e mapa fixo; sem relógio, janela ou rede. */
class InfinitoServiceTest {
    static final float QUADRO = 0.0625f;

    record Setor(int limite, int asteroidesExtras, int inimigosExtras, int vidas) { }
    record Gravado(int pontos, long segundos, ModoJogo modo, int setor) { }

    final List<Setor> setoresCriados = new ArrayList<>();
    final List<Gravado> gravados = new ArrayList<>();

    /** Nave na base com as vidas pedidas; asteroide em (0,1); passageiro (15 pontos) em (1,0); inimigos longe. */
    InfinitoService servico() {
        var fabrica = new FabricaMissoes() {
            @Override public Missao criarSetor(Dificuldade d, int limite, int asteroides, int inimigos, int vidas) {
                setoresCriados.add(new Setor(limite, asteroides, inimigos, vidas));
                var missao = new Missao(new Nave("T", 0, 0, 4, vidas));
                missao.adicionarPassageiro(new Professor("Ada", 1, 0));
                missao.adicionarAsteroide(new Asteroide(0, 1));
                missao.adicionarInimigo(new Inimigo(-limite, -limite));
                return missao;
            }
        };
        var parados = new Random() { @Override public int nextInt(int limite) { return 1; } };
        var servico = new InfinitoService(new RankingRepository() {
            @Override public void salvar(String nome, int pontuacao) { fail("sem modo"); }
            @Override public void salvar(String n, int p, Dificuldade d, int q, long t) { fail("sem modo"); }
            @Override public void salvar(String n, int p, Dificuldade d, int q, long t, ModoJogo modo, int setor) {
                gravados.add(new Gravado(p, t, modo, setor));
            }
            @Override public List<RankingEntry> listar() { return List.of(); }
            @Override public void limpar() { }
        }, fabrica, parados);
        servico.iniciar("Ana", Dificuldade.FACIL, 5);
        return servico;
    }

    static ResultadoAcao avancar(InfinitoService s, float segundos, Acao direcao) {
        ResultadoAcao ultimo = null;
        var eventos = new ArrayList<ResultadoAcao.Evento>();
        for (int i = 0; i < Math.round(segundos / QUADRO); i++) {
            ultimo = s.avancar(QUADRO, direcao);
            eventos.addAll(ultimo.eventos());
        }
        return new ResultadoAcao(ultimo.estado(), eventos);
    }

    /** Contagem de 3 s; depois cada direção é segurada por 0,1875 s (o passo da nave é 0,15 s). */
    static ResultadoAcao jogar(InfinitoService s, Acao... direcoes) {
        avancar(s, 3f, Acao.VAZIA);
        var eventos = new ArrayList<ResultadoAcao.Evento>();
        ResultadoAcao ultimo = null;
        for (var d : direcoes) {
            ultimo = avancar(s, 0.1875f, d);
            eventos.addAll(ultimo.eventos());
        }
        return new ResultadoAcao(ultimo.estado(), eventos);
    }

    @Test void comecaNoSetorUmComContagemEPontosDaDificuldade() {
        var s = servico();
        assertEquals(ModoJogo.INFINITO, s.modo());
        assertEquals(1, s.setor());
        assertEquals(3f, s.contagemRestante());
        assertEquals(30, s.consultarEstado().pontos());
        assertEquals(List.of(new Setor(5, 0, 0, 3)), setoresCriados);
    }

    @Test void concluirOSetorLevaAoProximoComPontosAcumuladosENovaContagem() {
        var s = servico();
        var r = jogar(s, Acao.DIREITA, Acao.ESQUERDA);
        assertEquals(2, s.setor());
        assertEquals(1, r.eventos().stream().filter(e -> e.tipo() == TipoEvento.SETOR_CONCLUIDO && e.valor() == 1).count());
        assertTrue(r.eventos().stream().noneMatch(e -> e.tipo() == TipoEvento.VITORIA));
        assertEquals(Situacao.EM_ANDAMENTO, r.estado().situacao());
        // Nova contagem de 3 s (os 2 quadros restantes do trecho já descontaram 0,125 s).
        assertEquals(2.875f, s.contagemRestante());
        // 30 iniciais + 15 do passageiro, nenhum segundo inteiro passou; + 30 de recarga do setor 2.
        assertEquals(75, r.estado().pontos());
        assertEquals(2, r.estado().movimentos());
        assertEquals(1, r.estado().passageiros().size());
        assertTrue(gravados.isEmpty(), "setor concluído não vai para o ranking");
    }

    @Test void vidasPassamDeUmSetorParaOOutro() {
        var s = servico();
        jogar(s, Acao.CIMA, Acao.BAIXO, Acao.DIREITA, Acao.ESQUERDA);
        assertEquals(2, s.setor());
        assertEquals(2, setoresCriados.get(1).vidas());
        assertEquals(2, s.consultarEstado().nave().vidas());
    }

    @Test void movimentosETempoSomamACorridaInteira() {
        var s = servico();
        jogar(s, Acao.DIREITA, Acao.ESQUERDA);
        var antes = s.consultarEstado();
        var depois = jogar(s, Acao.DIREITA).estado();
        assertEquals(antes.movimentos() + 1, depois.movimentos());
        assertTrue(depois.segundos() >= antes.segundos());
    }

    @Test void derrotaEncerraACorridaEGravaUmaVezComModoESetor() {
        var s = servico();
        jogar(s, Acao.DIREITA, Acao.ESQUERDA);
        // Setor 2: parada, os pontos (75) escoam até zerar.
        var fim = avancar(s, 80f, Acao.VAZIA);
        assertEquals(Situacao.DERROTA, fim.estado().situacao());
        assertEquals(2, s.setor());
        assertEquals(1, gravados.size());
        assertEquals(ModoJogo.INFINITO, gravados.get(0).modo());
        assertEquals(2, gravados.get(0).setor());
        avancar(s, 1f, Acao.VAZIA);
        assertEquals(1, gravados.size());
    }

    @Test void abortarNaoGravaEReiniciarVoltaAoSetorUm() {
        var s = servico();
        jogar(s, Acao.DIREITA, Acao.ESQUERDA);
        assertEquals(Situacao.ABORTADA, s.executar(Acao.ABORTAR).estado().situacao());
        assertTrue(gravados.isEmpty());
        s.iniciar("Ana", Dificuldade.FACIL, 5);
        assertEquals(1, s.setor());
        assertEquals(30, s.consultarEstado().pontos());
        assertEquals(0, s.consultarEstado().movimentos());
    }

    @Test void semCorridaIniciadaFalha() {
        var s = new InfinitoService(new RankingRepository() {
            @Override public void salvar(String nome, int pontuacao) { }
            @Override public void salvar(String n, int p, Dificuldade d, int q, long t) { }
            @Override public List<RankingEntry> listar() { return List.of(); }
            @Override public void limpar() { }
        });
        assertEquals(0, s.setor());
        assertEquals(0f, s.contagemRestante());
        assertThrows(IllegalStateException.class, () -> s.avancar(QUADRO, Acao.VAZIA));
    }
}
