package br.unifor.missaomarte.presentation.assets;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import br.unifor.missaomarte.model.Acao;

/** As quatro setas saem do mesmo padrão girado: a ponta tem de ficar no lado certo. */
class DesenhoSetaTest {
    static final int MEIO = DesenhoSeta.LADO / 2;
    static final int FIM = DesenhoSeta.LADO - 1;

    @Test void padraoQuadradoESimetrico() {
        assertEquals(DesenhoSeta.LADO, DesenhoSeta.PADRAO.length);
        for (String linha : DesenhoSeta.PADRAO) {
            assertEquals(DesenhoSeta.LADO, linha.length());
            assertEquals(new StringBuilder(linha).reverse().toString(), linha);
        }
    }

    @Test void pontaDeCadaSetaApontaParaSuaDirecao() {
        assertNotEquals(0, DesenhoSeta.cor(MEIO, 0, Acao.CIMA));
        assertEquals(0, DesenhoSeta.cor(MEIO, FIM - 1, Acao.CIMA) == 0 ? 1 : 0, "haste da seta para cima embaixo");
        assertNotEquals(0, DesenhoSeta.cor(MEIO, FIM, Acao.BAIXO));
        assertNotEquals(0, DesenhoSeta.cor(0, MEIO, Acao.ESQUERDA));
        assertNotEquals(0, DesenhoSeta.cor(FIM, MEIO, Acao.DIREITA));
        // Os cantos do lado da ponta ficam vazios.
        assertEquals(0, DesenhoSeta.cor(0, 0, Acao.CIMA));
        assertEquals(0, DesenhoSeta.cor(FIM, 0, Acao.DIREITA));
        assertEquals(0, DesenhoSeta.cor(0, FIM, Acao.ESQUERDA));
    }

    @Test void soMovimentosTemSeta() {
        assertThrows(IllegalArgumentException.class, () -> DesenhoSeta.cor(0, 0, Acao.EMBARCAR));
    }
}
