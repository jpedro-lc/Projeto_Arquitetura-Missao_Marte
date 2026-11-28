package br.unifor.missaomarte.presentation;

import java.util.Locale;
import br.unifor.missaomarte.model.EstadoPartida;

/** Formatação da apresentação; todos os valores vêm do estado da partida. */
public record HudDados(String pontos, String dificuldade, String movimentos, String tempo, int vidas,
                       int resgatados, int total) {
    public static HudDados de(EstadoPartida estado) {
        long segundos = Math.max(0, estado.segundos());
        String dificuldade = nomeDificuldade(estado.dificuldade());
        int embarcados = estado.nave().passageiros().size();
        return new HudDados(String.format(Locale.ROOT, "%05d", estado.pontos()), dificuldade,
                Integer.toString(estado.movimentos()), String.format(Locale.ROOT, "%02d:%02d", segundos / 60, segundos % 60),
                estado.nave().vidas(), embarcados, embarcados + estado.passageiros().size());
    }

    public static String nomeDificuldade(br.unifor.missaomarte.model.Dificuldade dificuldade) {
        return switch (dificuldade) {
            case FACIL -> "FÁCIL";
            case MEDIO -> "MÉDIO";
            case DIFICIL -> "DIFÍCIL";
        };
    }

    /** Corações no HUD: o máximo de vidas (modo Infinito). */
    public static final int CORACOES_MAXIMOS = br.unifor.missaomarte.service.ProgressaoInfinita.VIDAS_MAXIMAS;

    /**
     * Quantos corações aparecem: ao menos 3 (as vidas iniciais) ou o maior número de vidas já tido na partida,
     * para as perdidas aparecerem vazias.
     */
    public static int espacosDeVida(int vidas, int pico) {
        return Math.min(CORACOES_MAXIMOS, Math.max(3, Math.max(vidas, pico)));
    }

    /** 75 -> "01:15". */
    public static String tempo(long segundos) {
        long s = Math.max(0, segundos);
        return String.format(Locale.ROOT, "%02d:%02d", s / 60, s % 60);
    }
}
