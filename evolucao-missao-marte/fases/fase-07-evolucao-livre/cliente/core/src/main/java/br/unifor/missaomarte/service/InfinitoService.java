package br.unifor.missaomarte.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import br.unifor.missaomarte.model.Acao;
import br.unifor.missaomarte.model.Dificuldade;
import br.unifor.missaomarte.model.EstadoPartida;
import br.unifor.missaomarte.model.EstadoPartida.Situacao;
import br.unifor.missaomarte.model.ModoJogo;
import br.unifor.missaomarte.model.ResultadoAcao;
import br.unifor.missaomarte.model.ResultadoAcao.Evento;
import br.unifor.missaomarte.model.ResultadoAcao.TipoEvento;
import br.unifor.missaomarte.repository.RankingEntry;
import br.unifor.missaomarte.repository.RankingRepository;

/**
 * Modo Infinito: uma corrida de setores em tempo real. Cada setor é uma partida do TempoRealService;
 * ao vencer um, o próximo começa mais difícil (ProgressaoInfinita) com os pontos e as vidas que sobraram.
 * A corrida só termina em derrota (ou aborto, que não é registrado, como nos outros modos).
 */
public final class InfinitoService implements RegrasDePartida {
    private static final RankingRepository SEM_RANKING = new RankingRepository() {
        @Override public void salvar(String nome, int pontuacao) { }
        @Override public void salvar(String nome, int p, Dificuldade d, int passageiros, long tempo) { }
        @Override public List<RankingEntry> listar() { return List.of(); }
        @Override public void limpar() { }
    };

    private final RankingRepository ranking;
    private final FabricaMissoes fabrica;
    /** O motor não grava vitórias de setor: só a corrida inteira vai para o ranking. */
    private final TempoRealService motor;
    private String piloto;
    private Dificuldade dificuldade;
    private int limiteInicial;
    private int setor;
    private int movimentosAnteriores;
    private long segundosAnteriores;
    private int passageirosAnteriores;
    private boolean registrada;

    public InfinitoService(RankingRepository ranking) {
        this(ranking, new FabricaMissoes(), new Random());
    }

    public InfinitoService(RankingRepository ranking, FabricaMissoes fabrica, Random randomInimigos) {
        this.ranking = Objects.requireNonNull(ranking);
        this.fabrica = Objects.requireNonNull(fabrica);
        this.motor = new TempoRealService(SEM_RANKING, fabrica, randomInimigos, RitmoTempoReal::de);
    }

    @Override public ModoJogo modo() { return ModoJogo.INFINITO; }
    @Override public int setor() { return setor; }
    @Override public float contagemRestante() { return setor == 0 ? 0f : motor.contagemRestante(); }

    @Override public EstadoPartida iniciar(String piloto, Dificuldade dificuldade, int limite) {
        if (limite <= 0) throw new IllegalArgumentException("O limite deve ser positivo");
        this.piloto = piloto;
        this.dificuldade = Objects.requireNonNull(dificuldade);
        limiteInicial = limite;
        movimentosAnteriores = 0;
        segundosAnteriores = 0;
        passageirosAnteriores = 0;
        registrada = false;
        entrarNoSetor(1, 3, 0);
        return consultarEstado();
    }

    private void entrarNoSetor(int novo, int vidasRestantes, int pontosRestantes) {
        setor = novo;
        int limite = ProgressaoInfinita.limite(limiteInicial, setor);
        var missao = fabrica.criarSetor(dificuldade, limite, ProgressaoInfinita.asteroidesExtras(setor),
                ProgressaoInfinita.inimigosExtras(setor), ProgressaoInfinita.vidasAoEntrar(vidasRestantes, setor));
        motor.iniciarSetor(piloto, dificuldade, limite, missao, ProgressaoInfinita.ritmo(dificuldade, setor),
                pontosRestantes + TempoRealService.pontosIniciais(dificuldade));
    }

    @Override public ResultadoAcao executar(Acao acao) {
        exigirCorrida();
        return traduzir(motor.executar(acao));
    }

    @Override public ResultadoAcao avancar(float segundos, Acao direcaoMantida) {
        exigirCorrida();
        return traduzir(motor.avancar(segundos, direcaoMantida));
    }

    /** Vitória do motor = setor concluído: troca o evento e já monta o próximo setor. */
    private ResultadoAcao traduzir(ResultadoAcao doMotor) {
        var estado = doMotor.estado();
        if (estado.situacao() == Situacao.VITORIA) {
            var eventos = new ArrayList<Evento>();
            for (var e : doMotor.eventos()) {
                eventos.add(e.tipo() == TipoEvento.VITORIA ? new Evento(TipoEvento.SETOR_CONCLUIDO, "", setor) : e);
            }
            movimentosAnteriores += estado.movimentos();
            segundosAnteriores += estado.segundos();
            passageirosAnteriores += estado.nave().passageiros().size();
            entrarNoSetor(setor + 1, estado.nave().vidas(), estado.pontos());
            return new ResultadoAcao(consultarEstado(), eventos);
        }
        if (estado.situacao() == Situacao.DERROTA && !registrada) {
            registrada = true;
            var total = consultarEstado();
            ranking.salvar(total.piloto(), total.pontos(), dificuldade,
                    passageirosAnteriores + estado.nave().passageiros().size(), total.segundos(), ModoJogo.INFINITO, setor);
        }
        return new ResultadoAcao(consultarEstado(), doMotor.eventos());
    }

    /** Pontos, vidas e mapa são do setor atual; movimentos e tempo somam a corrida inteira. */
    @Override public EstadoPartida consultarEstado() {
        exigirCorrida();
        var e = motor.consultarEstado();
        return new EstadoPartida(e.piloto(), e.dificuldade(), e.limite(), e.pontos(),
                movimentosAnteriores + e.movimentos(), segundosAnteriores + e.segundos(), e.situacao(),
                e.nave(), e.passageiros(), e.asteroides(), e.inimigos());
    }

    private void exigirCorrida() {
        if (setor == 0) throw new IllegalStateException("Inicie uma partida antes de jogar");
    }
}
