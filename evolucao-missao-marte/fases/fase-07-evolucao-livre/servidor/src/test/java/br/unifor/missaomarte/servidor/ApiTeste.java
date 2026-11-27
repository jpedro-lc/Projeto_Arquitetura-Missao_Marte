package br.unifor.missaomarte.servidor;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import com.jayway.jsonpath.JsonPath;

/** Base dos testes de API: H2 em memória e segredo fixo (src/test/resources); cada teste é desfeito. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class ApiTeste {
    protected static final String SENHA = "senha-segura-1";

    @Autowired
    protected MockMvc mvc;

    protected ResultActions postar(String rota, String json) throws Exception {
        return mvc.perform(post(rota).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    protected ResultActions postar(String rota, String json, String token) throws Exception {
        return mvc.perform(post(rota).contentType(MediaType.APPLICATION_JSON).content(json)
                .header("Authorization", "Bearer " + token));
    }

    protected ResultActions cadastrar(String nome, String senha) throws Exception {
        return postar("/api/usuarios", "{\"nome\":\"" + nome + "\",\"senha\":\"" + senha + "\"}");
    }

    protected ResultActions login(String nome, String senha) throws Exception {
        return postar("/api/auth/login", "{\"nome\":\"" + nome + "\",\"senha\":\"" + senha + "\"}");
    }

    /** Cadastra (se preciso) e devolve um token válido. */
    protected String token(String nome) throws Exception {
        cadastrar(nome, SENHA);
        String corpo = login(nome, SENHA).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(corpo, "$.token");
    }

    protected static String partida(String resultado, int pontuacao, long duracao) {
        return "{\"dificuldade\":\"MEDIO\",\"resultado\":\"" + resultado + "\",\"pontuacao\":" + pontuacao
                + ",\"duracaoSegundos\":" + duracao + ",\"movimentos\":12}";
    }

    protected void registrar(String token, String resultado, int pontuacao, long duracao) throws Exception {
        postar("/api/partidas", partida(resultado, pontuacao, duracao), token).andExpect(status().isCreated());
    }
}
