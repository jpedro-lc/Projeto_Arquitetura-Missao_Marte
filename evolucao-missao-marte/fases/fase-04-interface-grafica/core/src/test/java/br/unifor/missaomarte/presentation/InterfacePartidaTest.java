package br.unifor.missaomarte.presentation;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import br.unifor.missaomarte.model.*;
import br.unifor.missaomarte.model.ResultadoAcao.Evento;
import br.unifor.missaomarte.model.ResultadoAcao.TipoEvento;
import br.unifor.missaomarte.presentation.assets.RecursosJogo;
import br.unifor.missaomarte.presentation.audio.AudioPartida;
import br.unifor.missaomarte.presentation.input.ComandoEntrada;
import br.unifor.missaomarte.repository.RankingEntry;
import br.unifor.missaomarte.repository.RankingRepository;
import br.unifor.missaomarte.service.FabricaMissoes;
import br.unifor.missaomarte.service.JogoService;

class InterfacePartidaTest {
    @Test void relogioDescontaPausasSemSaltarNaRetomada() {
        var agora = new AtomicLong(1_000);
        var relogio = new RelogioPausavel(agora::get);
        agora.set(6_000);
        relogio.pausar();
        relogio.pausar();
        agora.set(100_000);
        assertEquals(6_000, relogio.getAsLong());
        relogio.retomar();
        relogio.retomar();
        assertEquals(6_000, relogio.getAsLong());
        agora.set(102_000);
        assertEquals(8_000, relogio.getAsLong());
        relogio.pausar();
        agora.set(150_000);
        relogio.retomar();
        assertEquals(8_000, relogio.getAsLong());
    }

    @Test void pausaCongelaCronometroDoServicoSemMudarSuasRegras() {
        var agora = new AtomicLong();
        var relogio = new RelogioPausavel(agora::get);
        var jogo = jogo(relogio);
        jogo.iniciar("Ana", Dificuldade.MEDIO, 5);
        agora.set(5_000);
        relogio.pausar();
        var pausa = jogo.consultarEstado();
        agora.set(105_000);
        assertEquals(pausa, jogo.consultarEstado());
        relogio.retomar();
        agora.set(107_000);
        assertEquals(7, jogo.consultarEstado().segundos());
        assertEquals(0, jogo.consultarEstado().movimentos());
    }

    @Test void hudApresentaValoresReaisEContaTotalDaMissaoEmVezDeCapacidade() {
        var tempo = new AtomicLong();
        var jogo = jogo(tempo::get);
        jogo.iniciar("Ana", Dificuldade.MEDIO, 5);
        tempo.set(95_000);
        var dados = HudDados.de(jogo.consultarEstado());
        assertEquals("00020", dados.pontos());
        assertEquals("MÉDIO", dados.dificuldade());
        assertEquals("01:35", dados.tempo());
        assertEquals(1, dados.total());
        jogo.executar(Acao.DIREITA);
        jogo.executar(Acao.EMBARCAR);
        dados = HudDados.de(jogo.consultarEstado());
        assertEquals("00034", dados.pontos());
        assertEquals("1", dados.movimentos());
        assertEquals(1, dados.resgatados());
        assertEquals(1, dados.total());
    }

    @Test void resultadosSaoConsumidosUmaVezSemApagarMensagens() {
        var jogo = jogo(() -> 0);
        var controle = new ControladorPartida(jogo, () -> ComandoEntrada.acao(Acao.EMBARCAR));
        controle.atualizar();
        var primeira = controle.consumirResultados();
        assertEquals(1, primeira.size());
        assertFalse(controle.eventos().isEmpty());
        assertTrue(controle.consumirResultados().isEmpty());
        controle.atualizar();
        assertEquals(1, controle.consumirResultados().size(), "Uma nova ação igual precisa produzir novo resultado");
    }

    @Test void reinicioPreservaConfiguracaoELimpaResultadosPendentes() {
        var jogo = jogo(() -> 0);
        var controle = new ControladorPartida(jogo, () -> ComandoEntrada.acao(Acao.DIREITA), "Bia", Dificuldade.FACIL, 7);
        controle.atualizar();
        controle.reiniciar();
        assertEquals("Bia", controle.estado().piloto());
        assertEquals(Dificuldade.FACIL, controle.estado().dificuldade());
        assertEquals(7, controle.estado().limite());
        assertEquals(0, controle.estado().movimentos());
        assertEquals(30, controle.estado().pontos());
        assertTrue(controle.consumirResultados().isEmpty());
    }

