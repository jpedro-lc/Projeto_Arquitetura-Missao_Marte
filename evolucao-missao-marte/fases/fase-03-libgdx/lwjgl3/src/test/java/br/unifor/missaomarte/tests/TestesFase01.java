package br.unifor.missaomarte.tests;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.function.Supplier;
import br.unifor.missaomarte.model.*;
import br.unifor.missaomarte.model.EstadoPartida.Situacao;
import br.unifor.missaomarte.model.ResultadoAcao.TipoEvento;
import br.unifor.missaomarte.lwjgl3.console.JogoConsole;
import br.unifor.missaomarte.lwjgl3.console.MapaRenderer;
import br.unifor.missaomarte.repository.*;
import br.unifor.missaomarte.lwjgl3.repository.RankingArquivoRepository;
import br.unifor.missaomarte.service.*;

/** Suíte executável sem bibliotecas externas; falhas sempre lançam AssertionError. */
public class TestesFase01 {
    private static int verificacoes;

    public static void main(String[] args) throws Exception {
        testarInicioEMovimento();
        testarEmbarqueEVitoria();
        testarColisoesEPrioridade();
        testarCombustivel();
        testarTurnosEAborto();
        testarImutabilidadeEReinicio();
        testarFabrica();
        testarRankingArquivo();
        testarConsole();
        System.out.println("PASSOU: " + verificacoes + " verificações da Fase 1.");
    }

    private static void testarInicioEMovimento() {
        var c = cenario(TestesFase01::missaoPadrao);
        falha(IllegalStateException.class, () -> c.jogo.consultarEstado(), "estado exige início");
        falha(IllegalStateException.class, () -> c.jogo.executar(Acao.CIMA), "ação exige início");
        falha(IllegalArgumentException.class, () -> c.jogo.iniciar("A", Dificuldade.FACIL, 0), "limite positivo");
        for (Dificuldade d : Dificuldade.values()) {
            int esperado = d == Dificuldade.FACIL ? 30 : d == Dificuldade.MEDIO ? 20 : 15;
            var e = c.jogo.iniciar(" Ana ", d, 1);
            ok(e.pontos() == esperado && e.nave().vidas() == 3, "pontos e vidas iniciais " + d);
        }
        c.jogo.iniciar(" Ana ", Dificuldade.FACIL, 1);
        ok(c.jogo.consultarEstado().piloto().equals("Ana"), "nome normalizado");
        c.jogo.executar(Acao.DIREITA);
        var e = c.jogo.executar(Acao.DIREITA).estado();
        ok(e.nave().x() == 1 && e.pontos() == 28 && e.movimentos() == 2, "borda cobra ponto e movimento");
        c.jogo.executar(Acao.CIMA);
        c.jogo.executar(Acao.CIMA);
        ok(c.jogo.consultarEstado().nave().y() == 1, "borda superior");
        c.jogo.executar(Acao.BAIXO);
        c.jogo.executar(Acao.BAIXO);
        c.jogo.executar(Acao.BAIXO);
        ok(c.jogo.consultarEstado().nave().y() == -1, "borda inferior");
        c.jogo.executar(Acao.ESQUERDA);
        c.jogo.executar(Acao.ESQUERDA);
        e = c.jogo.executar(Acao.ESQUERDA).estado();
        ok(e.nave().x() == -1 && e.movimentos() == 10 && e.pontos() == 20, "borda esquerda e quatro direções");
    }

