package br.unifor.missaomarte.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Integra a suíte existente ao Gradle sem reescrever suas verificações. */
class RegrasJogoTest {
    @Test
    @DisplayName("Executa as 162 verificações herdadas da Fase 1")
    void executarVerificacoesExistentes() throws Exception {
        TestesFase01.main(new String[0]);
    }
}