    @Test void explosaoTerminaSemCriarOutroTurnoOuRepetirEfeito() {
        var jogo = jogo(() -> 0);
        jogo.iniciar("Ana", Dificuldade.MEDIO, 5);
        var resultado = jogo.executar(Acao.CIMA);
        assertEquals(TipoEvento.COLISAO, resultado.eventos().get(0).tipo());
        var efeitos = new EfeitosPartida();
        efeitos.receber(resultado);
        assertEquals(1, efeitos.ativos().size());
        assertEquals(0, efeitos.ativos().get(0).x());
        assertEquals(1, efeitos.ativos().get(0).y());
        efeitos.atualizar(0.44f);
        assertEquals(1, efeitos.ativos().size());
        efeitos.atualizar(0.45f);
        for (int i = 0; i < 500; i++) efeitos.atualizar(0.016f);
        assertTrue(efeitos.ativos().isEmpty());
        assertEquals(resultado.estado(), jogo.consultarEstado());
    }

    @Test void apenasResgateConfirmadoProduzEfeitoPositivo() {
        var jogo = jogo(() -> 0);
        jogo.iniciar("Ana", Dificuldade.MEDIO, 5);
        var efeitos = new EfeitosPartida();
        efeitos.receber(jogo.executar(Acao.EMBARCAR));
        assertTrue(efeitos.ativos().isEmpty());
        jogo.executar(Acao.DIREITA);
        var resgate = jogo.executar(Acao.EMBARCAR);
        efeitos.receber(resgate);
        assertEquals(EfeitosPartida.Tipo.RESGATE, efeitos.ativos().get(0).tipo());
        efeitos.atualizar(0.71f);
        assertTrue(efeitos.ativos().isEmpty());
        assertEquals(resgate.estado(), jogo.consultarEstado());
    }

    @Test void efeitosSonorosDistinguemImpactoDeAsteroideEInimigoPelosDadosDoCore() {
        var jogo = jogo(() -> 0);
        jogo.iniciar("Ana", Dificuldade.MEDIO, 5);
        var colisao = jogo.executar(Acao.CIMA);
        assertEquals(List.of(RecursosJogo.IMPACTO_ASTEROIDE), AudioPartida.efeitos(colisao));
        var estado = colisao.estado();
        var semAsteroide = new EstadoPartida(estado.piloto(), estado.dificuldade(), estado.limite(), estado.pontos(),
                estado.movimentos(), estado.segundos(), estado.situacao(), estado.nave(), estado.passageiros(),
                List.of(), List.of(new EstadoPartida.Posicao(0, 1)));
        assertEquals(List.of(RecursosJogo.IMPACTO_INIMIGO), AudioPartida.efeitos(new ResultadoAcao(semAsteroide, colisao.eventos())));
    }

    @Test void abortoOuFaltaDePontosNaoInventamExplosao() {
        var jogo = jogo(() -> 0);
        var estado = jogo.iniciar("Ana", Dificuldade.MEDIO, 5);
        var efeitos = new EfeitosPartida();
        efeitos.receber(jogo.executar(Acao.ABORTAR));
        efeitos.receber(new ResultadoAcao(estado, List.of(new Evento(TipoEvento.SEM_PONTOS, "", 0))));
        assertTrue(efeitos.ativos().isEmpty());
    }

    private static JogoService jogo(java.util.function.LongSupplier tempo) {
        var fabrica = new FabricaMissoes() {
            @Override public Missao criar(Dificuldade dificuldade, int limite) {
                var missao = new Missao(new Nave("Teste", 0, 0, 5));
                missao.adicionarPassageiro(new Professor("Ada", 1, 0));
                missao.adicionarAsteroide(new Asteroide(0, 1));
                return missao;
            }
        };
        return new JogoService(new RankingMemoria(), fabrica, new Random(2), tempo);
    }
    private static final class RankingMemoria implements RankingRepository {
        @Override public void salvar(String nome, int pontos) { }
        @Override public void salvar(String nome, int pontos, Dificuldade dificuldade, int passageiros, long tempo) { }
        @Override public List<RankingEntry> listar() { return List.of(); }
        @Override public void limpar() { }
    }
}
