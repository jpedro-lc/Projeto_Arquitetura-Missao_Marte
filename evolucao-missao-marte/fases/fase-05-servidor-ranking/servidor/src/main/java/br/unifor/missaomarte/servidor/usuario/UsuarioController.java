package br.unifor.missaomarte.servidor.usuario;

import java.net.URI;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuários")
public class UsuarioController {
    private final UsuarioService servico;

    public UsuarioController(UsuarioService servico) { this.servico = servico; }

    @PostMapping
    @SecurityRequirements
    public ResponseEntity<UsuarioResposta> cadastrar(@Valid @RequestBody CadastroRequisicao requisicao) {
        var usuario = servico.cadastrar(requisicao);
        return ResponseEntity.created(URI.create("/api/usuarios/" + usuario.getId())).body(UsuarioResposta.de(usuario));
    }
}
