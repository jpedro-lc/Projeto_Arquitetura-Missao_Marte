package br.unifor.missaomarte.servidor.partida;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/partidas")
@Tag(name = "Partidas")
public class PartidaController {
    private final PartidaService servico;

    public PartidaController(PartidaService servico) { this.servico = servico; }

    /** O dono da partida é sempre o usuário do token (claim sub), nunca um dado do corpo. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PartidaResposta registrar(@AuthenticationPrincipal Jwt token, @Valid @RequestBody PartidaRequisicao requisicao) {
        return servico.registrar(Long.valueOf(token.getSubject()), requisicao);
    }

    @GetMapping("/minhas")
    public List<PartidaResposta> minhas(@AuthenticationPrincipal Jwt token,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "A página não pode ser negativa.") int pagina,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "O tamanho deve ser de 1 a 50.")
            @Max(value = 50, message = "O tamanho deve ser de 1 a 50.") int tamanho) {
        return servico.historico(Long.valueOf(token.getSubject()), pagina, tamanho);
    }
}
