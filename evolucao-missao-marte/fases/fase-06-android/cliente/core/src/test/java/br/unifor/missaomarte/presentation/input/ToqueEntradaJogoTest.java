package br.unifor.missaomarte.presentation.input;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import br.unifor.missaomarte.model.Acao;

class ToqueEntradaJogoTest {
    @Test
    void toqueEmBotaoEnfileiraUmaAcaoEConsomePorQuadro() {
        var toque = new ToqueEntradaJogo();
        toque.acionar(Acao.DIREITA);
        toque.acionar(Acao.EMBARCAR);

        assertEquals(Acao.DIREITA, toque.ler().acao());
        assertEquals(Acao.EMBARCAR, toque.ler().acao());
        assertEquals(Acao.VAZIA, toque.ler().acao(), "Sem novo toque, não pode repetir turno");
    }

    @Test
    void deslizeConverteVetorEmDirecaoPredominanteEIgnoraToqueCurto() {
        var toque = new ToqueEntradaJogo();
        assertFalse(toque.deslizar(10f, -8f), "Arraste abaixo do limiar não deve mover a nave");
        assertEquals(Acao.VAZIA, toque.ler().acao());

        assertTrue(toque.deslizar(60f, 15f));
        assertEquals(Acao.DIREITA, toque.ler().acao());

        assertTrue(toque.deslizar(-55f, -20f));
        assertEquals(Acao.ESQUERDA, toque.ler().acao());

        assertTrue(toque.deslizar(12f, 70f));
        assertEquals(Acao.CIMA, toque.ler().acao());

        assertTrue(toque.deslizar(-10f, -65f));
        assertEquals(Acao.BAIXO, toque.ler().acao());
    }

    @Test
    void pausaReiniciarSairELimparFuncionamPorPulsoUnico() {
        var toque = new ToqueEntradaJogo();
        toque.solicitarPausa();
        assertTrue(toque.pausaSolicitada());
        assertFalse(toque.pausaSolicitada(), "Pausa deve ser consumida em uma única leitura");

        toque.reiniciar();
        assertTrue(toque.ler().reiniciar());

        toque.sair();
        assertTrue(toque.ler().sair());

        toque.acionar(Acao.CIMA);
        toque.solicitarPausa();
        toque.limpar();
        assertEquals(Acao.VAZIA, toque.ler().acao());
        assertFalse(toque.pausaSolicitada());
    }

    @Test
    void entradaCompostaCombinaTecladoEToqueSemPerderSinais() {
        var toqueFisico = new ToqueEntradaJogo();
        var toqueTela = new ToqueEntradaJogo();
        var composta = new EntradaComposta(toqueFisico, toqueTela);

        toqueTela.acionar(Acao.EMBARCAR);
        assertEquals(Acao.EMBARCAR, composta.ler().acao());

        toqueFisico.acionar(Acao.ESQUERDA);
        toqueTela.acionar(Acao.DIREITA);
        assertEquals(Acao.ESQUERDA, composta.ler().acao(), "Primeira fonte ativa tem prioridade no quadro");
        assertEquals(Acao.DIREITA, composta.ler().acao(), "Segunda fonte permanece para o próximo quadro");

        toqueTela.solicitarPausa();
        assertTrue(composta.pausaSolicitada());
        assertFalse(composta.pausaSolicitada());
    }
}
