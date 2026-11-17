package br.unifor.missaomarte.presentation;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Scaling;
import br.unifor.missaomarte.model.EstadoPartida;
import br.unifor.missaomarte.presentation.assets.RecursosJogo;

/** HUD de duas linhas com valores reais; não depende das dimensões físicas da janela. */
public final class HudPartida extends Table {
    private final Label pontos;
    private final Label dificuldade;
    private final Label movimentos;
    private final Label tempo;
    private final Label passageiros;
    private final Image[] vidas = new Image[3];

    public HudPartida(RecursosJogo recursos) {
        var estilo = new EstiloInterface(recursos);
        pontos = estilo.valor("");
        dificuldade = estilo.valor("");
        movimentos = estilo.valor("");
        tempo = estilo.valor("");
        passageiros = estilo.valor("");
        pontos.setName("hud-pontos");
        dificuldade.setName("hud-dificuldade");
        movimentos.setName("hud-movimentos");
        tempo.setName("hud-tempo");
        passageiros.setName("hud-passageiros");
        var linha = new Table();
        String[] nomes = {"PONTOS", "DIFICULDADE", "MOVIMENTOS", "TEMPO"};
        Label[] valores = {pontos, dificuldade, movimentos, tempo};
        for (int i = 0; i < nomes.length; i++) {
            Table celula = new Table();
            celula.setBackground(recursos.painel());
            celula.pad(14, 30, 12, 30);
            celula.add(estilo.texto(nomes[i])).left().row();
            celula.add(valores[i]).left();
            linha.add(celula).uniformX().expandX().fillX().padRight(i == 3 ? 0 : 6);
        }
        add(linha).growX().row();
        var segunda = new Table();
        segunda.pad(7, 26, 4, 26);
        segunda.add(estilo.texto("VIDAS")).padRight(18);
        for (int i = 0; i < vidas.length; i++) {
            vidas[i] = new Image(recursos.textura(RecursosJogo.NAVE));
            vidas[i].setScaling(Scaling.fit);
            segunda.add(vidas[i]).size(36, 36).padRight(8);
        }
        segunda.add().expandX();
        segunda.add(estilo.texto("PASSAGEIROS")).padRight(18);
        segunda.add(passageiros).minWidth(90);
        add(segunda).growX();
    }

    public void atualizar(EstadoPartida estado) {
        var dados = HudDados.de(estado);
        pontos.setText(dados.pontos());
        dificuldade.setText(dados.dificuldade());
        movimentos.setText(dados.movimentos());
        tempo.setText(dados.tempo());
        passageiros.setText(dados.resgatados() + "/" + dados.total());
        for (int i = 0; i < vidas.length; i++) vidas[i].setVisible(i < dados.vidas());
    }
}
