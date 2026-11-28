package br.unifor.missaomarte.service;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import br.unifor.missaomarte.model.Dificuldade;

class ProgressaoInfinitaTest {
    @Test void obstaculosCrescemAosPoucos() {
        int[] setores = {1, 2, 3, 4, 5, 10};
        int[] inimigos = {0, 0, 1, 1, 2, 4};
        int[] asteroides = {0, 0, 0, 1, 1, 3};
        for (int i = 0; i < setores.length; i++) {
            assertEquals(inimigos[i], ProgressaoInfinita.inimigosExtras(setores[i]), "inimigos no setor " + setores[i]);
            assertEquals(asteroides[i], ProgressaoInfinita.asteroidesExtras(setores[i]), "asteroides no setor " + setores[i]);
        }
    }

    @Test void mapaCresceACadaQuatroSetoresAteOMaximo() {
        assertEquals(5, ProgressaoInfinita.limite(5, 1));
        assertEquals(5, ProgressaoInfinita.limite(5, 4));
        assertEquals(6, ProgressaoInfinita.limite(5, 5));
        assertEquals(7, ProgressaoInfinita.limite(5, 9));
        assertEquals(7, ProgressaoInfinita.limite(5, 100));
        assertEquals(9, ProgressaoInfinita.limite(9, 50), "limite inicial maior que o máximo não encolhe");
    }

    @Test void inimigosAceleram8PorCentoPorSetorComPiso() {
        assertEquals(0.9f, ProgressaoInfinita.ritmo(Dificuldade.FACIL, 1).passoInimigos(), 1e-6);
        assertEquals(0.9f * 0.92f, ProgressaoInfinita.ritmo(Dificuldade.FACIL, 2).passoInimigos(), 1e-6);
        assertEquals(0.5f * 0.92f * 0.92f, ProgressaoInfinita.ritmo(Dificuldade.DIFICIL, 3).passoInimigos(), 1e-6);
        assertEquals(ProgressaoInfinita.PASSO_MINIMO_INIMIGOS, ProgressaoInfinita.ritmo(Dificuldade.DIFICIL, 40).passoInimigos());
        // A nave não fica mais lenta nem a contagem muda.
        assertEquals(RitmoTempoReal.PASSO_NAVE, ProgressaoInfinita.ritmo(Dificuldade.MEDIO, 12).passoNave());
        assertEquals(RitmoTempoReal.CONTAGEM, ProgressaoInfinita.ritmo(Dificuldade.MEDIO, 12).contagem());
    }

    @Test void vidaExtraNosSetores6E11ComTeto() {
        assertEquals(2, ProgressaoInfinita.vidasAoEntrar(2, 2));
        assertEquals(3, ProgressaoInfinita.vidasAoEntrar(2, 6));
        assertEquals(1, ProgressaoInfinita.vidasAoEntrar(1, 10));
        assertEquals(2, ProgressaoInfinita.vidasAoEntrar(1, 11));
        assertEquals(5, ProgressaoInfinita.vidasAoEntrar(5, 16));
        assertEquals(3, ProgressaoInfinita.vidasAoEntrar(3, 1), "o setor 1 não dá vida extra");
    }

    @Test void setorZeroOuNegativoEErro() {
        assertThrows(IllegalArgumentException.class, () -> ProgressaoInfinita.inimigosExtras(0));
        assertThrows(IllegalArgumentException.class, () -> ProgressaoInfinita.ritmo(Dificuldade.FACIL, -1));
    }

    @Test void fabricaDoSetorSoAcrescentaObstaculosEGuardaAsVidas() {
        var missao = new FabricaMissoes(new java.util.Random(4)).criarSetor(Dificuldade.FACIL, 5, 2, 3, 4);
        assertEquals(4, missao.getPassageiros().size());
        assertEquals(2 + 2, missao.getAsteroides().size());
        assertEquals(2 + 3, missao.getInimigos().size());
        assertEquals(4, missao.getNave().getVidas());
        // Sem extras e com 3 vidas, o sorteio é o mesmo do criar() das fases anteriores.
        var antigo = new FabricaMissoes(new java.util.Random(9)).criar(Dificuldade.MEDIO, 5);
        var setor = new FabricaMissoes(new java.util.Random(9)).criarSetor(Dificuldade.MEDIO, 5, 0, 0, 3);
        assertEquals(antigo.getPassageiros().stream().map(p -> p.getX() + "," + p.getY()).toList(),
                setor.getPassageiros().stream().map(p -> p.getX() + "," + p.getY()).toList());
        assertEquals(antigo.getInimigos().stream().map(i -> i.getX() + "," + i.getY()).toList(),
                setor.getInimigos().stream().map(i -> i.getX() + "," + i.getY()).toList());
    }
}
