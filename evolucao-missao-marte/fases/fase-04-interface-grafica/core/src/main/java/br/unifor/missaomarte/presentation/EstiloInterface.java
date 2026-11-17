package br.unifor.missaomarte.presentation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import br.unifor.missaomarte.presentation.assets.RecursosJogo;

/** Estilos comuns; botões usam os estados normal/hover/pressed do pacote SpaceRage. */
public final class EstiloInterface {
    public static final Color CIANO = new Color(0.22f, 0.95f, 1, 1);
    public static final Color LARANJA = new Color(1, 0.58f, 0.18f, 1);
    private final RecursosJogo recursos;
    public EstiloInterface(RecursosJogo recursos) { this.recursos = recursos; }
    public Label texto(String texto) { return new Label(texto, new Label.LabelStyle(recursos.pequena(), Color.WHITE)); }
    public Label valor(String texto) { return new Label(texto, new Label.LabelStyle(recursos.media(), CIANO)); }
    public Label titulo(String texto) { return new Label(texto, new Label.LabelStyle(recursos.titulo(), Color.WHITE)); }
    public TextButton botao(String texto, Runnable acao) {
        var estilo = new TextButton.TextButtonStyle();
        estilo.font = recursos.pequena();
        estilo.fontColor = Color.WHITE;
        estilo.overFontColor = CIANO;
        estilo.downFontColor = LARANJA;
        estilo.up = recursos.botao(RecursosJogo.BOTAO_NORMAL);
        estilo.over = recursos.botao(RecursosJogo.BOTAO_SOBRE);
        estilo.down = recursos.botao(RecursosJogo.BOTAO_PRESSIONADO);
        var botao = new TextButton(texto, estilo);
        botao.setName(texto);
        botao.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { acao.run(); }
        });
        return botao;
    }
}
