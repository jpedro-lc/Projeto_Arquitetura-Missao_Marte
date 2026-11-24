package br.unifor.missaomarte.repository;

import java.util.List;
import br.unifor.missaomarte.model.Dificuldade;

public interface RankingRepository {
    void salvar(String nome, int pontuacao);
    void salvar(String nome, int pontuacao, Dificuldade dificuldade, int passageirosColetados, long tempoJogo);
    List<RankingEntry> listar();
    void limpar();
}