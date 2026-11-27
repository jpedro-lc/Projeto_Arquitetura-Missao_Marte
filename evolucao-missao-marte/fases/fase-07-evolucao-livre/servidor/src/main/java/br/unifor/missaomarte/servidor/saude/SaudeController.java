package br.unifor.missaomarte.servidor.saude;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;

/** Público: verificação de saúde do Render e "acordar" o servidor gratuito antes do login. */
@RestController
@Tag(name = "Saúde")
public class SaudeController {
    @GetMapping("/api/saude")
    public Map<String, String> saude() { return Map.of("status", "ok"); }
}
