package br.unifor.missaomarte.servidor.auth;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação")
public class AuthController {
    private final AutenticacaoService servico;

    public AuthController(AutenticacaoService servico) { this.servico = servico; }

    @PostMapping("/login")
    @SecurityRequirements
    public TokenResposta entrar(@Valid @RequestBody LoginRequisicao requisicao) {
        return servico.entrar(requisicao);
    }
}
