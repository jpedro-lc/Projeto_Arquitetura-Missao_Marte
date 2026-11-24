package br.unifor.missaomarte.servidor.usuario;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import br.unifor.missaomarte.servidor.erro.NomeJaExisteException;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder senhas;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder senhas) {
        this.usuarios = usuarios;
        this.senhas = senhas;
    }

    public Usuario cadastrar(CadastroRequisicao requisicao) {
        String nome = requisicao.nome().trim();
        if (usuarios.existsByNomeNormalizado(Usuario.normalizar(nome))) throw new NomeJaExisteException();
        try {
            return usuarios.saveAndFlush(new Usuario(nome, senhas.encode(requisicao.senha())));
        } catch (DataIntegrityViolationException corrida) {
            // Dois cadastros simultâneos passaram pela checagem: o índice único decide.
            throw new NomeJaExisteException();
        }
    }
}
