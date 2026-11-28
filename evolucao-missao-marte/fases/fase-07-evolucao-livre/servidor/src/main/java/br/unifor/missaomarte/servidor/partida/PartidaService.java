package br.unifor.missaomarte.servidor.partida;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.unifor.missaomarte.servidor.usuario.UsuarioRepository;

@Service
public class PartidaService {
    private final PartidaRepository partidas;
    private final UsuarioRepository usuarios;

    public PartidaService(PartidaRepository partidas, UsuarioRepository usuarios) {
        this.partidas = partidas;
        this.usuarios = usuarios;
    }

    @Transactional
    public PartidaResposta registrar(Long usuarioId, PartidaRequisicao r) {
        var usuario = usuarios.getReferenceById(usuarioId);
        var partida = partidas.save(new Partida(usuario, r.dificuldade(), r.resultado(), r.pontuacao(),
                r.duracaoSegundos(), r.movimentos(), r.modoOuClassico(), r.setor()));
        return PartidaResposta.de(partida);
    }

    @Transactional(readOnly = true)
    public List<PartidaResposta> historico(Long usuarioId, int pagina, int tamanho) {
        return partidas.findByUsuarioIdOrderByRegistradaEmDescIdDesc(usuarioId, PageRequest.of(pagina, tamanho))
                .stream().map(PartidaResposta::de).toList();
    }
}
