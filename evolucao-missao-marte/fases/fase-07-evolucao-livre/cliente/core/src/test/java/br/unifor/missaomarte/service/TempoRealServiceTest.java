package br.unifor.missaomarte.service;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import br.unifor.missaomarte.model.*;
import br.unifor.missaomarte.model.EstadoPartida.Posicao;
import br.unifor.missaomarte.model.EstadoPartida.Situacao;
import br.unifor.missaomarte.model.ResultadoAcao.TipoEvento;
import br.unifor.missaomarte.repository.RankingEntry;
import br.unifor.missaomarte.repository.RankingRepository;

/** Tempo simulado em frações exatas em binário (1/16 s, abaixo do limite de 0,1 s por quadro), sem relógio, janela ou rede. */
class TempoRealServiceTest {
    static final float QUADRO = 0.0625f;
    /** Nave a cada 0,25 s, inimigos a cada 0,5 s, contagem de 1 s, 1 s invulnerável, 1 ponto por segundo. */
    static final RitmoTempoReal RITMO = new RitmoTempoReal(0.25f, 0.5f, 1f, 1f, 1f);

    final List<String> vitoriasSalvas = new ArrayList<>();
    Missao mapa;

    /** Nave na base (0,0); Professor (15 pontos) em (2,0); asteroide em (0,2); inimigo em (-4,-4). */
    TempoRealService servico(Random inimigos) {
        var fabrica = new FabricaMissoes() {
            @Override public Missao criar(Dificuldade dificuldade, int limite) {
                mapa = new Missao(new Nave("T", 0, 0, 4));
                mapa.adicionarPassageiro(new Professor("Ada", 2, 0));
                mapa.adicionarAsteroide(new Asteroide(0, 2));
                mapa.adicionarInimigo(new Inimigo(-4, -4));
                return mapa;
            }
        };
        return new TempoRealService(new RankingRepository() {
            @Override public void salvar(String nome, int pontuacao) { vitoriasSalvas.add(nome); }
            @Override public void salvar(String nome, int p, Dificuldade d, int passageiros, long tempo) { vitoriasSalvas.add(nome); }
            @Override public List<RankingEntry> listar() { return List.of(); }
            @Override public void limpar() { }
        }, fabrica, inimigos, d -> RITMO);
    }

    /** Inimigos parados: nextInt(3) = 1 vira deslocamento 0. */
    static Random inimigosParados() {
        return new Random() { @Override public int nextInt(int limite) { return 1; } };
    }

    TempoRealService iniciado() {
        var s = servico(inimigosParados());
        s.iniciar("Ana", Dificuldade.FACIL, 5);
        return s;
    }

    static ResultadoAcao avancar(TempoRealService s, float segundos, Acao direcao) {
        ResultadoAcao ultimo = null;
        var eventos = new ArrayList<ResultadoAcao.Evento>();
        for (int i = 0; i < Math.round(segundos / QUADRO); i++) {
            ultimo = s.avancar(QUADRO, direcao);
            eventos.addAll(ultimo.eventos());
        }
        return new ResultadoAcao(ultimo.estado(), eventos);
    }

    static void largar(TempoRealService s) { avancar(s, 1f, Acao.VAZIA); }

    @Test void contagemCongelaTudoMesmoComDirecaoPressionada() {
        var s = iniciado();
        assertEquals(1f, s.contagemRestante());
        var estado = avancar(s, 0.875f, Acao.DIREITA).estado();
        assertEquals(0, estado.nave().x());
        assertEquals(30, estado.pontos());
        assertEquals(0, estado.segundos());
        assertTrue(s.contagemRestante() > 0);
        avancar(s, 0.125f, Acao.DIREITA);
        assertEquals(0f, s.contagemRestante());
    }

    @Test void primeiraDirecaoMoveNaHoraESegurarRepeteNoRitmoDaNave() {
        var s = iniciado();
        largar(s);
        assertEquals(1, s.avancar(QUADRO, Acao.CIMA).estado().nave().y());
        // Segurando, o próximo passo só vem no quadro que completa 0,25 s.
        assertEquals(1, avancar(s, 0.1875f, Acao.CIMA).estado().nave().y());
        assertEquals(2, s.avancar(QUADRO, Acao.CIMA).estado().nave().y());
    }

    @Test void soltarParaETocarDeNovoMoveSemEsperarOIntervaloInteiro() {
        var s = iniciado();
        largar(s);
        s.avancar(QUADRO, Acao.BAIXO);
        avancar(s, 0.5f, Acao.VAZIA);
        assertEquals(-2, s.avancar(QUADRO, Acao.BAIXO).estado().nave().y());
        assertEquals(2, s.consultarEstado().movimentos());
    }

    @Test void pontosEscoamUmPorSegundoSoDepoisDaLargada() {
        var s = iniciado();
        largar(s);
        var estado = avancar(s, 2.5f, Acao.VAZIA).estado();
        assertEquals(28, estado.pontos());
        assertEquals(2, estado.segundos());
    }

    @Test void passarPeloPassageiroEmbarcaSozinhoEDaPontos() {
        var s = iniciado();
        largar(s);
        var r = avancar(s, 0.5f, Acao.DIREITA);
        assertEquals(2, r.estado().nave().x());
        assertTrue(r.eventos().stream().anyMatch(e -> e.tipo() == TipoEvento.EMBARCOU && e.nome().equals("Ada")));
        assertTrue(r.estado().passageiros().isEmpty());
        assertEquals(30 + 15, r.estado().pontos());
    }