    private static void testarEmbarqueEVitoria() {
        var c = cenario(() -> {
            var m = new Missao(new Nave("Teste", 0, 0, 3));
            m.adicionarPassageiro(new Professor("Ada", 1, 0));
            m.adicionarPassageiro(new Engenheiro("Rosa", 2, 0));
            m.adicionarPassageiro(new Astronauta("Lua", 3, 0));
            return m;
        });
        c.jogo.iniciar("Ana", Dificuldade.FACIL, 5);
        evento(c.jogo.executar(Acao.EMBARCAR), TipoEvento.SEM_PASSAGEIRO, "embarque vazio");
        int[] pontos = {44, 63, 72};
        for (int i = 0; i < 3; i++) {
            c.jogo.executar(Acao.DIREITA);
            var r = c.jogo.executar(Acao.EMBARCAR);
            evento(r, TipoEvento.EMBARCOU, "embarque polimórfico");
            ok(r.estado().pontos() == pontos[i], "bônus por passageiro " + i);
            ok(r.estado().nave().passageiros().size() == i + 1 && r.estado().passageiros().size() == 2 - i, "transferência passageiro");
        }
        ok(c.jogo.consultarEstado().situacao() == Situacao.EM_ANDAMENTO && c.ranking.dados.isEmpty(), "resgate longe da base não vence");
        evento(c.jogo.executar(Acao.VAZIA), null, "vazio não gera evento");
        evento(c.jogo.executar(Acao.ESQUERDA), TipoEvento.RETORNAR_BASE, "aviso de retorno");
        c.jogo.executar(Acao.ESQUERDA);
        c.tempo[0] = 12_000;
        var r = c.jogo.executar(Acao.ESQUERDA);
        evento(r, TipoEvento.VITORIA, "vitória na origem");
        ok(r.estado().situacao() == Situacao.VITORIA && r.estado().pontos() == 69, "resultado da vitória");
        ok(r.estado().movimentos() == 6 && r.estado().segundos() == 12, "estatísticas da vitória");
        var entry = c.ranking.listar().get(0);
        ok(entry.name.equals("Ana") && entry.score == 69 && entry.dificuldade == Dificuldade.FACIL, "ranking recebe identidade e pontos");
        ok(entry.passageirosColetados == 3 && entry.tempoJogo == 12, "ranking recebe passageiros e duração");
        c.tempo[0] = 99_000;
        var encerrada = c.jogo.executar(Acao.CIMA);
        ok(c.ranking.dados.size() == 1 && encerrada.eventos().isEmpty(), "resultado salvo uma vez");
        ok(encerrada.estado().segundos() == 12 && encerrada.estado().nave().y() == 0, "partida encerrada congela tempo e ações");

        var cheia = cenario(() -> {
            var m = new Missao(new Nave("Sem vagas", 0, 0, 0));
            m.adicionarPassageiro(new Professor("Ada", 0, 0));
            return m;
        });
        cheia.jogo.iniciar("Ana", Dificuldade.FACIL, 5);
        evento(cheia.jogo.executar(Acao.EMBARCAR), TipoEvento.NAVE_CHEIA, "capacidade respeitada");
        ok(cheia.jogo.consultarEstado().pontos() == 30 && cheia.jogo.consultarEstado().passageiros().size() == 1, "recusa não pontua nem remove");
    }

    private static void testarColisoesEPrioridade() {
        for (boolean inimigo : new boolean[] {false, true}) {
            var c = cenario(() -> {
                var m = missaoPadrao();
                if (inimigo) m.adicionarInimigo(new Inimigo(0, 0));
                else m.adicionarAsteroide(new Asteroide(0, 0));
                return m;
            });
            c.jogo.iniciar("Ana", Dificuldade.MEDIO, 5);
            var r = c.jogo.executar(Acao.EMBARCAR);
            evento(r, TipoEvento.COLISAO, "colisão " + inimigo);
            ok(r.estado().nave().vidas() == 2, "perde uma vida");
            c.jogo.executar(Acao.EMBARCAR);
            r = c.jogo.executar(Acao.EMBARCAR);
            evento(r, TipoEvento.NAVE_DESTRUIDA, "terceira colisão");
            ok(r.estado().situacao() == Situacao.DERROTA && c.ranking.dados.isEmpty(), "derrota não salva ranking");
        }
        var c = cenario(() -> {
            var nave = new Nave("Teste", 0, 0);
            nave.perderVida(); nave.perderVida();
            var m = new Missao(nave);
            m.adicionarAsteroide(new Asteroide(0, 0));
            return m;
        });
        c.jogo.iniciar("A", Dificuldade.FACIL, 5);
        var r = c.jogo.executar(Acao.ABORTAR);
        evento(r, TipoEvento.ABORTOU, "aborto informado antes de colisão");
        evento(r, TipoEvento.NAVE_DESTRUIDA, "aborto ainda verifica colisão fatal");
        ok(r.estado().situacao() == Situacao.DERROTA && c.ranking.dados.isEmpty(), "colisão precede vitória na base");
    }

