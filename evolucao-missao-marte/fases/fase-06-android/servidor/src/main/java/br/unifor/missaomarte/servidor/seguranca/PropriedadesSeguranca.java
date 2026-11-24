package br.unifor.missaomarte.servidor.seguranca;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** missaomarte.seguranca.*: segredo do JWT (do ambiente) e validade do token. */
@ConfigurationProperties("missaomarte.seguranca")
public record PropriedadesSeguranca(String segredo, Duration validade) {
    public PropriedadesSeguranca {
        if (validade == null) validade = Duration.ofHours(2);
    }
}
