package br.unifor.missaomarte.repository;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import br.unifor.missaomarte.model.Dificuldade;

class RankingLocalRepositoryTest {
    @Test
    void salvaListaOrdenadoELimpaNoDiretorioInternoDaPlataforma(@TempDir Path dir) {
        var arquivo = dir.resolve("interno/ranking-solid-exercicio10.json").toFile();
        var repo = new RankingLocalRepository(arquivo);

        repo.salvar("Ana", 30, Dificuldade.MEDIO, 1, 20);
        repo.salvar("Bia", 45, Dificuldade.FACIL, 1, 15);

        var lista = repo.listar();
        assertEquals(2, lista.size());
        assertEquals("Bia", lista.get(0).name);
        assertEquals(45, lista.get(0).score);
        assertEquals("Ana", lista.get(1).name);

        repo.limpar();
        assertTrue(repo.listar().isEmpty());
    }
}
