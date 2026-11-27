package br.unifor.missaomarte.presentation;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import br.unifor.missaomarte.presentation.FormularioLogin.Campo;
import br.unifor.missaomarte.presentation.FormularioLogin.Modo;

class FormularioLoginTest {
    private static void digitar(FormularioLogin f, String texto) { for (char c : texto.toCharArray()) f.digitar(c); }

    @Test void nomeAceitaSoLetrasNumerosSublinhadoEHifenAte20() {
        var f = new FormularioLogin();
        assertTrue(f.digitar('a'));
        assertTrue(f.digitar('Z'));
        assertTrue(f.digitar('7'));
        assertTrue(f.digitar('_'));
        assertTrue(f.digitar('-'));
        for (char c : " !@\"çã".toCharArray()) assertFalse(f.digitar(c), "rejeita '" + c + "'");
        digitar(f, "x".repeat(30));
        assertEquals(20, f.nome().length());
    }

    @Test void senhaEMascaradaAceitaSimbolosELimitaA72Bytes() {
        var f = new FormularioLogin();
        f.trocarCampo();
        assertEquals(Campo.SENHA, f.campo());
        digitar(f, "a\"b\\c d!");
        assertEquals("a\"b\\c d!", f.senha());
        assertEquals("********", f.senhaMascarada());
        assertFalse(f.digitar('\n'));
        var g = new FormularioLogin();
        g.trocarCampo();
        digitar(g, "ç".repeat(40));
        assertEquals(36, g.senha().length(), "36 × 2 bytes = 72");
        assertFalse(g.toString().contains("ç"), "toString não mostra a senha");
    }

    @Test void apagarAgeNoCampoAtivo() {
        var f = new FormularioLogin();
        digitar(f, "ana");
        f.apagar();
        assertEquals("an", f.nome());
        f.trocarCampo();
        f.apagar();
        digitar(f, "12");
        f.apagar();
        assertEquals("1", f.senha());
        assertEquals("an", f.nome());
        f.trocarCampo();
        assertEquals(Campo.NOME, f.campo());
    }

    @Test void validacaoDeEntrarEDeCriarConta() {
        var f = new FormularioLogin();
        assertTrue(f.validar().isPresent(), "nome vazio");
        digitar(f, "ana");
        assertEquals("Informe a senha.", f.validar().orElseThrow());
        f.trocarCampo();
        digitar(f, "1234");
        assertTrue(f.validar().isEmpty(), "entrar não repete a regra de tamanho de senha");
        f.alternarModo();
        assertEquals(Modo.CRIAR_CONTA, f.modo());
        assertEquals("A senha deve ter pelo menos 8 caracteres.", f.validar().orElseThrow());
        digitar(f, "5678");
        assertTrue(f.validar().isEmpty());
    }
}
