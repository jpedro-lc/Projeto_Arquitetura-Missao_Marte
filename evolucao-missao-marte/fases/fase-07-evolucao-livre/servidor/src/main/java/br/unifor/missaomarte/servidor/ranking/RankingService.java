package br.unifor.missaomarte.servidor.ranking;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.unifor.missaomarte.servidor.partida.PartidaRepository;

@Service
public class RankingService {
    static final int TAMANHO = 10;
    private final PartidaRepository partidas;

    public RankingService(PartidaRepository partidas) { this.partidas = partidas; }

    @Transactional(readOnly = true)
    public List<RankingResposta> top10() {
        var melhores = partidas.melhoresVitorias(PageRequest.of(0, TAMANHO));
        var linhas = new ArrayList<RankingResposta>(melhores.size());
        for (int i = 0; i < melhores.size(); i++) {
            var p = melhores.get(i);
            linhas.add(new RankingResposta(i + 1, p.getUsuario().getNome(), p.getPontuacao(), p.getDificuldade(),
                    p.getDuracaoSegundos(), p.getRegistradaEm()));
        }
        return linhas;
    }
}
