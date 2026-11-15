package br.unifor.missaomarte.presentation;

import java.util.List;
import java.util.Objects;
import br.unifor.missaomarte.model.Acao;
import br.unifor.missaomarte.model.Dificuldade;
import br.unifor.missaomarte.model.EstadoPartida;
import br.unifor.missaomarte.model.EstadoPartida.Situacao;
import br.unifor.missaomarte.model.ResultadoAcao.Evento;
import br.unifor.missaomarte.presentation.input.EntradaJogo;
import br.unifor.missaomarte.service.JogoService;

/** Coordena a entrada da tela com o serviço; não desenha nem redefine regras. */
public final class ControladorPartida {
    private final JogoService jogo;
    private final EntradaJogo entrada;
    private EstadoPartida estado;
    private List<Evento> eventos = List.of();
    private boolean pausado;

    public ControladorPartida(JogoService jogo, EntradaJogo entrada) {
        this.jogo = Objects.requireNonNull(jogo);
        this.entrada = Objects.requireNonNull(entrada);
        estado = jogo.iniciar(null, Dificuldade.MEDIO, 5);
    }

    /** Retorna true quando a aplicação deve fechar; nenhum frame vira turno sozinho. */
    public boolean atualizar() {
        if (pausado) return false;
        var comando = entrada.ler();
        if (comando.sair()) return true;
        if (comando.reiniciar() && estado.situacao() != Situacao.EM_ANDAMENTO) {
            estado = jogo.iniciar(estado.piloto(), estado.dificuldade(), estado.limite());
            eventos = List.of();
        } else if (comando.acao() != Acao.VAZIA && estado.situacao() == Situacao.EM_ANDAMENTO) {
            var resultado = jogo.executar(comando.acao());
            estado = resultado.estado();
            eventos = resultado.eventos();
        } else {
            // Apenas o relógio pode avançar nesta consulta; não há execução de ação.
            estado = jogo.consultarEstado();
        }
        return false;
    }

    public EstadoPartida estado() { return estado; }
    public List<Evento> eventos() { return eventos; }
    public void pausar() { pausado = true; }
    public void retomar() { pausado = false; }
}
