package br.unifor.missaomarte.remoto;

/** Guarda a sessão do modo logado; sem sessão, o jogo está no modo convidado. */
public final class SessaoAtual {
    private Sessao sessao;

    public Sessao sessao() { return sessao; }
    public boolean logado() { return sessao != null; }
    public void entrar(Sessao nova) { sessao = nova; }
    public void encerrar() { sessao = null; }
}
