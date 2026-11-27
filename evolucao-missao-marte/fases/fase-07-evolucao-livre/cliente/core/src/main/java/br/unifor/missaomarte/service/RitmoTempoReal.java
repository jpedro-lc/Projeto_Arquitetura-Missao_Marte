package br.unifor.missaomarte.service;

import br.unifor.missaomarte.model.Dificuldade;

/**
 * Valores do modo tempo real, em segundos. Ficam juntos para o balanceamento mexer num lugar só
 * (e para o modo Infinito, na etapa 7.3, derivar ritmos mais rápidos a partir destes).
 */
public record RitmoTempoReal(float passoNave, float passoInimigos, float contagem,
                             float invulneravelAposColisao, float segundosPorPonto) {
    public static final float PASSO_NAVE = 0.15f;
    public static final float CONTAGEM = 3f;
    public static final float INVULNERAVEL = 1.5f;

    public RitmoTempoReal {
        if (passoNave <= 0 || passoInimigos <= 0 || segundosPorPonto <= 0 || contagem < 0 || invulneravelAposColisao < 0) {
            throw new IllegalArgumentException("Ritmo inválido");
        }
    }

    public static RitmoTempoReal de(Dificuldade dificuldade) {
        float inimigos = switch (dificuldade) {
            case FACIL -> 0.9f;
            case MEDIO -> 0.7f;
            case DIFICIL -> 0.5f;
        };
        return new RitmoTempoReal(PASSO_NAVE, inimigos, CONTAGEM, INVULNERAVEL, 1f);
    }
}
