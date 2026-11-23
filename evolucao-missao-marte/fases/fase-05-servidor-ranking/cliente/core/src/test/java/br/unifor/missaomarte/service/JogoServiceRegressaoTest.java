package br.unifor.missaomarte.service;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import br.unifor.missaomarte.model.*;
import br.unifor.missaomarte.model.EstadoPartida.Situacao;
import br.unifor.missaomarte.model.ResultadoAcao.TipoEvento;
import br.unifor.missaomarte.repository.RankingEntry;
import br.unifor.missaomarte.repository.RankingRepository;

class JogoServiceRegressaoTest {
    @ParameterizedTest
    @CsvSource({
        "5, 5, 0, 1, 0, 5, 0",
        "5, -5, 0, -1, 0, -5, 0",
        "5, 0, 5, 0, 1, 0, 5",
        "5, 0, -5, 0, -1, 0, -5",
        "5, 5, 5, 1, 1, 5, 5",
        "5, -5, -5, -1, -1, -5, -5",
        "5, 5, 5, -1, -1, 4, 4",
        "5, -5, -5, 1, 1, -4, -4",
        "5, 5, 2, 1, -1, 5, 1",
        "1, 1, 1, 1, 1, 1, 1",
        "2, -2, -2, -1, -1, -2, -2",
        "12, 12, 4, 1, -1, 12, 3"
    })
    void inimigosRespeitamLimiteDaPartida(int limite, int x, int y, int dx, int dy, int esperadoX, int esperadoY) {
        var c = cenario(() -> {
            var missao = missaoComPassageiro();
            missao.adicionarInimigo(new Inimigo(x, y));
            return missao;
        }, limite, dx, dy);
        var estado = c.jogo.executar(Acao.EMBARCAR).estado();
        var inimigo = estado.inimigos().get(0);
        assertEquals(esperadoX, inimigo.x());
        assertEquals(esperadoY, inimigo.y());
        assertTrue(Math.abs(inimigo.x()) <= limite && Math.abs(inimigo.y()) <= limite);
        assertEquals(2, c.random.chamadas, "Um sorteio por eixo, sem tentativas adicionais");
    }

    @Test
    void abortarEncerraAntesDeMoverInimigosOuVerificarColisao() {
        var c = cenario(() -> {
            var missao = missaoComPassageiro();
            missao.getNave().perderVida();
            missao.getNave().perderVida();
            missao.adicionarAsteroide(new Asteroide(0, 0));
            missao.adicionarInimigo(new Inimigo(1, 0));
            return missao;
        }, 5, -1, 0);
        var antes = c.jogo.consultarEstado();
        c.tempo[0] = 2_000;
        var resultado = c.jogo.executar(Acao.ABORTAR);
        assertEquals(Situacao.ABORTADA, resultado.estado().situacao());
        assertEquals(antes.nave(), resultado.estado().nave());
        assertEquals(antes.inimigos(), resultado.estado().inimigos());
        assertEquals(antes.pontos(), resultado.estado().pontos());
        assertEquals(antes.movimentos(), resultado.estado().movimentos());
        assertEquals(List.of(TipoEvento.ABORTOU), resultado.eventos().stream().map(e -> e.tipo()).toList());
        assertEquals(0, c.random.chamadas);
        assertEquals(0, c.ranking.gravacoes);
        assertEquals(2, resultado.estado().segundos());
    }

    @ParameterizedTest
    @EnumSource(value = Situacao.class, names = {"VITORIA", "DERROTA", "ABORTADA"})
    void nenhumaAcaoContaMovimentosOuAlteraEstadoAposEncerrar(Situacao situacao) {
        var c = cenario(() -> {
            var missao = new Missao(new Nave("Teste", 0, 0));
            missao.adicionarInimigo(new Inimigo(4, 4));
            if (situacao != Situacao.VITORIA) missao.adicionarPassageiro(new Professor("Ada", 5, 5));
            if (situacao == Situacao.DERROTA) {
                missao.getNave().perderVida();
                missao.getNave().perderVida();
                missao.adicionarAsteroide(new Asteroide(1, 0));
            }
            return missao;
        }, 5, 0, 0);
        Acao ultima = switch (situacao) {
            case VITORIA -> Acao.EMBARCAR;
            case DERROTA -> Acao.DIREITA;
            case ABORTADA -> Acao.ABORTAR;
            default -> throw new AssertionError("Situação não terminal");
        };
        c.tempo[0] = 3_000;
        var finalizado = c.jogo.executar(ultima).estado();
        assertEquals(situacao, finalizado.situacao());
        int sorteios = c.random.chamadas;
        int gravacoes = c.ranking.gravacoes;
        c.tempo[0] = 99_000;
        for (Acao acao : Acao.values()) {
            var resultado = c.jogo.executar(acao);
            assertEquals(finalizado, resultado.estado(), "Estado congelado após " + situacao + ": " + acao);
            assertTrue(resultado.eventos().isEmpty());
        }
        assertEquals(sorteios, c.random.chamadas);
        assertEquals(gravacoes, c.ranking.gravacoes);
    }

    @Test
    void movimentoQueEsgotaPontosContaUmaVezENovosComandosNaoContam() {
        var c = cenario(JogoServiceRegressaoTest::missaoComPassageiro, 5, 0, 0);
        for (int i = 0; i < 20; i++) c.jogo.executar(Acao.DIREITA);
        var fim = c.jogo.consultarEstado();
        assertEquals(Situacao.DERROTA, fim.situacao());
        assertEquals(20, fim.movimentos());
        assertEquals(0, fim.pontos());
        for (int i = 0; i < 100; i++) assertEquals(fim, c.jogo.executar(Acao.ESQUERDA).estado());
    }

    private static Missao missaoComPassageiro() {
        var missao = new Missao(new Nave("Teste", 0, 0));
        missao.adicionarPassageiro(new Professor("Ada", -1, -1));
        return missao;
    }

    private static Cenario cenario(Supplier<Missao> origem, int limite, int dx, int dy) {
        var ranking = new RankingMemoria();
        var random = new RandomPasso(dx, dy);
        long[] tempo = {0};
        var fabrica = new FabricaMissoes() {
            @Override public Missao criar(Dificuldade dificuldade, int raio) { return origem.get(); }
        };
        var jogo = new JogoService(ranking, fabrica, random, () -> tempo[0]);
        jogo.iniciar("Teste", Dificuldade.MEDIO, limite);
        return new Cenario(jogo, ranking, random, tempo);
    }

    private record Cenario(JogoService jogo, RankingMemoria ranking, RandomPasso random, long[] tempo) { }

    private static final class RandomPasso extends Random {
        private static final long serialVersionUID = 1L;
        private final int dx;
        private final int dy;
        int chamadas;
        RandomPasso(int dx, int dy) { this.dx = dx; this.dy = dy; }
        @Override public int nextInt(int bound) { return (chamadas++ % 2 == 0 ? dx : dy) + 1; }
    }

    private static final class RankingMemoria implements RankingRepository {
        int gravacoes;
        @Override public void salvar(String nome, int pontos) { gravacoes++; }
        @Override public void salvar(String nome, int pontos, Dificuldade dificuldade, int passageiros, long segundos) {
            gravacoes++;
        }
        @Override public List<RankingEntry> listar() { return List.of(); }
        @Override public void limpar() { }
    }
}
