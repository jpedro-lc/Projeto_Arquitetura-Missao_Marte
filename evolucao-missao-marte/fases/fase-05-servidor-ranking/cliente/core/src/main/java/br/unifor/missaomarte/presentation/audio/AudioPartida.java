package br.unifor.missaomarte.presentation.audio;

import java.util.ArrayList;
import java.util.List;
import br.unifor.missaomarte.model.ResultadoAcao;
import br.unifor.missaomarte.presentation.assets.RecursosJogo;

/** Não é dono dos recursos; o AssetManager do Game faz seu descarte. */
public final class AudioPartida {
    private final RecursosJogo recursos;
    private boolean trilhaAtiva;
    private boolean derrotaPausada;

    public AudioPartida(RecursosJogo recursos) { this.recursos = recursos; }

    public void iniciar() {
        parar();
        var musica = recursos.musica(RecursosJogo.MUSICA);
        musica.setLooping(true);
        musica.setVolume(0.22f);
        musica.play();
        trilhaAtiva = true;
    }

    public void processar(ResultadoAcao resultado) {
        for (String efeito : efeitos(resultado)) recursos.som(efeito).play(0.65f);
        switch (resultado.estado().situacao()) {
            case VITORIA -> parar();
            case DERROTA -> {
                parar();
                var fim = recursos.musica(RecursosJogo.DERROTA);
                fim.setLooping(false);
                fim.setVolume(0.55f);
                fim.play();
            }
            case ABORTADA -> parar();
            default -> { }
        }
    }

    /** A causa da colisão é lida da fotografia; não existe novo cálculo físico. */
    public static List<String> efeitos(ResultadoAcao resultado) {
        List<String> sons = new ArrayList<>();
        for (var evento : resultado.eventos()) {
            switch (evento.tipo()) {
                case COLISAO, NAVE_DESTRUIDA -> {
                    var nave = resultado.estado().nave();
                    boolean asteroide = resultado.estado().asteroides().stream()
                            .anyMatch(a -> a.x() == nave.x() && a.y() == nave.y());
                    sons.add(asteroide ? RecursosJogo.IMPACTO_ASTEROIDE : RecursosJogo.IMPACTO_INIMIGO);
                }
                case EMBARCOU -> sons.add(RecursosJogo.RESGATE);
                case VITORIA -> sons.add(RecursosJogo.VITORIA);
                case SEM_PASSAGEIRO, NAVE_CHEIA, COMANDO_INVALIDO -> sons.add(RecursosJogo.ERRO);
                default -> { }
            }
        }
        return List.copyOf(sons);
    }

    public void selecionar() { recursos.som(RecursosJogo.MENU).play(0.45f); }
    public void pausar() {
        derrotaPausada = recursos.musica(RecursosJogo.DERROTA).isPlaying();
        recursos.musica(RecursosJogo.MUSICA).pause();
        recursos.musica(RecursosJogo.DERROTA).pause();
    }
    public void retomar() {
        if (trilhaAtiva) recursos.musica(RecursosJogo.MUSICA).play();
        if (derrotaPausada) recursos.musica(RecursosJogo.DERROTA).play();
        derrotaPausada = false;
    }
    public void parar() {
        recursos.musica(RecursosJogo.MUSICA).stop();
        recursos.musica(RecursosJogo.DERROTA).stop();
        trilhaAtiva = false;
        derrotaPausada = false;
    }
}
