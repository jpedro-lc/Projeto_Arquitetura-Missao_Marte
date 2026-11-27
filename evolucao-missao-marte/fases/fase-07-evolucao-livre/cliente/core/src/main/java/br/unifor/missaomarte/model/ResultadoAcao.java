package br.unifor.missaomarte.model;

import java.util.List;

/** Eventos são fatos da partida; frases e efeitos visuais pertencem à apresentação. */
public record ResultadoAcao(EstadoPartida estado, List<Evento> eventos) {
    public enum TipoEvento {
        SEM_PASSAGEIRO, EMBARCOU, NAVE_CHEIA, COMANDO_INVALIDO, ABORTOU,
        COLISAO, NAVE_DESTRUIDA, SEM_PONTOS, RETORNAR_BASE, VITORIA
    }
    public record Evento(TipoEvento tipo, String nome, int valor) { }
    public ResultadoAcao { eventos = List.copyOf(eventos); }
}
