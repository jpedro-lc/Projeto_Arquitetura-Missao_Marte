package br.unifor.missaomarte.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import br.unifor.missaomarte.model.Acao;
import br.unifor.missaomarte.model.Dificuldade;
import br.unifor.missaomarte.model.EstadoPartida;
import br.unifor.missaomarte.model.EstadoPartida.NaveVista;
import br.unifor.missaomarte.model.EstadoPartida.PassageiroVista;
import br.unifor.missaomarte.model.EstadoPartida.Posicao;
import br.unifor.missaomarte.model.EstadoPartida.Situacao;
import br.unifor.missaomarte.model.Missao;
import br.unifor.missaomarte.model.ModoJogo;
import br.unifor.missaomarte.model.Nave;
import br.unifor.missaomarte.model.Passageiro;
import br.unifor.missaomarte.model.ResultadoAcao;
import br.unifor.missaomarte.model.ResultadoAcao.Evento;
import br.unifor.missaomarte.model.ResultadoAcao.TipoEvento;
import br.unifor.missaomarte.repository.RankingRepository;

/**
 * Modo tempo real: a partida anda sozinha. Reaproveita Missao, Nave, colisões e FabricaMissoes;
 * muda só quem dá o ritmo (o tempo recebido em avancar, nunca o relógio do sistema).
 */
public final class TempoRealService implements RegrasDePartida {
    /** Evita "saltos" quando um quadro demora (app voltando do segundo plano, depuração). */
    static final float QUADRO_MAXIMO = 0.1f;

    private final RankingRepository ranking;
    private final FabricaMissoes fabrica;
    private final Random randomInimigos;
    private final Function<Dificuldade, RitmoTempoReal> ritmos;
    private Missao missao;
    private RitmoTempoReal ritmo;
    private String piloto;
    private Dificuldade dificuldade;
    private int limite;
    private int pontos;
    private int movimentos;
    private float segundos;
    private float contagem;
    private float desdePassoNave;
    private float acumuladoInimigos;
    private float acumuladoPontos;
    private float invulneravel;
    private boolean avisouRetorno;
    private Situacao situacao;

    public TempoRealService(RankingRepository ranking) {
        this(ranking, new FabricaMissoes(), new Random(), RitmoTempoReal::de);
    }

    public TempoRealService(RankingRepository ranking, FabricaMissoes fabrica, Random randomInimigos,
                            Function<Dificuldade, RitmoTempoReal> ritmos) {
        this.ranking = Objects.requireNonNull(ranking);
        this.fabrica = Objects.requireNonNull(fabrica);
        this.randomInimigos = Objects.requireNonNull(randomInimigos);
        this.ritmos = Objects.requireNonNull(ritmos);
    }

    @Override public ModoJogo modo() { return ModoJogo.TEMPO_REAL; }

    @Override public EstadoPartida iniciar(String piloto, Dificuldade dificuldade, int limite) {
        Objects.requireNonNull(dificuldade);
        if (limite <= 0) throw new IllegalArgumentException("O limite deve ser positivo");
        // Mesmo saldo inicial do clássico; aqui ele escoa 1 ponto por segundo.
        return iniciarSetor(piloto, dificuldade, limite, fabrica.criar(dificuldade, limite),
                Objects.requireNonNull(ritmos.apply(dificuldade)), pontosIniciais(dificuldade));
    }

    /** Saldo de pontos ao começar (e, no Infinito, a recarga de cada setor). */
    public static int pontosIniciais(Dificuldade dificuldade) {
        return switch (dificuldade) {
            case FACIL -> 30;
            case MEDIO -> 20;
            case DIFICIL -> 15;
        };
    }

    /** Começa uma missão já montada (o Infinito monta um setor por vez), com a contagem de largada. */
    EstadoPartida iniciarSetor(String piloto, Dificuldade dificuldade, int limite, Missao missao,
                               RitmoTempoReal ritmo, int pontos) {
        if (limite <= 0) throw new IllegalArgumentException("O limite deve ser positivo");
        this.missao = Objects.requireNonNull(missao);
        this.ritmo = Objects.requireNonNull(ritmo);
        this.piloto = piloto == null || piloto.isBlank() ? "Piloto Anônimo" : piloto.trim();
        this.dificuldade = Objects.requireNonNull(dificuldade);
        this.limite = limite;
        this.pontos = pontos;
        movimentos = 0;
        segundos = 0;
        contagem = ritmo.contagem();
        // A primeira direção pressionada move na hora; segurar repete a cada passoNave.
        desdePassoNave = ritmo.passoNave();
        acumuladoInimigos = 0;
        acumuladoPontos = 0;
        invulneravel = 0;
        avisouRetorno = false;
        situacao = Situacao.EM_ANDAMENTO;
        return consultarEstado();
    }

    @Override public ResultadoAcao executar(Acao acao) {
        Objects.requireNonNull(acao);
        exigirPartida();
        var eventos = new ArrayList<Evento>();
        if (situacao == Situacao.EM_ANDAMENTO) {
            if (acao == Acao.ABORTAR) {
                situacao = Situacao.ABORTADA;
                eventos.add(new Evento(TipoEvento.ABORTOU, "", 0));
            } else if (acao == Acao.EMBARCAR && contagem <= 0) {
                // O embarque já é automático; a tecla continua valendo para quem vem do clássico.
                if (!embarcar(eventos)) eventos.add(new Evento(TipoEvento.SEM_PASSAGEIRO, "", 0));
            }
        }
        return new ResultadoAcao(consultarEstado(), eventos);
    }

