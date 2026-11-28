package br.unifor.missaomarte.servidor.partida;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import br.unifor.missaomarte.servidor.usuario.Usuario;

/** Resultado de uma partida enviado pelo cliente. O servidor não refaz o jogo. */
@Entity
@Table(name = "partida", indexes = @Index(name = "ix_partida_usuario", columnList = "usuario_id"))
public class Partida {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Dificuldade dificuldade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Resultado resultado;

    @Column(nullable = false)
    private int pontuacao;

    @Column(name = "duracao_segundos", nullable = false)
    private long duracaoSegundos;

    @Column(nullable = false)
    private int movimentos;

    @Column(name = "registrada_em", nullable = false)
    private Instant registradaEm;

    // Fase 7. Coluna anulável para o ddl-auto=update conseguir acrescentá-la a uma tabela com dados;
    // MigracaoModo preenche as partidas antigas com CLASSICO ao iniciar.
    @Enumerated(EnumType.STRING)
    @Column(length = 12)
    private ModoJogo modo;

    /** Só no modo Infinito: setor alcançado. */
    private Integer setor;

    protected Partida() { }

    public Partida(Usuario usuario, Dificuldade dificuldade, Resultado resultado, int pontuacao,
                   long duracaoSegundos, int movimentos) {
        this.usuario = usuario;
        this.dificuldade = dificuldade;
        this.resultado = resultado;
        this.pontuacao = pontuacao;
        this.duracaoSegundos = duracaoSegundos;
        this.movimentos = movimentos;
        this.registradaEm = Instant.now();
        this.modo = ModoJogo.CLASSICO;
    }

    public Partida(Usuario usuario, Dificuldade dificuldade, Resultado resultado, int pontuacao,
                   long duracaoSegundos, int movimentos, ModoJogo modo, Integer setor) {
        this(usuario, dificuldade, resultado, pontuacao, duracaoSegundos, movimentos);
        this.modo = modo;
        this.setor = setor;
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public Dificuldade getDificuldade() { return dificuldade; }
    public Resultado getResultado() { return resultado; }
    public int getPontuacao() { return pontuacao; }
    public long getDuracaoSegundos() { return duracaoSegundos; }
    public int getMovimentos() { return movimentos; }
    public Instant getRegistradaEm() { return registradaEm; }
    public ModoJogo getModo() { return modo == null ? ModoJogo.CLASSICO : modo; }
    public Integer getSetor() { return setor; }
}
