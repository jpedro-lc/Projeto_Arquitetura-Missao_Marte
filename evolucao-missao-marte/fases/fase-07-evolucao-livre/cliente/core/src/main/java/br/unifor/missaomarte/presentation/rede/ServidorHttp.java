package br.unifor.missaomarte.presentation.rede;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpRequestBuilder;
import br.unifor.missaomarte.model.Dificuldade;
import br.unifor.missaomarte.model.ModoJogo;
import br.unifor.missaomarte.remoto.LinhaRanking;
import br.unifor.missaomarte.remoto.PartidaConcluida;
import br.unifor.missaomarte.remoto.PartidaRegistrada;
import br.unifor.missaomarte.remoto.Resposta;
import br.unifor.missaomarte.remoto.Retorno;
import br.unifor.missaomarte.remoto.ServidorDoJogo;
import br.unifor.missaomarte.remoto.Sessao;

/**
 * Adaptador HTTP da porta ServidorDoJogo com Gdx.net (funciona no desktop e no Android).
 * O LibGDX chama o listener em outra thread: todo resultado volta pela thread de desenho (postRunnable).
 */
public final class ServidorHttp implements ServidorDoJogo {
    // O Render gratuito dorme após 15 min e leva cerca de 1 min para responder ao acordar.
    static final int TEMPO_LIMITE_MS = 70_000;
    static final String SEM_CONEXAO = "Não foi possível conectar ao servidor.";
    static final String SESSAO_EXPIRADA = "Sua sessão expirou. Entre novamente.";
    private final String base;

    public ServidorHttp(String base) {
        this.base = Objects.requireNonNull(base).replaceAll("/+$", "");
    }

    @Override public void cadastrar(String nome, String senha, Retorno<String> retorno) {
        chamar(Net.HttpMethods.POST, "/api/usuarios", JsonDoServidor.credenciais(nome, senha), null,
                JsonDoServidor::lerNome, retorno);
    }

    @Override public void entrar(String nome, String senha, Retorno<Sessao> retorno) {
        chamar(Net.HttpMethods.POST, "/api/auth/login", JsonDoServidor.credenciais(nome, senha), null,
                corpo -> new Sessao(nome, JsonDoServidor.lerToken(corpo)), retorno);
    }

    @Override public void enviarPartida(Sessao sessao, PartidaConcluida partida, Retorno<PartidaRegistrada> retorno) {
        chamar(Net.HttpMethods.POST, "/api/partidas", JsonDoServidor.partida(partida), sessao,
                JsonDoServidor::lerPartida, retorno);
    }

    @Override public void carregarRanking(Sessao sessao, Retorno<List<LinhaRanking>> retorno) {
        chamar(Net.HttpMethods.GET, "/api/ranking", null, sessao, JsonDoServidor::lerRanking, retorno);
    }

    @Override public void carregarRanking(Sessao sessao, ModoJogo modo, Dificuldade dificuldade,
                                          Retorno<List<LinhaRanking>> retorno) {
        chamar(Net.HttpMethods.GET, "/api/ranking?modo=" + modo.name() + "&dificuldade=" + dificuldade.name(), null, sessao,
                JsonDoServidor::lerRanking, retorno);
    }

    @Override public void carregarHistorico(Sessao sessao, Retorno<List<PartidaRegistrada>> retorno) {
        chamar(Net.HttpMethods.GET, "/api/partidas/minhas?pagina=0&tamanho=10", null, sessao,
                JsonDoServidor::lerHistorico, retorno);
    }

    @Override public void acordar() {
        chamar(Net.HttpMethods.GET, "/api/saude", null, null, corpo -> corpo, resposta -> { });
    }

    private <T> void chamar(String metodo, String rota, String corpo, Sessao sessao,
                            Function<String, T> conversor, Retorno<T> retorno) {
        var pedido = new HttpRequestBuilder().newRequest().method(metodo).url(base + rota)
                .timeout(TEMPO_LIMITE_MS).header("Accept", "application/json");
        if (corpo != null) pedido.header("Content-Type", "application/json").content(corpo);
        if (sessao != null) pedido.header("Authorization", "Bearer " + sessao.token());
        // A chamada não bloqueia: o código seguinte roda antes da resposta chegar.
        Gdx.net.sendHttpRequest(pedido.build(), new Net.HttpResponseListener() {
            @Override public void handleHttpResponse(Net.HttpResponse resposta) {
                entregar(retorno, interpretar(resposta.getStatus().getStatusCode(), resposta.getResultAsString(),
                        sessao != null, conversor));
            }
            @Override public void failed(Throwable erro) { entregar(retorno, Resposta.erro(SEM_CONEXAO)); }
            @Override public void cancelled() { entregar(retorno, Resposta.erro(SEM_CONEXAO)); }
        });
    }

    /** Tradução pura do HTTP para Resposta; testável sem rede. */
    static <T> Resposta<T> interpretar(int status, String corpo, boolean rotaProtegida, Function<String, T> conversor) {
        if (status >= 200 && status < 300) {
            try {
                return Resposta.sucesso(conversor.apply(corpo));
            } catch (RuntimeException invalida) {
                return Resposta.erro("Resposta inválida do servidor.");
            }
        }
        if (status == 401 && rotaProtegida) return Resposta.naoAutorizado(SESSAO_EXPIRADA);
        return Resposta.erro(JsonDoServidor.mensagemDeErro(status, corpo));
    }

    private static <T> void entregar(Retorno<T> retorno, Resposta<T> resposta) {
        Gdx.app.postRunnable(() -> retorno.receber(resposta));
    }
}
