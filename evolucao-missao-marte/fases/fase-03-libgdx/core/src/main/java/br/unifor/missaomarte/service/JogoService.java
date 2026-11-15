package br.unifor.missaomarte.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.LongSupplier;
import br.unifor.missaomarte.model.*;
import br.unifor.missaomarte.model.EstadoPartida.*;
import br.unifor.missaomarte.model.ResultadoAcao.Evento;
import br.unifor.missaomarte.model.ResultadoAcao.TipoEvento;
import br.unifor.missaomarte.repository.RankingEntry;
import br.unifor.missaomarte.repository.RankingRepository;

/** Controller da aplicação: processa um turno sem depender da interface. */
public class JogoService {
    private final RankingRepository ranking;
    private final FabricaMissoes fabrica;
    private final Random randomInimigos;
    private final LongSupplier relogio;
    private Missao missao;
    private String piloto;
    private Dificuldade dificuldade;
    private int limite;
    private int pontos;
    private int movimentos;
    private long inicio;
    private long fim;
    private Situacao situacao;

    public JogoService(RankingRepository ranking) {
        this(ranking, new FabricaMissoes(), new Random(), System::currentTimeMillis);
    }

    public JogoService(RankingRepository ranking, FabricaMissoes fabrica,
                       Random randomInimigos, LongSupplier relogio) {
        this.ranking = Objects.requireNonNull(ranking);
        this.fabrica = Objects.requireNonNull(fabrica);
        this.randomInimigos = Objects.requireNonNull(randomInimigos);
        this.relogio = Objects.requireNonNull(relogio);
    }

    public EstadoPartida iniciar(String piloto, Dificuldade dificuldade, int limite) {
        Objects.requireNonNull(dificuldade);
        if (limite <= 0) throw new IllegalArgumentException("O limite deve ser positivo");
        Missao novaMissao = fabrica.criar(dificuldade, limite);
        this.piloto = piloto == null || piloto.isBlank() ? "Piloto Anônimo" : piloto.trim();
        this.dificuldade = dificuldade;
        this.limite = limite;
        this.missao = novaMissao;
        this.pontos = switch (dificuldade) {
            case FACIL -> 30;
            case MEDIO -> 20;
            case DIFICIL -> 15;
        };
        this.movimentos = 0;
        this.inicio = relogio.getAsLong();
        this.situacao = Situacao.EM_ANDAMENTO;
        return consultarEstado();
    }

    public ResultadoAcao executar(Acao acao) {
        Objects.requireNonNull(acao);
        exigirPartida();
        if (situacao != Situacao.EM_ANDAMENTO || acao == Acao.VAZIA) {
            return new ResultadoAcao(consultarEstado(), List.of());
        }
        List<Evento> eventos = new ArrayList<>();
        Nave nave = missao.getNave();
        if (acao.movimento()) {
            nave.moverComLimites(acao.dx(), acao.dy(), -limite, limite, -limite, limite);
            pontos--;
            movimentos++;
        } else if (acao == Acao.EMBARCAR) {
            embarcar(eventos);
        } else if (acao == Acao.ABORTAR) {
            encerrar(Situacao.ABORTADA);
            eventos.add(new Evento(TipoEvento.ABORTOU, "", 0));
        } else {
            eventos.add(new Evento(TipoEvento.COMANDO_INVALIDO, "", 0));
        }

        // A ordem do turno é a mesma da base, inclusive após aborto e comando inválido.
        missao.moverInimigos(randomInimigos);
        if (missao.verificaColisao()) {
            nave.perderVida();
            if (nave.getVidas() > 0) {
                eventos.add(new Evento(TipoEvento.COLISAO, "", nave.getVidas()));
            } else {
                eventos.add(new Evento(TipoEvento.NAVE_DESTRUIDA, "", 0));
                encerrar(Situacao.DERROTA);
            }
        }
        if (pontos <= 0 && situacao == Situacao.EM_ANDAMENTO) {
            eventos.add(new Evento(TipoEvento.SEM_PONTOS, "", 0));
            encerrar(Situacao.DERROTA);
        }
        if (missao.todosEmbarcados() && situacao == Situacao.EM_ANDAMENTO) {
            if (nave.getX() == 0 && nave.getY() == 0) {
                encerrar(Situacao.VITORIA);
                ranking.salvar(piloto, pontos, dificuldade, nave.getPassageiros().size(), (fim - inicio) / 1000);
                eventos.add(new Evento(TipoEvento.VITORIA, "", 0));
            } else {
                eventos.add(new Evento(TipoEvento.RETORNAR_BASE, "", 0));
            }
        }
        return new ResultadoAcao(consultarEstado(), eventos);
    }

    private void embarcar(List<Evento> eventos) {
        Passageiro passageiro = missao.passagemNaPosicao();
        if (passageiro == null) {
            eventos.add(new Evento(TipoEvento.SEM_PASSAGEIRO, "", 0));
        } else if (missao.embarcarPassageiroNaPosicao()) {
            pontos += passageiro.getPontuacao();
            eventos.add(new Evento(TipoEvento.EMBARCOU, passageiro.getNome(), passageiro.getPontuacao()));
        } else {
            eventos.add(new Evento(TipoEvento.NAVE_CHEIA, "", 0));
        }
    }

    /** Constrói dados novos, sem expor Missao, Nave ou Passageiro mutáveis. */
    public EstadoPartida consultarEstado() {
        exigirPartida();
        Nave nave = missao.getNave();
        long agora = situacao == Situacao.EM_ANDAMENTO ? relogio.getAsLong() : fim;
        return new EstadoPartida(piloto, dificuldade, limite, pontos, movimentos,
                (agora - inicio) / 1000, situacao,
                new NaveVista(nave.getX(), nave.getY(), nave.getVidas(), nave.getCapacidade(),
                        nave.getPassageiros().stream().map(this::visualizar).toList()),
                missao.getPassageiros().stream().map(this::visualizar).toList(),
                missao.getAsteroides().stream().map(a -> new Posicao(a.getX(), a.getY())).toList(),
                missao.getInimigos().stream().map(i -> new Posicao(i.getX(), i.getY())).toList());
    }

    private PassageiroVista visualizar(Passageiro p) {
        return new PassageiroVista(p.getNome(), p.getTipo(), p.getX(), p.getY(), p.getPontuacao());
    }

    public void registrarPontuacao(String nome, int pontuacao) { ranking.salvar(nome, pontuacao); }
    public List<RankingEntry> listarRanking() { return List.copyOf(ranking.listar()); }
    public void limparRanking() { ranking.limpar(); }

    private void encerrar(Situacao situacao) {
        this.situacao = situacao;
        this.fim = relogio.getAsLong();
    }

    private void exigirPartida() {
        if (missao == null) throw new IllegalStateException("Inicie uma partida antes de jogar");
    }
}