    private static void testarCombustivel() {
        var c = cenario(TestesFase01::missaoPadrao);
        c.jogo.iniciar("Ana", Dificuldade.DIFICIL, 5);
        ResultadoAcao r = null;
        for (int i = 0; i < 15; i++) r = c.jogo.executar(i % 2 == 0 ? Acao.DIREITA : Acao.ESQUERDA);
        evento(r, TipoEvento.SEM_PONTOS, "combustível zerado");
        ok(r.estado().situacao() == Situacao.DERROTA && r.estado().pontos() == 0, "derrota por combustível");
        var prioridade = cenario(() -> new Missao(new Nave("Teste", 1, 0)));
        prioridade.jogo.iniciar("Ana", Dificuldade.MEDIO, 1);
        for (int i = 0; i < 19; i++) prioridade.jogo.executar(Acao.DIREITA);
        r = prioridade.jogo.executar(Acao.ESQUERDA);
        ok(r.estado().nave().x() == 0 && r.estado().pontos() == 0, "retorno no último ponto");
        ok(r.estado().situacao() == Situacao.DERROTA && prioridade.ranking.dados.isEmpty(), "combustível precede vitória");
    }

    private static void testarTurnosEAborto() {
        var c = cenario(() -> {
            var m = missaoPadrao(); m.adicionarInimigo(new Inimigo(4, 4)); return m;
        });
        c.jogo.iniciar("", Dificuldade.MEDIO, 5);
        ok(c.jogo.consultarEstado().piloto().equals("Piloto Anônimo"), "nome padrão");
        evento(c.jogo.executar(Acao.VAZIA), null, "vazio sem eventos");
        ok(c.random.chamadas == 0, "vazio não move inimigos");
        evento(c.jogo.executar(Acao.INVALIDA), TipoEvento.COMANDO_INVALIDO, "inválido gera evento");
        ok(c.random.chamadas == 2 && c.jogo.consultarEstado().pontos() == 20, "inválido move inimigos sem custo");
        var r = c.jogo.executar(Acao.ABORTAR);
        ok(c.random.chamadas == 4 && r.estado().situacao() == Situacao.ABORTADA, "aborto ainda move inimigos");
        ok(c.ranking.dados.isEmpty(), "aborto não salva");
        c.jogo.executar(Acao.CIMA);
        ok(c.random.chamadas == 4, "não há turnos após encerrar");
        var m = new Missao(new Nave("T", 0, 0));
        m.adicionarInimigo(new Inimigo(5, 5));
        m.moverInimigos(new RandomFixo(2));
        ok(m.getInimigos().get(0).getX() == 6 && m.getInimigos().get(0).getY() == 6, "movimento de inimigos mantém ausência de limite");
    }

    private static void testarImutabilidadeEReinicio() {
        var c = cenario(TestesFase01::missaoPadrao);
        var antes = c.jogo.iniciar("A", Dificuldade.FACIL, 5);
        falha(UnsupportedOperationException.class, () -> antes.passageiros().clear(), "lista visível imutável");
        falha(UnsupportedOperationException.class, () -> antes.nave().passageiros().clear(), "tripulação visível imutável");
        var resultado = c.jogo.executar(Acao.DIREITA);
        falha(UnsupportedOperationException.class, () -> resultado.eventos().clear(), "eventos imutáveis");
        ok(antes.nave().x() == 0 && resultado.estado().nave().x() == 1, "fotografia anterior não acompanha mutação");
        var m = missaoPadrao();
        falha(UnsupportedOperationException.class, () -> m.getPassageiros().clear(), "coleção de domínio protegida");
        falha(UnsupportedOperationException.class, () -> m.getAsteroides().clear(), "coleção de asteroides protegida");
        falha(UnsupportedOperationException.class, () -> m.getInimigos().clear(), "coleção de inimigos protegida");
        falha(UnsupportedOperationException.class, () -> m.getNave().getPassageiros().clear(), "coleção da nave protegida");
        c.jogo.executar(Acao.ABORTAR);
        c.tempo[0] = 50_000;
        var nova = c.jogo.iniciar("B", Dificuldade.MEDIO, 5);
        ok(nova.movimentos() == 0 && nova.pontos() == 20 && nova.nave().x() == 0, "reinício limpa estado");
        ok(nova.segundos() == 0 && nova.situacao() == Situacao.EM_ANDAMENTO, "reinício limpa tempo e situação");
        var captura = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try (var saida = new PrintStream(captura, true, StandardCharsets.UTF_8)) {
            System.setOut(saida);
            c.jogo.executar(Acao.DIREITA); c.jogo.executar(Acao.EMBARCAR); c.jogo.consultarEstado();
        } finally { System.setOut(original); }
        ok(captura.size() == 0, "serviço não escreve no terminal");
    }

