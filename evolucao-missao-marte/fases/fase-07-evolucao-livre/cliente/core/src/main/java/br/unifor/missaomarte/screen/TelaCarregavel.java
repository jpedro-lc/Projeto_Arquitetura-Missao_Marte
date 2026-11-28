package br.unifor.missaomarte.screen;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import br.unifor.missaomarte.MissaoMarteGame;
import br.unifor.missaomarte.presentation.EstiloInterface;
import br.unifor.missaomarte.presentation.assets.RecursosJogo;
import br.unifor.missaomarte.remoto.Resposta;

/** Tela com estados visíveis (State): carregando, pronto e erro. R recarrega após erro; Enter/Esc voltam. */
public abstract class TelaCarregavel extends TelaPainel {
    public enum Estado { CARREGANDO, PRONTO, ERRO }
    private final Table corpo = new Table();
    /** Fica entre o título e o conteúdo e não some ao recarregar (abas do ranking). */
    protected final Table cabecalho = new Table();
    private Estado estado = Estado.CARREGANDO;
    private boolean ativa = true;

    protected TelaCarregavel(MissaoMarteGame game, RecursosJogo recursos, String titulo) {
        super(game, recursos, 960);
        conteudo.add(estilo.valor(titulo)).padBottom(20).row();
        conteudo.add(cabecalho).row();
        conteudo.add(corpo).minHeight(380).row();
        var botoes = new Table();
        botoes.add(botao("RECARREGAR", this::carregar)).size(240, 52).padRight(12);
        botoes.add(botao("VOLTAR", () -> game.mostrarMenu())).size(240, 52);
        conteudo.add(botoes).padTop(16);
        stage.addListener(new InputListener() {
            @Override public boolean keyDown(InputEvent event, int tecla) {
                if (tecla == Keys.R && estado == Estado.ERRO) carregar();
                else if (tecla == Keys.ENTER || tecla == Keys.ESCAPE || tecla == Keys.BACK) game.mostrarMenu();
                else return false;
                return true;
            }
        });
    }

    /** Subclasses chamam no fim do construtor e ao recarregar. */
    protected final void carregar() {
        estado = Estado.CARREGANDO;
        mostrar(aviso("Carregando...", EstiloInterface.CIANO));
        buscar();
    }

    protected abstract void buscar();

    /** Trata a resposta assíncrona: 401 descarta a sessão; erro mostra o motivo e "R tenta de novo". */
    protected final <T> boolean aceitar(Resposta<T> resposta) {
        if (!ativa) return false;
        if (resposta.ok()) { estado = Estado.PRONTO; return true; }
        if (resposta.naoAutorizado()) {
            game.sessaoExpirada();
            game.mostrarMenu(resposta.erro());
            return false;
        }
        estado = Estado.ERRO;
        var erro = new Table();
        erro.add(aviso(resposta.erro(), EstiloInterface.LARANJA)).padBottom(10).row();
        erro.add(estilo.texto("Pressione R ou RECARREGAR para tentar de novo."));
        mostrar(erro);
        return false;
    }

    protected final void mostrar(com.badlogic.gdx.scenes.scene2d.Actor ator) {
        corpo.clearChildren();
        corpo.add(ator);
    }

    protected final void pronto() { estado = Estado.PRONTO; }
    public Estado estado() { return estado; }
    @Override public void hide() { ativa = false; super.hide(); }
}
