package br.unifor.missaomarte.servidor.nuvem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

class UrlDoBancoTest {
    @Test void urlDoRenderViraJdbcComUsuarioESenha() {
        var p = UrlDoBanco.paraSpring("postgresql://missaomarte:s3nh%40forte@dpg-abc-a:5432/missaomarte");
        assertThat(p).containsEntry("spring.datasource.url", "jdbc:postgresql://dpg-abc-a:5432/missaomarte")
                .containsEntry("spring.datasource.username", "missaomarte")
                .containsEntry("spring.datasource.password", "s3nh@forte");
    }

    @Test void semPortaUsa5432EMantemParametros() {
        var p = UrlDoBanco.paraSpring("postgres://u:p@host.render.com/banco?sslmode=require");
        assertThat(p).containsEntry("spring.datasource.url", "jdbc:postgresql://host.render.com:5432/banco?sslmode=require");
    }

    @Test void vazioNaoMudaNadaEJdbcPassaDireto() {
        assertThat(UrlDoBanco.paraSpring(null)).isEmpty();
        assertThat(UrlDoBanco.paraSpring("  ")).isEmpty();
        assertThat(UrlDoBanco.paraSpring("jdbc:postgresql://h:1/b")).containsOnlyKeys("spring.datasource.url");
    }

    @Test void urlInvalidaFalhaComMensagemClara() {
        assertThatThrownBy(() -> UrlDoBanco.paraSpring("mysql://u:p@h/b")).hasMessageContaining("postgres");
        assertThatThrownBy(() -> UrlDoBanco.paraSpring("postgres://u:p@h")).hasMessageContaining("banco");
    }
}
