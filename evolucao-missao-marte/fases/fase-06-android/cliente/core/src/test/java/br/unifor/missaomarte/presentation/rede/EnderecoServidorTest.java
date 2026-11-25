package br.unifor.missaomarte.presentation.rede;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class EnderecoServidorTest {
    @Test void semEnderecoNoBuildEscolhePeloTipoDeAparelho() {
        assertEquals("http://10.0.2.2:8080", EnderecoServidor.android("", true));
        assertEquals("http://127.0.0.1:8080", EnderecoServidor.android("", false));
        assertEquals("http://10.0.2.2:8080", EnderecoServidor.android(null, true));
        assertEquals("http://127.0.0.1:8080", EnderecoServidor.android("   ", false));
    }

    @Test void enderecoInformadoNoBuildSempreVence() {
        assertEquals("http://192.168.1.15:8080", EnderecoServidor.android(" http://192.168.1.15:8080 ", true));
        assertEquals("http://192.168.1.15:8080", EnderecoServidor.android("http://192.168.1.15:8080", false));
    }
}
