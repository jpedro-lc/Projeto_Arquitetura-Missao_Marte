package br.unifor.missaomarte.presentation.rede;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import br.unifor.missaomarte.model.Dificuldade;
import br.unifor.missaomarte.remoto.LinhaRanking;
import br.unifor.missaomarte.remoto.PartidaConcluida;
import br.unifor.missaomarte.remoto.PartidaRegistrada;

/**
 * Só converte JSON (sem rede nem OpenGL). O texto enviado é montado pelo JsonWriter,
 * que escapa aspas e barras: uma senha como a"b\c não quebra a estrutura.
 */
public final class JsonDoServidor {
    private JsonDoServidor() { }

    public static String credenciais(String nome, String senha) {
        return escrever(w -> w.set("nome", nome).set("senha", senha));
    }

    public static String partida(PartidaConcluida p) {
        return escrever(w -> w.set("dificuldade", p.dificuldade().name()).set("resultado", p.resultado().name())
                .set("pontuacao", p.pontuacao()).set("duracaoSegundos", p.duracaoSegundos()).set("movimentos", p.movimentos()));
    }

    public static String lerToken(String corpo) {
        String token = ler(corpo).getString("token", null);
        if (token == null || token.isBlank()) throw new IllegalArgumentException("Resposta sem token");
        return token;
    }

    public static String lerNome(String corpo) { return ler(corpo).getString("nome"); }

    public static PartidaRegistrada lerPartida(String corpo) { return partida(ler(corpo)); }

    public static List<PartidaRegistrada> lerHistorico(String corpo) {
        var lista = new ArrayList<PartidaRegistrada>();
        for (JsonValue v = lista(corpo).child; v != null; v = v.next) lista.add(partida(v));
        return lista;
    }

    public static List<LinhaRanking> lerRanking(String corpo) {
        var lista = new ArrayList<LinhaRanking>();
        for (JsonValue v = lista(corpo).child; v != null; v = v.next) {
            lista.add(new LinhaRanking(v.getInt("posicao"), v.getString("nome"), v.getInt("pontuacao"),
                    Dificuldade.valueOf(v.getString("dificuldade")), v.getLong("duracaoSegundos")));
        }
        return lista;
    }

    /** Mensagem para o jogador: 1º erro de campo, ou "detail" do ProblemDetail, ou texto por código. Nunca o corpo bruto. */
    public static String mensagemDeErro(int status, String corpo) {
        try {
            JsonValue raiz = new JsonReader().parse(corpo);
            if (raiz != null && raiz.isObject()) {
                JsonValue erros = raiz.get("erros");
                if (erros != null && erros.isArray() && erros.child != null) {
                    String mensagem = erros.child.getString("mensagem", null);
                    if (mensagem != null && !mensagem.isBlank()) return mensagem;
                }
                String detalhe = raiz.getString("detail", null);
                if (detalhe != null && !detalhe.isBlank()) return detalhe;
            }
        } catch (RuntimeException naoEJson) {
            // Corpo ausente ou que não é JSON: cai no texto padrão.
        }
        if (status == 401) return "Não autorizado. Entre novamente.";
        if (status == 409) return "Este nome já está em uso.";
        if (status == 400) return "Dados inválidos.";
        if (status >= 500) return "O servidor encontrou um erro. Tente mais tarde.";
        return "Erro inesperado do servidor (código " + status + ").";
    }

    private static PartidaRegistrada partida(JsonValue v) {
        return new PartidaRegistrada(v.getLong("id"), Dificuldade.valueOf(v.getString("dificuldade")),
                PartidaConcluida.Resultado.valueOf(v.getString("resultado")), v.getInt("pontuacao"),
                v.getLong("duracaoSegundos"), v.getInt("movimentos"), Instant.parse(v.getString("registradaEm")));
    }

    private static JsonValue ler(String corpo) {
        JsonValue raiz = new JsonReader().parse(corpo);
        if (raiz == null || !raiz.isObject()) throw new IllegalArgumentException("JSON não é um objeto");
        return raiz;
    }

    private static JsonValue lista(String corpo) {
        JsonValue raiz = new JsonReader().parse(corpo);
        if (raiz == null || !raiz.isArray()) throw new IllegalArgumentException("JSON não é uma lista");
        return raiz;
    }

    private interface Campos { void escrever(JsonWriter w) throws IOException; }

    private static String escrever(Campos campos) {
        var texto = new StringWriter();
        try (var w = new JsonWriter(texto)) {
            w.setOutputType(JsonWriter.OutputType.json);
            w.object();
            campos.escrever(w);
            w.pop();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return texto.toString();
    }
}