    @Test void voltarABaseComTodosEmbarcadosVenceESalvaUmaVez() {
        var s = iniciado();
        largar(s);
        var ida = avancar(s, 0.5f, Acao.DIREITA);
        var volta = avancar(s, 0.5f, Acao.ESQUERDA);
        assertTrue(ida.eventos().stream().anyMatch(e -> e.tipo() == TipoEvento.RETORNAR_BASE));
        assertEquals(Situacao.VITORIA, volta.estado().situacao());
        assertEquals(1, volta.eventos().stream().filter(e -> e.tipo() == TipoEvento.VITORIA).count());
        avancar(s, 1f, Acao.ESQUERDA);
        assertEquals(List.of("Ana"), vitoriasSalvas);
    }

    @Test void colisaoTiraUmaVidaEDaInvulnerabilidadeTemporaria() {
        var s = iniciado();
        largar(s);
        var bate = avancar(s, 0.5f, Acao.CIMA);
        assertEquals(new Posicao(0, 2), new Posicao(bate.estado().nave().x(), bate.estado().nave().y()));
        assertEquals(2, bate.estado().nave().vidas());
        assertEquals(1, bate.eventos().stream().filter(e -> e.tipo() == TipoEvento.COLISAO).count());
        // Parada sobre o asteroide: protegida durante 1 s, depois perde outra vida.
        assertEquals(2, avancar(s, 0.75f, Acao.VAZIA).estado().nave().vidas());
        assertEquals(1, avancar(s, 0.5f, Acao.VAZIA).estado().nave().vidas());
    }

    @Test void terceiraColisaoEncerraEmDerrota() {
        var s = iniciado();
        largar(s);
        avancar(s, 0.5f, Acao.CIMA);
        var r = avancar(s, 3f, Acao.VAZIA);
        assertEquals(Situacao.DERROTA, r.estado().situacao());
        assertEquals(0, r.estado().nave().vidas());
        assertTrue(r.eventos().stream().anyMatch(e -> e.tipo() == TipoEvento.NAVE_DESTRUIDA));
        assertEquals(Situacao.DERROTA, s.avancar(QUADRO, Acao.CIMA).estado().situacao());
    }

    @Test void pontosZeradosEncerramEmDerrota() {
        var s = iniciado();
        largar(s);
        var r = avancar(s, 30f, Acao.VAZIA);
        assertEquals(Situacao.DERROTA, r.estado().situacao());
        assertEquals(0, r.estado().pontos());
        assertEquals(1, r.eventos().stream().filter(e -> e.tipo() == TipoEvento.SEM_PONTOS).count());
        assertTrue(vitoriasSalvas.isEmpty());
    }

    @Test void inimigosAndamSozinhosSoNoRitmoDeles() {
        var direita = new Random() { @Override public int nextInt(int limite) { return 2; } };
        var s = servico(direita);
        s.iniciar("Ana", Dificuldade.FACIL, 5);
        largar(s);
        assertEquals(new Posicao(-4, -4), avancar(s, 0.375f, Acao.VAZIA).estado().inimigos().get(0));
        assertEquals(new Posicao(-3, -3), avancar(s, 0.125f, Acao.VAZIA).estado().inimigos().get(0));
        assertEquals(new Posicao(-1, -1), avancar(s, 1f, Acao.VAZIA).estado().inimigos().get(0));
    }

    @Test void quadroLongoNaoViraSaltoNoTempo() {
        var s = iniciado();
        largar(s);
        s.avancar(10f, Acao.VAZIA);
        assertEquals(0.1f, TempoRealService.QUADRO_MAXIMO);
        assertEquals(30, s.consultarEstado().pontos());
    }

    @Test void abortarEncerraENadaMaisAnda() {
        var s = iniciado();
        largar(s);
        assertEquals(Situacao.ABORTADA, s.executar(Acao.ABORTAR).estado().situacao());
        var depois = avancar(s, 2f, Acao.DIREITA).estado();
        assertEquals(0, depois.nave().x());
        assertEquals(30, depois.pontos());
    }

    @Test void naBordaDoMapaANaveNaoSaiNemContaMovimento() {
        var s = iniciado();
        largar(s);
        var estado = avancar(s, 3f, Acao.ESQUERDA).estado();
        assertEquals(-5, estado.nave().x());
        assertEquals(5, estado.movimentos());
    }

    @Test void ritmoPorDificuldadeAceleraOsInimigos() {
        assertEquals(0.9f, RitmoTempoReal.de(Dificuldade.FACIL).passoInimigos());
        assertEquals(0.7f, RitmoTempoReal.de(Dificuldade.MEDIO).passoInimigos());
        assertEquals(0.5f, RitmoTempoReal.de(Dificuldade.DIFICIL).passoInimigos());
        assertEquals(0.15f, RitmoTempoReal.de(Dificuldade.MEDIO).passoNave());
        assertThrows(IllegalArgumentException.class, () -> new RitmoTempoReal(0, 1, 1, 1, 1));
    }

    @Test void semPartidaIniciadaFalhaComMensagemClara() {
        var s = servico(inimigosParados());
        assertThrows(IllegalStateException.class, () -> s.avancar(QUADRO, Acao.VAZIA));
        assertEquals(0f, s.contagemRestante());
        assertEquals(ModoJogo.TEMPO_REAL, s.modo());
    }
}
