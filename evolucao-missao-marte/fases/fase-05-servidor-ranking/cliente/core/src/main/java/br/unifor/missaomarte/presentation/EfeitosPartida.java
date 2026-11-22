package br.unifor.missaomarte.presentation;

import java.util.ArrayList;
import java.util.List;
import br.unifor.missaomarte.model.ResultadoAcao;

/** Efeitos temporários independentes do turno; recebem apenas resultados novos. */
public final class EfeitosPartida {
    public enum Tipo { EXPLOSAO, RESGATE }
    public record Efeito(Tipo tipo, int x, int y, float idade) {
        public float duracao() { return tipo == Tipo.EXPLOSAO ? 0.88f : 0.7f; }
    }
    private final List<Efeito> efeitos = new ArrayList<>();

    public void receber(ResultadoAcao resultado) {
        for (var evento : resultado.eventos()) {
            switch (evento.tipo()) {
                case COLISAO, NAVE_DESTRUIDA -> efeitos.add(new Efeito(Tipo.EXPLOSAO,
                        resultado.estado().nave().x(), resultado.estado().nave().y(), 0));
                case EMBARCOU -> efeitos.add(new Efeito(Tipo.RESGATE,
                        resultado.estado().nave().x(), resultado.estado().nave().y(), 0));
                default -> { }
            }
        }
    }

    public void atualizar(float delta) {
        var it = efeitos.listIterator();
        while (it.hasNext()) {
            var efeito = it.next();
            float idade = efeito.idade() + Math.max(0, delta);
            if (idade >= efeito.duracao()) it.remove();
            else it.set(new Efeito(efeito.tipo(), efeito.x(), efeito.y(), idade));
        }
    }

    public List<Efeito> ativos() { return List.copyOf(efeitos); }
    public void limpar() { efeitos.clear(); }
}
