package br.unifor.missaomarte.service;

import java.util.Objects;
import java.util.Random;
import br.unifor.missaomarte.model.*;

/** Fábrica simples extraída do serviço, preservando o algoritmo de geração. */
public class FabricaMissoes {
    private final Random random;

    public FabricaMissoes() { this(new Random()); }
    public FabricaMissoes(Random random) { this.random = Objects.requireNonNull(random); }

    public Missao criar(Dificuldade dificuldade, int limite) {
        return criarNovaMissao(dificuldade, -limite, limite, -limite, limite);
    }

    /**
     * Modo Infinito: mesmo sorteio do criar(), com obstáculos extras e as vidas que a nave já tinha.
     * Os extras entram depois dos sorteios originais (só aumentam as quantidades); criar() não muda.
     */
    public Missao criarSetor(Dificuldade dificuldade, int limite, int asteroidesExtras, int inimigosExtras, int vidas) {
        return criarNovaMissao(dificuldade, -limite, limite, -limite, limite, asteroidesExtras, inimigosExtras, vidas);
    }

    private Missao criarNovaMissao(Dificuldade dificuldade, int minX, int maxX, int minY, int maxY) {
        return criarNovaMissao(dificuldade, minX, maxX, minY, maxY, 0, 0, 3);
    }

    private Missao criarNovaMissao(Dificuldade dificuldade, int minX, int maxX, int minY, int maxY,
                                   int asteroidesExtras, int inimigosExtras, int vidas) {
        int qtdPassageiros = 4;
        int qtdAsteroides = 2;
        int qtdInimigos = 2;

        if (dificuldade == Dificuldade.MEDIO) {
            qtdPassageiros = 5;
        } else if (dificuldade == Dificuldade.DIFICIL) {
            qtdPassageiros = 6;
            qtdAsteroides = 3;
            qtdInimigos = 3;
        }

        qtdAsteroides += asteroidesExtras;
        qtdInimigos += inimigosExtras;
        Nave nave = new Nave("A-1", 0, 0, qtdPassageiros, vidas);
        Missao missao = new Missao(nave);

        posicionarPassageiros(missao, qtdPassageiros, minX, maxX, minY, maxY, nave);

        posicionarEntidades(missao, qtdAsteroides, minX, maxX, minY, maxY, nave, true);
        posicionarEntidades(missao, qtdInimigos, minX, maxX, minY, maxY, nave, false);

        return missao;
    }

    private void posicionarPassageiros(Missao missao, int qtdPassageiros, int minX, int maxX, int minY, int maxY, Nave nave) {
        int indice = 0;
        while (missao.getPassageiros().size() < qtdPassageiros) {
            int[] posicao = sortearPosicaoLivre(missao, minX, maxX, minY, maxY, nave);
            int x = posicao[0];
            int y = posicao[1];
            if (indice % 3 == 0) {
                missao.adicionarPassageiro(new Professor("Dr. Silva", x, y));
            } else if (indice % 3 == 1) {
                missao.adicionarPassageiro(new Engenheiro("Eng. Rosa", x, y));
            } else {
                missao.adicionarPassageiro(new Professor("Dr. Lima", x, y));
            }
            indice++;
        }
    }

    private void posicionarEntidades(Missao missao, int qtd, int minX, int maxX, int minY, int maxY, Nave nave, boolean asteroide) {
        int total = asteroide ? missao.getAsteroides().size() : missao.getInimigos().size();
        while (total < qtd) {
            int[] posicao = sortearPosicaoLivre(missao, minX, maxX, minY, maxY, nave);
            if (asteroide) {
                missao.adicionarAsteroide(new Asteroide(posicao[0], posicao[1]));
            } else {
                missao.adicionarInimigo(new Inimigo(posicao[0], posicao[1]));
            }
            total++;
        }
    }

    private int[] sortearPosicaoLivre(Missao missao, int minX, int maxX, int minY, int maxY, Nave nave) {
        int tentativasMaximas = Math.max(20, (maxX - minX + 1) * (maxY - minY + 1) * 2);
        for (int tentativa = 0; tentativa < tentativasMaximas; tentativa++) {
            int x = random.nextInt(maxX - minX + 1) + minX;
            int y = random.nextInt(maxY - minY + 1) + minY;
            if (!posicaoOcupada(missao, x, y) && !(x == nave.getX() && y == nave.getY())) {
                return new int[] { x, y };
            }
        }
        throw new IllegalStateException("O mapa nao possui posicoes livres suficientes");
    }

    private boolean posicaoOcupada(Missao missao, int x, int y) {
        for (Passageiro passageiro : missao.getPassageiros()) {
            if (passageiro.getX() == x && passageiro.getY() == y) {
                return true;
            }
        }
        for (Asteroide asteroide : missao.getAsteroides()) {
            if (asteroide.getX() == x && asteroide.getY() == y) {
                return true;
            }
        }
        for (Inimigo inimigo : missao.getInimigos()) {
            if (inimigo.getX() == x && inimigo.getY() == y) {
                return true;
            }
        }
        return false;
    }
}
