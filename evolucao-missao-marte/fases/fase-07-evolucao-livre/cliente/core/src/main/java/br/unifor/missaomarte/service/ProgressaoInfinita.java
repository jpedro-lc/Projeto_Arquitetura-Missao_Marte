package br.unifor.missaomarte.service;

import br.unifor.missaomarte.model.Dificuldade;

/**
 * Quanto o modo Infinito aperta a cada setor (1, 2, 3...). Classe pura: o balanceamento muda aqui
 * e é conferido por testes, sem janela.
 */
public final class ProgressaoInfinita {
    public static final float PASSO_MINIMO_INIMIGOS = 0.25f;
    public static final float ACELERACAO_POR_SETOR = 0.92f;
    public static final int LIMITE_MAXIMO = 7;
    public static final int VIDAS_MAXIMAS = 5;

    private ProgressaoInfinita() { }

    private static void exigirSetor(int setor) {
        if (setor < 1) throw new IllegalArgumentException("Setores começam em 1");
    }

    /** +1 inimigo a cada 2 setores. */
    public static int inimigosExtras(int setor) { exigirSetor(setor); return (setor - 1) / 2; }

    /** +1 asteroide a cada 3 setores. */
    public static int asteroidesExtras(int setor) { exigirSetor(setor); return (setor - 1) / 3; }

    /** O mapa ganha 1 casa de raio a cada 4 setores, até LIMITE_MAXIMO (ou o limite inicial, se maior). */
    public static int limite(int limiteInicial, int setor) {
        exigirSetor(setor);
        return Math.min(Math.max(LIMITE_MAXIMO, limiteInicial), limiteInicial + (setor - 1) / 4);
    }

    /** Inimigos 8% mais rápidos a cada setor, sem passar de PASSO_MINIMO_INIMIGOS. */
    public static RitmoTempoReal ritmo(Dificuldade dificuldade, int setor) {
        exigirSetor(setor);
        var base = RitmoTempoReal.de(dificuldade);
        float passo = (float) Math.max(PASSO_MINIMO_INIMIGOS,
                base.passoInimigos() * Math.pow(ACELERACAO_POR_SETOR, setor - 1));
        return new RitmoTempoReal(base.passoNave(), passo, base.contagem(), base.invulneravelAposColisao(),
                base.segundosPorPonto());
    }

    /** Vidas ao entrar no setor: as que sobraram, +1 nos setores 6, 11, 16... (no máximo VIDAS_MAXIMAS). */
    public static int vidasAoEntrar(int vidasRestantes, int setor) {
        exigirSetor(setor);
        boolean bonus = setor > 1 && (setor - 1) % 5 == 0;
        return Math.min(VIDAS_MAXIMAS, vidasRestantes + (bonus ? 1 : 0));
    }
}
