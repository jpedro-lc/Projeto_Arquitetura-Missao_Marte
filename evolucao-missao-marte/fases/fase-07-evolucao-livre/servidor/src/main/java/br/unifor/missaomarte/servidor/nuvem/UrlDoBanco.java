package br.unifor.missaomarte.servidor.nuvem;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converte a URL de banco dos provedores (postgres://usuario:senha@host:porta/banco) nas propriedades
 * spring.datasource.*, que exigem o formato JDBC. Sem dependência do Spring, para ser testada sozinha.
 */
public final class UrlDoBanco {
    private UrlDoBanco() { }

    public static Map<String, String> paraSpring(String url) {
        if (url == null || url.isBlank()) return Map.of();
        String limpa = url.trim();
        if (limpa.startsWith("jdbc:")) return Map.of("spring.datasource.url", limpa);
        URI uri = URI.create(limpa);
        String esquema = uri.getScheme();
        if (!"postgres".equals(esquema) && !"postgresql".equals(esquema)) {
            throw new IllegalArgumentException("DATABASE_URL deve começar com postgres:// ou postgresql://");
        }
        if (uri.getHost() == null || uri.getPath() == null || uri.getPath().length() <= 1) {
            throw new IllegalArgumentException("DATABASE_URL sem host ou nome do banco");
        }
        int porta = uri.getPort() == -1 ? 5432 : uri.getPort();
        String jdbc = "jdbc:postgresql://" + uri.getHost() + ":" + porta + uri.getPath()
                + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());
        var propriedades = new LinkedHashMap<String, String>();
        propriedades.put("spring.datasource.url", jdbc);
        String credenciais = uri.getRawUserInfo();
        if (credenciais != null) {
            int doisPontos = credenciais.indexOf(':');
            String usuario = doisPontos < 0 ? credenciais : credenciais.substring(0, doisPontos);
            propriedades.put("spring.datasource.username", decodificar(usuario));
            if (doisPontos >= 0) propriedades.put("spring.datasource.password", decodificar(credenciais.substring(doisPontos + 1)));
        }
        return propriedades;
    }

    private static String decodificar(String texto) {
        return URLDecoder.decode(texto, StandardCharsets.UTF_8);
    }
}
