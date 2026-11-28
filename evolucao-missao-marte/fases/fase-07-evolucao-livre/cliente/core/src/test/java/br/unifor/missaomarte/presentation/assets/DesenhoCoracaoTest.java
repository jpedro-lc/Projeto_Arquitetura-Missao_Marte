package br.unifor.missaomarte.presentation.assets;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import br.unifor.missaomarte.presentation.HudDados;

/** O coração é desenhado por código: o desenho e a regra de quantos aparecem são testados sem janela. */
class DesenhoCoracaoTest {
    @Test void padraoRetangularESimetricoForaDoBrilho() {
        assertEquals(DesenhoCoracao.ALTURA, DesenhoCoracao.PADRAO.length);
        for (String linha : DesenhoCoracao.PADRAO) {
            assertEquals(DesenhoCoracao.LARGURA, linha.length());
            String semBrilho = linha.replace('H', 'X');
            assertEquals(new StringBuilder(semBrilho).reverse().toString(), semBrilho, "coração torto: " + linha);
        }
    }

    @Test void cheioTemCorEVazioMostraSoOContornoForte() {
        int recheioCheio = DesenhoCoracao.cor(6, 5, true);
        int recheioVazio = DesenhoCoracao.cor(6, 5, false);
        assertNotEquals(recheioCheio, recheioVazio);
        assertEquals(0xFF, recheioCheio & 0xFF, "recheio cheio é opaco");
        assertTrue((recheioVazio & 0xFF) < 0xFF, "recheio vazio é translúcido");
        assertEquals(0, DesenhoCoracao.cor(0, 0, true), "canto transparente");
        assertEquals(0, DesenhoCoracao.cor(0, 0, false));
        assertEquals(0xFF, DesenhoCoracao.cor(0, 2, false) & 0xFF, "contorno do vazio é opaco");
    }

    @Test void espacosMostramAsVidasPerdidasAteOMaximo() {
        assertEquals(3, HudDados.espacosDeVida(3, 3));
        assertEquals(3, HudDados.espacosDeVida(1, 3), "2 corações vazios ao perder 2 vidas");
        assertEquals(5, HudDados.espacosDeVida(2, 5), "no Infinito, o pico de 5 continua visível");
        assertEquals(4, HudDados.espacosDeVida(4, 0));
        assertEquals(5, HudDados.espacosDeVida(9, 9));
        assertEquals(3, HudDados.espacosDeVida(0, 0));
    }
}
