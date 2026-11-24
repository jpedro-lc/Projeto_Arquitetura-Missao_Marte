package br.unifor.missaomarte.presentation;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import br.unifor.missaomarte.model.*;
import br.unifor.missaomarte.presentation.EstadoVisualBase.Fase;
import br.unifor.missaomarte.repository.RankingEntry;
import br.unifor.missaomarte.repository.RankingRepository;
import br.unifor.missaomarte.service.FabricaMissoes;
import br.unifor.missaomarte.service.JogoService;

class AnimacaoVisualTest {
    @Test void naveDeslizaEntreCelulasSemExecutarTurno() {
        var jogo = jogo();
        var inicial = jogo.iniciar("Ana", Dificuldade.MEDIO, 5);
        var animador = new AnimadorPosicoes();
        animador.fixar(inicial);
        var depois = jogo.executar(Acao.DIREITA).estado();
        animador.acompanhar(depois);
        assertEquals(0f, animador.naveX(), 1e-4);
        animador.atualizar(AnimadorPosicoes.DURACAO / 2);
        assertTrue(animador.naveX() > 0.2f && animador.naveX() < 0.8f, "posição intermediária");
        assertTrue(animador.emMovimento());
        for (int i = 0; i < 100; i++) { animador.acompanhar(jogo.consultarEstado()); animador.atualizar(0.016f); }
        assertEquals(1f, animador.naveX(), 1e-4);
        assertFalse(animador.emMovimento());
        assertEquals(depois, jogo.consultarEstado(), "Frames de animação não podem virar turnos");
    }

    @Test void novaTeclaCortaAnimacaoAPartirDaPosicaoDesenhada() {
        var jogo = jogo();
        var animador = new AnimadorPosicoes();
        animador.fixar(jogo.iniciar("Ana", Dificuldade.MEDIO, 5));
        animador.acompanhar(jogo.executar(Acao.DIREITA).estado());
        animador.atualizar(AnimadorPosicoes.DURACAO / 2);
        float meio = animador.naveX();
        animador.acompanhar(jogo.executar(Acao.ESQUERDA).estado());
        assertEquals(meio, animador.naveX(), 1e-4, "sem salto ao trocar de destino");
        animador.atualizar(1);
        assertEquals(0f, animador.naveX(), 1e-4);
    }

    @Test void baseSoInterpretaSituacaoDecididaPeloCore() {
        var jogo = jogo();
        var base = new EstadoVisualBase();
        assertEquals(Fase.OCIOSA, base.atualizar(jogo.iniciar("Ana", Dificuldade.MEDIO, 5), 0.1f));
        jogo.executar(Acao.DIREITA);
        assertEquals(Fase.ATIVA, base.atualizar(jogo.executar(Acao.EMBARCAR).estado(), 0.1f));
        var vitoria = jogo.executar(Acao.ESQUERDA).estado();
        assertEquals(EstadoPartida.Situacao.VITORIA, vitoria.situacao(), "vitória imediata, sem esperar o pouso");
        assertEquals(Fase.POUSO, base.atualizar(vitoria, 0.016f));
        assertEquals(Fase.POUSO, base.atualizar(vitoria, EstadoVisualBase.DURACAO_POUSO / 2));
        assertEquals(Fase.CONCLUIDA, base.atualizar(vitoria, EstadoVisualBase.DURACAO_POUSO));
        assertEquals(vitoria, jogo.consultarEstado());
        assertEquals(Fase.OCIOSA, base.atualizar(jogo.iniciar("Ana", Dificuldade.MEDIO, 5), 0.1f), "reinício volta ao início");
    }

    private static JogoService jogo() {
        var fabrica = new FabricaMissoes() {
            @Override public Missao criar(Dificuldade dificuldade, int limite) {
                var missao = new Missao(new Nave("Teste", 0, 0, 5));
                missao.adicionarPassageiro(new Professor("Ada", 1, 0));
                return missao;
            }
        };
        return new JogoService(new RankingRepository() {
            @Override public void salvar(String nome, int pontos) { }
            @Override public void salvar(String nome, int pontos, Dificuldade d, int passageiros, long tempo) { }
            @Override public List<RankingEntry> listar() { return List.of(); }
            @Override public void limpar() { }
        }, fabrica, new Random(2), () -> 0);
    }
}
