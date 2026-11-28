package br.unifor.missaomarte.repository;

import java.util.List;
import br.unifor.missaomarte.model.Dificuldade;
import br.unifor.missaomarte.model.ModoJogo;

public interface RankingRepository {
    void salvar(String nome, int pontuacao);
    void salvar(String nome, int pontuacao, Dificuldade dificuldade, int passageirosColetados, long tempoJogo);
    /** Fase 7: guarda também o modo (e o setor, no Infinito). Quem não sabe o modo grava como antes. */
    default void salvar(String nome, int pontuacao, Dificuldade dificuldade, int passageirosColetados, long tempoJogo,
                        ModoJogo modo, int setor) {
        salvar(nome, pontuacao, dificuldade, passageirosColetados, tempoJogo);
    }
    List<RankingEntry> listar();
    void limpar();
}