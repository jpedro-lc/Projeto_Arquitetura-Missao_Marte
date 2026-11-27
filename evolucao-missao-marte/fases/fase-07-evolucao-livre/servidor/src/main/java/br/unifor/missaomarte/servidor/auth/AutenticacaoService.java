package br.unifor.missaomarte.servidor.auth;

import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import br.unifor.missaomarte.servidor.erro.CredenciaisInvalidasException;
import br.unifor.missaomarte.servidor.seguranca.TokenService;
import br.unifor.missaomarte.servidor.usuario.Usuario;
import br.unifor.missaomarte.servidor.usuario.UsuarioRepository;

@Service
public class AutenticacaoService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder senhas;
    private final TokenService tokens;
    // Comparado quando o nome não existe: o tempo de resposta não revela quais contas existem.
    private final String hashFalso;

    public AutenticacaoService(UsuarioRepository usuarios, PasswordEncoder senhas, TokenService tokens) {
        this.usuarios = usuarios;
        this.senhas = senhas;
        this.tokens = tokens;
        this.hashFalso = senhas.encode(UUID.randomUUID().toString());
    }

    public TokenResposta entrar(LoginRequisicao requisicao) {
        var usuario = usuarios.findByNomeNormalizado(Usuario.normalizar(requisicao.nome()));
        if (usuario.isEmpty()) {
            senhas.matches(requisicao.senha(), hashFalso);
            throw new CredenciaisInvalidasException();
        }
        if (!senhas.matches(requisicao.senha(), usuario.get().getSenhaHash())) throw new CredenciaisInvalidasException();
        return tokens.emitir(usuario.get());
    }
}
