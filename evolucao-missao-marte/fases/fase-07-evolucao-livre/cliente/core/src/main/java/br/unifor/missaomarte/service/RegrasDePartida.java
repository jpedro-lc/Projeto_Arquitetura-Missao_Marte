package br.unifor.missaomarte.service;

import br.unifor.missaomarte.model.Acao;
import br.unifor.missaomarte.model.Dificuldade;
import br.unifor.missaomarte.model.EstadoPartida;
import br.unifor.missaomarte.model.ModoJogo;
import br.unifor.missaomarte.model.ResultadoAcao;

/**
 * Strategy: o controlador da tela conversa com qualquer modo de jogo por este contrato.
 * O tempo chega por parâmetro (segundos desde o último quadro); nada aqui lê o relógio do sistema.
 */
public interface RegrasDePartida {
    ModoJogo modo();
    EstadoPartida iniciar(String piloto, Dificuldade dificuldade, int limite);
    /** Ação pontual do jogador (no clássico, também os movimentos). */
    ResultadoAcao executar(Acao acao);
    /** Faz o tempo andar; direcaoMantida é o movimento pressionado agora (ou VAZIA). */
    ResultadoAcao avancar(float segundos, Acao direcaoMantida);
    EstadoPartida consultarEstado();
    /** Segundos da contagem antes da largada; 0 quando a partida já corre. */
    default float contagemRestante() { return 0f; }
    /** Modo Infinito: setor atual (1, 2, ...); 0 nos modos de partida única. */
    default int setor() { return 0; }
}