    private static void testarFabrica() {
        for (Dificuldade d : Dificuldade.values()) {
            var fabrica = new FabricaMissoes(new Random(77));
            var m = fabrica.criar(d, 5);
            int passageiros = d == Dificuldade.FACIL ? 4 : d == Dificuldade.MEDIO ? 5 : 6;
            int perigos = d == Dificuldade.DIFICIL ? 3 : 2;
            ok(m.getPassageiros().size() == passageiros && m.getNave().getCapacidade() == passageiros, "quantidade de passageiros " + d);
            ok(m.getAsteroides().size() == perigos && m.getInimigos().size() == perigos, "quantidade de perigos " + d);
            var entidades = new ArrayList<EntidadeMapa>();
            entidades.addAll(m.getPassageiros()); entidades.addAll(m.getAsteroides()); entidades.addAll(m.getInimigos());
            var posicoes = new HashSet<String>();
            for (var e : entidades) {
                ok(e.getX() >= -5 && e.getX() <= 5 && e.getY() >= -5 && e.getY() <= 5, "entidade dentro do cenário");
                ok(!(e.getX() == 0 && e.getY() == 0) && posicoes.add(e.getX() + "," + e.getY()), "sem sobreposição inicial");
            }
            ok(m.getPassageiros().stream().noneMatch(p -> p instanceof Astronauta), "geração preserva ausência de astronautas");
            var igual = new FabricaMissoes(new Random(77)).criar(d, 5);
            ok(igual.getPassageiros().get(0).getX() == m.getPassageiros().get(0).getX(), "semente reproduz cenário");
        }
        falha(IllegalStateException.class, () -> new FabricaMissoes(new RandomFixo(0)).criar(Dificuldade.FACIL, 5), "limite de tentativas preservado");
        falha(IllegalStateException.class, () -> new FabricaMissoes(new Random(7)).criar(Dificuldade.DIFICIL, 1), "mapa pequeno continua falhando");
    }

    private static void testarRankingArquivo() throws Exception {
        Path pasta = Files.createTempDirectory("marte-fase01-ranking-");
        Path arquivo = pasta.resolve("dados/ranking-solid-exercicio10.json");
        try {
            RankingRepository r = new RankingArquivoRepository(arquivo.toString());
            ok(r.listar().isEmpty(), "ranking ausente é vazio");
            r.salvar("Ana", 10);
            r.salvar("Bia", 99, Dificuldade.DIFICIL, 6, 12);
            r.salvar("Cris", 30, Dificuldade.FACIL, 4, 7);
            var leitura = new RankingArquivoRepository(arquivo.toString()).listar();
            ok(leitura.size() == 3 && leitura.get(0).score == 99 && leitura.get(2).score == 10, "persistência e ordenação");
            ok(leitura.get(0).passageirosColetados == 6 && leitura.get(0).tempoJogo == 12 && leitura.get(0).dificuldade == Dificuldade.DIFICIL, "roundtrip dos campos");
            ok(Files.readString(arquivo).contains("Bia|99|DIFICIL|6|"), "compatibilidade do formato delimitado");
            Files.writeString(arquivo, "Legado|51|MEDIO|5|13/11/2026 12:00|19\n", StandardCharsets.UTF_8);
            ok(r.listar().get(0).name.equals("Legado") && r.listar().get(0).tempoJogo == 19, "leitura de arquivo legado");
            r.salvar("Novo", 12);
            ok(r.listar().size() == 2, "append mantém legado");
            r.limpar();
            ok(!Files.exists(arquivo) && r.listar().isEmpty(), "limpeza em disco");
            r.limpar();
            ok(!Files.exists(arquivo), "limpeza de arquivo inexistente");
        } finally {
            Files.deleteIfExists(arquivo);
            Files.deleteIfExists(arquivo.getParent());
            Files.deleteIfExists(pasta);
        }
    }

