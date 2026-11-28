package br.unifor.missaomarte.repository;

import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import br.unifor.missaomarte.model.Dificuldade;
import br.unifor.missaomarte.model.ModoJogo;

/**
 * Decorator: o JogoService continua gravando a vitória, mas só o convidado usa o ranking local;
 * logado, o resultado vai para o servidor pela apresentação.
 */
public final class RankingSomenteConvidado implements RankingRepository {
    private final RankingRepository local;
    private final BooleanSupplier convidado;

    public RankingSomenteConvidado(RankingRepository local, BooleanSupplier convidado) {
        this.local = Objects.requireNonNull(local);
        this.convidado = Objects.requireNonNull(convidado);
    }

    @Override public void salvar(String nome, int pontuacao) {
        if (convidado.getAsBoolean()) local.salvar(nome, pontuacao);
    }

    @Override public void salvar(String nome, int pontuacao, Dificuldade dificuldade, int passageiros, long tempo) {
        if (convidado.getAsBoolean()) local.salvar(nome, pontuacao, dificuldade, passageiros, tempo);
    }

    @Override public void salvar(String nome, int pontuacao, Dificuldade dificuldade, int passageiros, long tempo,
                                 ModoJogo modo, int setor) {
        if (convidado.getAsBoolean()) local.salvar(nome, pontuacao, dificuldade, passageiros, tempo, modo, setor);
    }
    @Override public List<RankingEntry> listar() { return local.listar(); }
    @Override public void limpar() { local.limpar(); }
}
