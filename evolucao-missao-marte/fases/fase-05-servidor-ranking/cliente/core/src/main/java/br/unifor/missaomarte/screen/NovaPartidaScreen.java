package br.unifor.missaomarte.screen;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import br.unifor.missaomarte.MissaoMarteGame;
import br.unifor.missaomarte.model.Dificuldade;
import br.unifor.missaomarte.presentation.EstiloInterface;
import br.unifor.missaomarte.presentation.HudDados;
import br.unifor.missaomarte.presentation.assets.RecursosJogo;

/** Convidado digita o nome; logado usa o nome da conta. Setas mudam a dificuldade, Enter decola. */
public final class NovaPartidaScreen extends TelaPainel {
    static final int NOME_MAX = 20;
    private final StringBuilder nome = new StringBuilder();
    private final Label campoNome;
    private final TextButton[] opcoes = new TextButton[Dificuldade.values().length];
    private Dificuldade dificuldade = Dificuldade.MEDIO;

    public NovaPartidaScreen(MissaoMarteGame game, RecursosJogo recursos) {
        super(game, recursos, 760);
        conteudo.add(estilo.valor("NOVA PARTIDA")).padBottom(24).row();
        boolean logado = game.sessao().logado();
        campoNome = estilo.valor(logado ? game.sessao().sessao().nome() : "_");
        campoNome.setName("nova-nome");
        var linhaNome = new Table();
        linhaNome.add(estilo.texto(logado ? "PILOTO (CONTA)" : "SEU NOME")).width(190).left();
        var caixa = new Table();
        caixa.setBackground(recursos.botao(RecursosJogo.BOTAO_NORMAL));
        caixa.pad(8, 16, 8, 16);
        caixa.add(campoNome).growX().left();
        linhaNome.add(caixa).width(420).height(56);
        conteudo.add(linhaNome).padBottom(18).row();

        var linhaDificuldade = new Table();
        for (var d : Dificuldade.values()) {
            var botao = botao(HudDados.nomeDificuldade(d), () -> escolher(d));
            opcoes[d.ordinal()] = botao;
            linhaDificuldade.add(botao).size(190, 52).padRight(8);
        }
        conteudo.add(linhaDificuldade).padBottom(18).row();
        if (!logado) {
            var aviso = aviso("Modo convidado: a vitória vai para o ranking local.", EstiloInterface.CIANO);
            conteudo.add(aviso).padBottom(16).row();
        }
        var botoes = new Table();
        botoes.add(botao("DECOLAR", this::decolar)).size(260, 56).padRight(12);
        botoes.add(botao("VOLTAR", () -> game.mostrarMenu())).size(260, 56);
        conteudo.add(botoes).padBottom(12).row();
        var dica = estilo.texto("Setas mudam a dificuldade  •  Enter decola  •  Esc volta");
        dica.setColor(0.6f, 0.82f, 0.88f, 1);
        conteudo.add(dica);
        stage.addListener(new InputListener() {
            @Override public boolean keyTyped(InputEvent event, char c) { return digitar(c); }
            @Override public boolean keyDown(InputEvent event, int tecla) { return tecla(tecla); }
        });
        escolher(dificuldade);
    }

    private boolean digitar(char c) {
        if (c == '\r' || c == '\n') { decolar(); return true; }
        if (game.sessao().logado()) return false;
        if (c == '\b') { if (nome.length() > 0) nome.setLength(nome.length() - 1); }
        else if (nome.length() < NOME_MAX && (Character.isLetterOrDigit(c) || c == ' ' || c == '_' || c == '-')) nome.append(c);
        else return false;
        campoNome.setText(nome + "_");
        return true;
    }

    private boolean tecla(int tecla) {
        int i = dificuldade.ordinal();
        int n = Dificuldade.values().length;
        switch (tecla) {
            case Keys.LEFT -> escolher(Dificuldade.values()[(i + n - 1) % n]);
            case Keys.RIGHT -> escolher(Dificuldade.values()[(i + 1) % n]);
            case Keys.ESCAPE -> game.mostrarMenu();
            default -> { return false; }
        }
        return true;
    }

    private void escolher(Dificuldade nova) {
        dificuldade = nova;
        for (var d : Dificuldade.values()) {
            opcoes[d.ordinal()].getLabel().setColor(d == nova ? EstiloInterface.LARANJA : Color.WHITE);
        }
    }

    private void decolar() { game.iniciarPartida(nome.toString().trim(), dificuldade); }
}
