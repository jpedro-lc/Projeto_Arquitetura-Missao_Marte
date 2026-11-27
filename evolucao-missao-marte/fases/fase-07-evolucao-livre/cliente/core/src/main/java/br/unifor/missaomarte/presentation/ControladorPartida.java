package br.unifor.missaomarte.presentation;

import java.util.List;
import java.util.ArrayDeque;
import java.util.Objects;
import br.unifor.missaomarte.model.Acao;
import br.unifor.missaomarte.model.Dificuldade;
import br.unifor.missaomarte.model.EstadoPartida;
import br.unifor.missaomarte.model.EstadoPartida.Situacao;
import br.unifor.missaomarte.model.ModoJogo;
import br.unifor.missaomarte.model.ResultadoAcao.Evento;
import br.unifor.missaomarte.model.ResultadoAcao;
import br.unifor.missaomarte.presentation.input.EntradaJogo;
import br.unifor.missaomarte.service.JogoService;
import br.unifor.missaomarte.service.RegrasClassicas;
import br.unifor.missaomarte.service.RegrasDePartida;

/** Coordena a entrada da tela com as regras do modo escolhido; não desenha nem redefine regras. */
public final class ControladorPartida {
    private final RegrasDePartida regras;
    private final EntradaJogo entrada;
    private EstadoPartida estado;
    private List<Evento> eventos = List.of();
    private boolean pausado;
    private final ArrayDeque<ResultadoAcao> novosResultados = new ArrayDeque<>();
    private int geracao = 1;

    public ControladorPartida(JogoService jogo, EntradaJogo entrada) {
        this(jogo, entrada, null, Dificuldade.MEDIO, 5);
    }

    /** Modo clássico, como nas Fases 3 a 6. */
    public ControladorPartida(JogoService jogo, EntradaJogo entrada, String piloto, Dificuldade dificuldade, int limite) {
        this(new RegrasClassicas(Objects.requireNonNull(jogo)), entrada, piloto, dificuldade, limite);
    }

    public ControladorPartida(RegrasDePartida regras, EntradaJogo entrada, String piloto, Dificuldade dificuldade, int limite) {
        this.regras = Objects.requireNonNull(regras);
        this.entrada = Objects.requireNonNull(entrada);
        estado = regras.iniciar(piloto, dificuldade, limite);
    }

    /** Clássico: nenhum quadro vira turno sozinho. */
    public boolean atualizar() { return atualizar(0f); }

    /** Retorna true quando a aplicação deve fechar; no tempo real, os segundos do quadro fazem a partida andar. */
    public boolean atualizar(float segundos) {
        if (pausado) return false;
        var comando = entrada.ler();
        if (comando.sair()) return true;
        boolean emAndamento = estado.situacao() == Situacao.EM_ANDAMENTO;
        if (comando.reiniciar() && !emAndamento) {
            reiniciar();
        } else if (regras.modo() == ModoJogo.CLASSICO) {
            if (comando.acao() != Acao.VAZIA && emAndamento) registrar(regras.executar(comando.acao()));
            else estado = regras.consultarEstado(); // Só o relógio pode avançar nesta consulta.
        } else if (emAndamento) {
            if (comando.acao() != Acao.VAZIA && !comando.acao().movimento()) registrar(regras.executar(comando.acao()));
            // Toque rápido (começa e termina no mesmo quadro) ainda vale um passo.
            Acao direcao = entrada.direcaoMantida();
            if (!direcao.movimento() && comando.acao().movimento()) direcao = comando.acao();
            int movimentosAntes = estado.movimentos();
            var resultado = regras.avancar(segundos, direcao);
            if (!resultado.eventos().isEmpty() || resultado.estado().movimentos() != movimentosAntes) registrar(resultado);
            else estado = resultado.estado();
        }
        return false;
    }

    private void registrar(ResultadoAcao resultado) {
        estado = resultado.estado();
        eventos = resultado.eventos();
        novosResultados.addLast(resultado);
    }

    public EstadoPartida estado() { return estado; }
    public List<Evento> eventos() { return eventos; }
    public ModoJogo modo() { return regras.modo(); }
    public float contagemRestante() { return regras.contagemRestante(); }
    public void pausar() { pausado = true; }
    public void retomar() { pausado = false; }

    public void reiniciar() {
        estado = regras.iniciar(estado.piloto(), estado.dificuldade(), estado.limite());
        eventos = List.of();
        novosResultados.clear();
        geracao++;
    }

    public void abortar() {
        if (estado.situacao() == Situacao.EM_ANDAMENTO) estado = regras.executar(Acao.ABORTAR).estado();
        novosResultados.clear();
    }

    /** Consequências sonoras/visuais consomem cada resultado uma vez. */
    public List<ResultadoAcao> consumirResultados() {
        var resultados = List.copyOf(novosResultados);
        novosResultados.clear();
        return resultados;
    }

    public int geracao() { return geracao; }
}
