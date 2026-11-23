package br.unifor.missaomarte.screen;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import br.unifor.missaomarte.MissaoMarteGame;
import br.unifor.missaomarte.presentation.EstiloInterface;
import br.unifor.missaomarte.presentation.FormularioLogin;
import br.unifor.missaomarte.presentation.FormularioLogin.Campo;
import br.unifor.missaomarte.presentation.FormularioLogin.Modo;
import br.unifor.missaomarte.presentation.assets.RecursosJogo;
import br.unifor.missaomarte.remoto.Resposta;
import br.unifor.missaomarte.remoto.Sessao;

/**
 * Entrar ou criar conta. Tab/setas trocam o campo, F2 alterna o modo, Enter confirma, Esc volta.
 * As regras do formulário ficam em FormularioLogin (sem LibGDX).
 */
public final class LoginScreen extends TelaPainel {
    private final FormularioLogin formulario = new FormularioLogin();
    private final Label titulo;
    private final Label nome;
    private final Label senha;
    private final Label status;
    private boolean aguardando;
    private boolean ativa = true;

    public LoginScreen(MissaoMarteGame game, RecursosJogo recursos) {
        super(game, recursos, 900);
        titulo = estilo.valor("");
        titulo.setName("login-titulo");
        conteudo.add(titulo).padBottom(24).row();
        nome = campo("NOME", Campo.NOME, "login-nome");
        senha = campo("SENHA", Campo.SENHA, "login-senha");
        status = aviso(" ", EstiloInterface.LARANJA);
        status.setName("login-status");
        conteudo.add(status).padTop(8).padBottom(14).row();
        var botoes = new Table();
        botoes.add(botao("CONFIRMAR", this::confirmar)).size(220, 54).padRight(10);
        botoes.add(botao("ALTERNAR (F2)", this::alternar)).size(220, 54).padRight(10);
        botoes.add(botao("VOLTAR", () -> game.mostrarMenu())).size(220, 54);
        conteudo.add(botoes).padBottom(14).row();
        var dica = estilo.texto("Tab troca o campo  •  F2 alterna entrar/criar  •  Enter confirma  •  Esc volta");
        dica.setColor(0.6f, 0.82f, 0.88f, 1);
        conteudo.add(dica);
        stage.addListener(new InputListener() {
            @Override public boolean keyTyped(InputEvent event, char c) { return digitar(c); }
            @Override public boolean keyDown(InputEvent event, int tecla) { return tecla(tecla); }
        });
        atualizar();
    }

    private Label campo(String rotulo, Campo qual, String nomeAtor) {
        var linha = new Table();
        linha.add(estilo.texto(rotulo)).width(110).left();
        var caixa = new Table();
        caixa.setBackground(recursos.botao(RecursosJogo.BOTAO_NORMAL));
        caixa.pad(8, 16, 8, 16);
        var valor = estilo.valor("");
        valor.setName(nomeAtor);
        caixa.add(valor).growX().left();
        caixa.setTouchable(Touchable.enabled);
        caixa.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (formulario.campo() != qual) formulario.trocarCampo();
                atualizar();
            }
        });
        linha.add(caixa).width(460).height(56);
        conteudo.add(linha).padBottom(12).row();
        return valor;
    }

    /** Também usado pelo smoke gráfico para simular a digitação. */
    public boolean digitar(char c) {
        if (aguardando) return true;
        if (c == '\b') formulario.apagar();
        else if (c == '\t') formulario.trocarCampo();
        else if (c == '\r' || c == '\n') confirmar();
        else if (c >= 32) formulario.digitar(c);
        else return false;
        atualizar();
        return true;
    }

    public boolean tecla(int tecla) {
        if (aguardando) return true;
        switch (tecla) {
            case Keys.F2 -> alternar();
            case Keys.UP, Keys.DOWN -> { formulario.trocarCampo(); atualizar(); }
            case Keys.ESCAPE -> game.mostrarMenu();
            default -> { return false; }
        }
        return true;
    }

    private void alternar() {
        formulario.alternarModo();
        status.setText(" ");
        atualizar();
    }

    private void confirmar() {
        if (aguardando) return;
        var problema = formulario.validar();
        if (problema.isPresent()) { mostrar(problema.get(), EstiloInterface.LARANJA); return; }
        aguardando = true;
        String n = formulario.nome();
        String s = formulario.senha();
        if (formulario.modo() == Modo.CRIAR_CONTA) {
            mostrar("Criando conta...", EstiloInterface.CIANO);
            game.servidor().cadastrar(n, s, criada -> {
                if (!ativa) return;
                if (!criada.ok()) { falhou(criada); return; }
                mostrar("Conta criada. Entrando...", EstiloInterface.CIANO);
                game.servidor().entrar(n, s, this::aoEntrar);
            });
        } else {
            mostrar("Entrando...", EstiloInterface.CIANO);
            game.servidor().entrar(n, s, this::aoEntrar);
        }
    }

    private void aoEntrar(Resposta<Sessao> resposta) {
        if (!ativa) return;
        if (resposta.ok()) game.entrar(resposta.valor());
        else falhou(resposta);
    }

    private void falhou(Resposta<?> resposta) {
        aguardando = false;
        mostrar(resposta.erro(), EstiloInterface.LARANJA);
    }

    private void mostrar(String texto, Color cor) {
        status.setText(texto);
        status.setColor(cor);
    }

    private void atualizar() {
        titulo.setText(formulario.modo() == Modo.ENTRAR ? "ENTRAR NA CONTA" : "CRIAR CONTA");
        boolean noNome = formulario.campo() == Campo.NOME;
        nome.setText(formulario.nome() + (noNome ? "_" : ""));
        senha.setText(formulario.senhaMascarada() + (noNome ? "" : "_"));
        nome.setColor(noNome ? Color.WHITE : Color.GRAY);
        senha.setColor(noNome ? Color.GRAY : Color.WHITE);
    }

    public FormularioLogin formulario() { return formulario; }
    @Override public void hide() { ativa = false; super.hide(); }
}
