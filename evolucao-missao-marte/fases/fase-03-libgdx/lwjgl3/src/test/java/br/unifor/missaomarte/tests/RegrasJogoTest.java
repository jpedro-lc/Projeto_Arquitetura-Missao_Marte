package br.unifor.missaomarte.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Integra a suíte herdada, ajustada às correções autorizadas de limites e aborto. */
class RegrasJogoTest {
    @Test
    @DisplayName("Executa 162 verificações herdadas com as correções autorizadas")
    void executarVerificacoesExistentes() throws Exception {
        TestesFase01.main(new String[0]);
    }
}
