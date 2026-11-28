package br.unifor.missaomarte.model;

import java.util.ArrayList;
import java.util.List;

public class Nave extends EntidadeMapa implements Movel {
    private final String nome;
    private final List<Passageiro> passageiros;
    private final int capacidade;
    private int vidas;

    public Nave(String nome, int x, int y) {
        this(nome, x, y, 3);
    }

    public Nave(String nome, int x, int y, int capacidade) {
        this(nome, x, y, capacidade, 3);
    }

    /** Modo Infinito: as vidas passam de um setor para o outro. */
    public Nave(String nome, int x, int y, int capacidade, int vidas) {
        super(x, y);
        if (vidas < 1) throw new IllegalArgumentException("A nave precisa de ao menos uma vida");
        this.nome = nome;
        this.passageiros = new ArrayList<>();
        this.capacidade = capacidade;
        this.vidas = vidas;
    }

    public String getNome() {
        return nome;
    }

    public void embarcar(Passageiro passageiro) {
        if (passageiros.size() < capacidade) {
            passageiros.add(passageiro);
        }
    }

    public void moverComLimites(int dx, int dy, int minX, int maxX, int minY, int maxY) {
        int novoX = this.x + dx;
        int novoY = this.y + dy;
        if (novoX >= minX && novoX <= maxX && novoY >= minY && novoY <= maxY) {
            this.x = novoX;
            this.y = novoY;
        }
    }

    public void perderVida() {
        this.vidas = Math.max(0, this.vidas - 1);
    }

    public int getVidas() {
        return vidas;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public List<Passageiro> getPassageiros() {
        return List.copyOf(passageiros);
    }

    @Override
    public void mover(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    @Override
    public String getSimbolo() {
        return "N";
    }
}
