package br.unifor.missaomarte.servidor.seguranca;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

/** Regras de acesso, hash de senhas e chaves do JWT. */
@Configuration
@EnableConfigurationProperties(PropriedadesSeguranca.class)
public class SegurancaConfig {
    private static final Logger LOG = LoggerFactory.getLogger(SegurancaConfig.class);
    static final int BYTES_MINIMOS_SEGREDO = 32;

    @Bean
    SecurityFilterChain regras(HttpSecurity http) throws Exception {
        return http
                // API sem estado: sem sessão nem cookie, então não há o que um ataque CSRF reaproveitar.
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(regras -> regras
                        .requestMatchers(HttpMethod.POST, "/api/usuarios", "/api/auth/login").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/saude").permitAll()
                        .requestMatchers(HttpMethod.GET, "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Tudo o mais protegido por padrão: rota nova nasce exigindo token.
                        .anyRequest().authenticated())
                .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()))
                .build();
    }

    @Bean
    PasswordEncoder codificadorDeSenhas() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    SecretKey chaveDoToken(PropriedadesSeguranca propriedades) {
        byte[] bytes;
        if (propriedades.segredo() == null || propriedades.segredo().isBlank()) {
            bytes = new byte[BYTES_MINIMOS_SEGREDO];
            new SecureRandom().nextBytes(bytes);
            LOG.warn("MISSAOMARTE_JWT_SEGREDO não definido: usando segredo aleatório; os tokens deixam de valer ao reiniciar.");
        } else {
            bytes = propriedades.segredo().getBytes(StandardCharsets.UTF_8);
            if (bytes.length < BYTES_MINIMOS_SEGREDO) {
                throw new IllegalStateException("O segredo do token precisa de pelo menos 32 bytes.");
            }
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    JwtEncoder codificadorDeToken(SecretKey chave) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(chave));
    }

    @Bean
    JwtDecoder decodificadorDeToken(SecretKey chave) {
        // Por padrão o resource server espera RS256; aqui a chave é simétrica (HS256).
        return NimbusJwtDecoder.withSecretKey(chave).macAlgorithm(MacAlgorithm.HS256).build();
    }
}
