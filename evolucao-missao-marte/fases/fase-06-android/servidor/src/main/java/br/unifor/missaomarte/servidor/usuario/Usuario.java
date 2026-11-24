package br.unifor.missaomarte.servidor.usuario;

import java.time.Instant;
import java.util.Locale;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** Conta do jogador. Guarda só o hash da senha; nunca sai pela API (usa-se UsuarioResposta). */
@Entity
@Table(name = "usuario", uniqueConstraints = @UniqueConstraint(name = "uk_usuario_nome_normalizado",
        columnNames = "nome_normalizado"))
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String nome;

    // Índice único no banco: "Ana" e "ana" não convivem, mesmo com dois cadastros simultâneos.
    @Column(name = "nome_normalizado", nullable = false, length = 20)
    private String nomeNormalizado;

    @Column(name = "senha_hash", nullable = false, length = 100)
    private String senhaHash;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    protected Usuario() { }

    public Usuario(String nome, String senhaHash) {
        this.nome = nome;
        this.nomeNormalizado = normalizar(nome);
        this.senhaHash = senhaHash;
        this.criadoEm = Instant.now();
    }

    public static String normalizar(String nome) { return nome.trim().toLowerCase(Locale.ROOT); }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getSenhaHash() { return senhaHash; }
    public Instant getCriadoEm() { return criadoEm; }

    @Override public String toString() { return "Usuario[id=" + id + ", nome=" + nome + "]"; }
}
