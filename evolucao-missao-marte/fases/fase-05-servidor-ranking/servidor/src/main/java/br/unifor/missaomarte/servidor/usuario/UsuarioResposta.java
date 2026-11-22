package br.unifor.missaomarte.servidor.usuario;

/** O que a API revela de uma conta: nunca senha nem hash. */
public record UsuarioResposta(Long id, String nome) {
    static UsuarioResposta de(Usuario usuario) { return new UsuarioResposta(usuario.getId(), usuario.getNome()); }
}