    private static void testarConsole() {
        var c = cenario(() -> {
            var m = new Missao(new Nave("T", 0, 0));
            m.adicionarPassageiro(new Professor("Ada", 1, 0)); return m;
        });
        String texto = console(c.jogo, "99\n2\n1\nAna\nfacil\n5\n\n\nZ\nD\nC\nA\n2\n3\n2\n4\n");
        ok(texto.contains("Opção inválida") && texto.contains("Comando inválido"), "menus e comandos inválidos");
        ok(texto.contains("Mapa da Missão") && texto.contains("@=Nave") && texto.contains("Ada embarcado"), "mapa e embarque no console");
        ok(texto.contains("MISSÃO CUMPRIDA") || texto.contains("Missão cumprida"), "vitória no console");
        ok(texto.contains("Pontuação final: 43") && texto.contains("Movimentos realizados: 2"), "estatísticas no console");
        ok(texto.contains("TOP 5") && texto.contains("1. Ana") && texto.contains("Histórico de ranking removido"), "listagem e reset");
        ok(texto.contains("Obrigado por jogar") && c.ranking.dados.isEmpty(), "saída e ranking limpo");
        var abortar = cenario(TestesFase01::missaoPadrao);
        texto = console(abortar.jogo, "1\n\n\nabc\n\nq\n4\n");
        ok(texto.contains("tamanho padrão (5)") && texto.contains("Missão abortada pelo piloto"), "padrões, tamanho inválido e aborto");
        for (String prefixo : List.of("", "1\n", "1\nAna\n", "1\nAna\nfacil\n", "1\nAna\nfacil\n5\n", "1\nAna\nfacil\n5\n\n")) {
            console(cenario(TestesFase01::missaoPadrao).jogo, prefixo);
            ok(true, "fim de entrada tratado em cada etapa");
        }
        var top = cenario(TestesFase01::missaoPadrao);
        for (int i = 1; i <= 7; i++) top.jogo.registrarPontuacao("Piloto" + i, 100 - i);
        texto = console(top.jogo, "2\n4\n");
        ok(texto.contains("Piloto5") && !texto.contains("Piloto6") && top.ranking.dados.size() == 7, "exibe cinco sem truncar persistência");
    }

    private static String console(JogoService jogo, String entrada) {
        var captura = new ByteArrayOutputStream();
        try (var scanner = new Scanner(entrada); var saida = new PrintStream(captura, true, StandardCharsets.UTF_8)) {
            new JogoConsole(jogo, scanner, saida, new MapaRenderer(saida)).executar();
        }
        return captura.toString(StandardCharsets.UTF_8);
    }

    private static Missao missaoPadrao() {
        var m = new Missao(new Nave("Teste", 0, 0));
        m.adicionarPassageiro(new Professor("Referência", 1, 1));
        return m;
    }

    private static Cenario cenario(Supplier<Missao> origem) {
        var ranking = new RankingMemoria();
        var random = new RandomFixo(1); // nextInt(3)-1 = 0, inimigos imóveis.
        long[] tempo = {0};
        var fabrica = new FabricaMissoes(new Random(1)) {
            @Override public Missao criar(Dificuldade dificuldade, int limite) { return origem.get(); }
        };
        return new Cenario(new JogoService(ranking, fabrica, random, () -> tempo[0]), ranking, random, tempo);
    }

    private record Cenario(JogoService jogo, RankingMemoria ranking, RandomFixo random, long[] tempo) { }

    private static class RandomFixo extends Random {
        private static final long serialVersionUID = 1L;
        private final int valor;
        int chamadas;
        RandomFixo(int valor) { this.valor = valor; }
        @Override public int nextInt(int bound) { chamadas++; return valor % bound; }
    }

    private static class RankingMemoria implements RankingRepository {
        final List<RankingEntry> dados = new ArrayList<>();
        public void salvar(String nome, int pontos) { salvar(nome, pontos, Dificuldade.MEDIO, 0, 0); }
        public void salvar(String nome, int pontos, Dificuldade dificuldade, int passageiros, long segundos) {
            dados.add(new RankingEntry(nome, pontos, dificuldade, passageiros, "teste", segundos));
        }
        public List<RankingEntry> listar() { return dados.stream().sorted(Comparator.comparingInt((RankingEntry e) -> e.score).reversed()).toList(); }
        public void limpar() { dados.clear(); }
    }

    private static void evento(ResultadoAcao r, TipoEvento tipo, String descricao) {
        ok(tipo == null ? r.eventos().isEmpty() : r.eventos().stream().anyMatch(e -> e.tipo() == tipo), descricao);
    }
    private static void ok(boolean condicao, String descricao) {
        if (!condicao) throw new AssertionError(descricao);
        verificacoes++;
    }
    private static void falha(Class<? extends Throwable> tipo, Runnable operacao, String descricao) {
        try { operacao.run(); } catch (Throwable e) {
            if (!tipo.isInstance(e)) throw new AssertionError(descricao, e);
            verificacoes++; return;
        }
        throw new AssertionError(descricao + ": exceção esperada " + tipo.getSimpleName());
    }
}
