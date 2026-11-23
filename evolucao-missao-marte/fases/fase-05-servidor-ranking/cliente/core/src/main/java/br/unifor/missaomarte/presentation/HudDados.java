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

    /** 75 -> "01:15". */
    public static String tempo(long segundos) {
        long s = Math.max(0, segundos);
        return String.format(Locale.ROOT, "%02d:%02d", s / 60, s % 60);
    }
}
