package br.unifor.missaomarte.servidor.ranking;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import br.unifor.missaomarte.servidor.partida.Dificuldade;
import br.unifor.missaomarte.servidor.partida.ModoJogo;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/ranking")
@Tag(name = "Ranking")
public class RankingController {
    private final RankingService servico;

    public RankingController(RankingService servico) { this.servico = servico; }

    /**
     * Sem parâmetros: Top 10 de vitórias de todos os modos (contrato das Fases 5 e 6).
     * Fase 7: ?modo=…&amp;dificuldade=… devolve uma das 9 tabelas (3 modos × 3 dificuldades), por pontos.
     */
    @GetMapping
    public List<RankingResposta> top10(@RequestParam(required = false) ModoJogo modo,
                                       @RequestParam(required = false) Dificuldade dificuldade) {
        if (modo == null && dificuldade == null) return servico.top10();
        if (modo == null || dificuldade == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o modo e a dificuldade juntos.");
        }
        return servico.top10(modo, dificuldade);
    }
}