    @Override public ResultadoAcao avancar(float decorrido, Acao direcaoMantida) {
        exigirPartida();
        if (situacao != Situacao.EM_ANDAMENTO || decorrido <= 0) return new ResultadoAcao(consultarEstado(), List.of());
        float dt = Math.min(decorrido, QUADRO_MAXIMO);
        if (contagem > 0) {
            contagem = Math.max(0, contagem - dt);
            return new ResultadoAcao(consultarEstado(), List.of());
        }
        var eventos = new ArrayList<Evento>();
        Nave nave = missao.getNave();
        segundos += dt;
        invulneravel = Math.max(0, invulneravel - dt);

        acumuladoPontos += dt;
        while (acumuladoPontos >= ritmo.segundosPorPonto()) {
            acumuladoPontos -= ritmo.segundosPorPonto();
            pontos--;
        }

        desdePassoNave = Math.min(ritmo.passoNave(), desdePassoNave + dt);
        if (direcaoMantida != null && direcaoMantida.movimento() && desdePassoNave >= ritmo.passoNave()) {
            int antesX = nave.getX();
            int antesY = nave.getY();
            nave.moverComLimites(direcaoMantida.dx(), direcaoMantida.dy(), -limite, limite, -limite, limite);
            desdePassoNave = 0;
            if (nave.getX() != antesX || nave.getY() != antesY) {
                movimentos++;
                embarcar(eventos);
            }
        }

        acumuladoInimigos += dt;
        while (acumuladoInimigos >= ritmo.passoInimigos()) {
            acumuladoInimigos -= ritmo.passoInimigos();
            missao.moverInimigos(randomInimigos, limite);
        }

        if (invulneravel <= 0 && missao.verificaColisao()) {
            nave.perderVida();
            invulneravel = ritmo.invulneravelAposColisao();
            if (nave.getVidas() > 0) {
                eventos.add(new Evento(TipoEvento.COLISAO, "", nave.getVidas()));
            } else {
                eventos.add(new Evento(TipoEvento.NAVE_DESTRUIDA, "", 0));
                situacao = Situacao.DERROTA;
            }
        }
        if (pontos <= 0 && situacao == Situacao.EM_ANDAMENTO) {
            pontos = 0;
            eventos.add(new Evento(TipoEvento.SEM_PONTOS, "", 0));
            situacao = Situacao.DERROTA;
        }
        if (missao.todosEmbarcados() && situacao == Situacao.EM_ANDAMENTO) {
            if (nave.getX() == 0 && nave.getY() == 0) {
                situacao = Situacao.VITORIA;
                ranking.salvar(piloto, pontos, dificuldade, nave.getPassageiros().size(), (long) segundos,
                        ModoJogo.TEMPO_REAL, 0);
                eventos.add(new Evento(TipoEvento.VITORIA, "", 0));
            } else if (!avisouRetorno) {
                avisouRetorno = true;
                eventos.add(new Evento(TipoEvento.RETORNAR_BASE, "", 0));
            }
        }
        return new ResultadoAcao(consultarEstado(), eventos);
    }

    /** Embarca quem está na posição da nave; devolve false quando não havia passageiro. */
    private boolean embarcar(List<Evento> eventos) {
        Passageiro passageiro = missao.passagemNaPosicao();
        if (passageiro == null) return false;
        if (missao.embarcarPassageiroNaPosicao()) {
            pontos += passageiro.getPontuacao();
            eventos.add(new Evento(TipoEvento.EMBARCOU, passageiro.getNome(), passageiro.getPontuacao()));
        } else {
            eventos.add(new Evento(TipoEvento.NAVE_CHEIA, "", 0));
        }
        return true;
    }

    @Override public float contagemRestante() { return missao == null ? 0f : contagem; }

    /** Segundos de invulnerabilidade após uma colisão (a tela pisca a nave). */
    public float invulneravelRestante() { return invulneravel; }

    @Override public EstadoPartida consultarEstado() {
        exigirPartida();
        Nave nave = missao.getNave();
        return new EstadoPartida(piloto, dificuldade, limite, pontos, movimentos, (long) segundos, situacao,
                new NaveVista(nave.getX(), nave.getY(), nave.getVidas(), nave.getCapacidade(),
                        nave.getPassageiros().stream().map(TempoRealService::visualizar).toList()),
                missao.getPassageiros().stream().map(TempoRealService::visualizar).toList(),
                missao.getAsteroides().stream().map(a -> new Posicao(a.getX(), a.getY())).toList(),
                missao.getInimigos().stream().map(i -> new Posicao(i.getX(), i.getY())).toList());
    }

    private static PassageiroVista visualizar(Passageiro p) {
        return new PassageiroVista(p.getNome(), p.getTipo(), p.getX(), p.getY(), p.getPontuacao());
    }

    private void exigirPartida() {
        if (missao == null) throw new IllegalStateException("Inicie uma partida antes de jogar");
    }
}
