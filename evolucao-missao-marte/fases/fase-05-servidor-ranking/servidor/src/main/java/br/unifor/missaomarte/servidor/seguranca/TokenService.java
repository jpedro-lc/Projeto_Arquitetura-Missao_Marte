package br.unifor.missaomarte.servidor.seguranca;

import java.time.Instant;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import br.unifor.missaomarte.servidor.auth.TokenResposta;
import br.unifor.missaomarte.servidor.usuario.Usuario;

/** Emite o JWT: sub = id do usuário. A senha nunca entra no token. */
@Service
public class TokenService {
    private final JwtEncoder codificador;
    private final PropriedadesSeguranca propriedades;

    public TokenService(JwtEncoder codificador, PropriedadesSeguranca propriedades) {
        this.codificador = codificador;
        this.propriedades = propriedades;
    }

    public TokenResposta emitir(Usuario usuario) {
        Instant agora = Instant.now();
        Instant expira = agora.plus(propriedades.validade());
        var claims = JwtClaimsSet.builder()
                .issuer("missao-marte")
                .subject(usuario.getId().toString())
                .claim("nome", usuario.getNome())
                .issuedAt(agora)
                .expiresAt(expira)
                .build();
        String token = codificador.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
        return new TokenResposta(token, "Bearer", expira);
    }
}
