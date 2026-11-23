package br.unifor.missaomarte.servidor.ranking;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/ranking")
@Tag(name = "Ranking")
public class RankingController {
    private final RankingService servico;

    public RankingController(RankingService servico) { this.servico = servico; }

    @GetMapping
    public List<RankingResposta> top10() { return servico.top10(); }
}
