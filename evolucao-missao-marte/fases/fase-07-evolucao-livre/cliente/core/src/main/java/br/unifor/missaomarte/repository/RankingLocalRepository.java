package br.unifor.missaomarte.repository;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import br.unifor.missaomarte.model.Dificuldade;
import br.unifor.missaomarte.model.ModoJogo;

/**
 * Repositório local em arquivo compartilhado entre Desktop e Android (modo convidado).
 * Independente de framework gráfico: recebe o File/Path da raiz de composição de cada plataforma.
 */
public final class RankingLocalRepository implements RankingRepository {
    private final Path arquivo;

    public RankingLocalRepository(File arquivo) {
        this(Objects.requireNonNull(arquivo).toPath());
    }

    public RankingLocalRepository(Path arquivo) {
        this.arquivo = Objects.requireNonNull(arquivo);
    }

    @Override
    public void salvar(String nome, int pontuacao) {
        salvar(nome, pontuacao, Dificuldade.MEDIO, 0, 0);
    }

    @Override
    public void salvar(String nome, int pontuacao, Dificuldade dificuldade, int passageirosColetados, long tempoJogo) {
        salvar(nome, pontuacao, dificuldade, passageirosColetados, tempoJogo, ModoJogo.CLASSICO, 0);
    }

    /** Fase 7: duas colunas a mais no fim da linha; arquivos antigos (6 colunas) continuam legíveis. */
    @Override
    public void salvar(String nome, int pontuacao, Dificuldade dificuldade, int passageirosColetados, long tempoJogo,
                       ModoJogo modo, int setor) {
        List<String> linhas = new ArrayList<>();
        if (Files.exists(arquivo)) {
            try {
                linhas = new ArrayList<>(Files.readAllLines(arquivo, StandardCharsets.UTF_8));
            } catch (IOException ignored) {
            }
        }
        String dataHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        linhas.add(nome + "|" + pontuacao + "|" + dificuldade + "|" + passageirosColetados + "|" + dataHora + "|" + tempoJogo
                + "|" + (modo == null ? ModoJogo.CLASSICO : modo) + "|" + setor);
        try {
            Path parent = arquivo.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(arquivo, linhas, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível salvar o ranking", e);
        }
    }

    @Override
    public List<RankingEntry> listar() {
        if (!Files.exists(arquivo)) {
            return new ArrayList<>();
        }
        try {
            List<String> linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);
            List<RankingEntry> ranking = new ArrayList<>();
            for (String linha : linhas) {
                String[] partes = linha.split("\\|");
                if (partes.length >= 6) {
                    ranking.add(new RankingEntry(
                            partes[0],
                            Integer.parseInt(partes[1]),
                            Dificuldade.deString(partes[2]),
                            Integer.parseInt(partes[3]),
                            partes[4],
                            Long.parseLong(partes[5]),
                            partes.length >= 8 ? modo(partes[6]) : ModoJogo.CLASSICO,
                            partes.length >= 8 ? Integer.parseInt(partes[7]) : 0
                    ));
                }
            }
            ranking.sort(Comparator.comparingInt((RankingEntry entry) -> entry.score).reversed());
            return ranking;
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private static ModoJogo modo(String texto) {
        try {
            return ModoJogo.valueOf(texto);
        } catch (IllegalArgumentException desconhecido) {
            return ModoJogo.CLASSICO;
        }
    }

    @Override
    public void limpar() {
        try {
            Files.deleteIfExists(arquivo);
        } catch (IOException ignored) {
        }
    }
}
