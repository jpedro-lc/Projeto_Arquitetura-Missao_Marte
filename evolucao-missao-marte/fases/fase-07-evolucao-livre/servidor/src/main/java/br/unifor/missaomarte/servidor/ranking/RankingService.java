package br.unifor.missaomarte.servidor.ranking;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.unifor.missaomarte.servidor.partida.Dificuldade;
import br.unifor.missaomarte.servidor.partida.ModoJogo;
import br.unifor.missaomarte.servidor.partida.Partida;
import br.unifor.missaomarte.servidor.partida.PartidaRepository;

@Service
public class RankingService {
    static final int TAMANHO = 10;
    private final PartidaRepository partidas;

    public RankingService(PartidaRepository partidas) { this.partidas = partidas; }

    @Transactional(readOnly = true)
    public List<RankingResposta> top10() {
        return linhas(partidas.melhoresVitorias(PageRequest.of(0, TAMANHO)));
    }

    /** Uma das 9 tabelas da Fase 7; no Infinito contam todas as corridas (nele não há vitória). */
    @Transactional(readOnly = true)
    public List<RankingResposta> top10(ModoJogo modo, Dificuldade dificuldade) {
        return linhas(partidas.melhores(modo, dificuldade, modo == ModoJogo.INFINITO, PageRequest.of(0, TAMANHO)));
    }

    private static List<RankingResposta> linhas(List<Partida> melhores) {
        var linhas = new ArrayList<RankingResposta>(melhores.size());
        for (int i = 0; i < melhores.size(); i++) {
            var p = melhores.get(i);
            linhas.add(new RankingResposta(i + 1, p.getUsuario().getNome(), p.getPontuacao(), p.getDificuldade(),
                    p.getDuracaoSegundos(), p.getRegistradaEm(), p.getModo(), p.getSetor()));
        }
        return linhas;
    }
}
