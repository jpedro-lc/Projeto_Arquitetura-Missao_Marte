package br.unifor.missaomarte.presentation;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import br.unifor.missaomarte.model.EstadoPartida;
import br.unifor.missaomarte.presentation.assets.RecursosJogo;

/** HUD SpaceRage: painel superior contínuo e inferior com ícones; valores sempre do estado. */
public final class HudPartida extends Table {
    private final Label pontos;
    private final Label dificuldade;
    private final Label movimentos;
    private final Label tempo;
    private final Label passageiros;
    /** Até 5 corações (o máximo do modo Infinito); os vazios mostram as vidas perdidas. */
    private final Image[] vidas = new Image[HudDados.CORACOES_MAXIMOS];
    private final TextureRegionDrawable cheio;
    private final TextureRegionDrawable vazio;
    private int picoDeVidas;

    public HudPartida(RecursosJogo recursos) {
        var estilo = new EstiloInterface(recursos);
        cheio = new TextureRegionDrawable(new TextureRegion(recursos.coracao(true)));
        vazio = new TextureRegionDrawable(new TextureRegion(recursos.coracao(false)));
        pontos = valor(estilo, "hud-pontos");
        dificuldade = valor(estilo, "hud-dificuldade");
        movimentos = valor(estilo, "hud-movimentos");
        tempo = valor(estilo, "hud-tempo");
        passageiros = valor(estilo, "hud-passageiros");

        var superior = new Table();
        superior.setBackground(recursos.painelHud());
        superior.pad(8, 34, 8, 34);
        String[] nomes = {"PONTOS", "DIFICULDADE", "MOVIMENTOS", "TEMPO"};
        Label[] valores = {pontos, dificuldade, movimentos, tempo};
        for (int i = 0; i < nomes.length; i++) {
            superior.add(estilo.texto(nomes[i])).right().padRight(14);
            superior.add(valores[i]).left().expandX();
        }
        add(superior).growX().height(64).row();

        var inferior = new Table();
        inferior.setBackground(recursos.painelHudInferior());
        inferior.pad(6, 34, 6, 34);
        inferior.add(estilo.texto("VIDAS")).padRight(16);
        for (int i = 0; i < vidas.length; i++) {
            vidas[i] = new Image(cheio);
            vidas[i].setName("hud-vida-" + (i + 1));
            vidas[i].setScaling(Scaling.fit);
            inferior.add(vidas[i]).size(30, 28).padRight(6);
        }
        inferior.add().expandX();
        var icone = new Image(new TextureRegion(recursos.textura(RecursosJogo.astronauta(1))));
        icone.setScaling(Scaling.fit);
        inferior.add(icone).size(26, 32).padRight(10);
        inferior.add(estilo.texto("PASSAGEIROS")).padRight(14);
        inferior.add(passageiros).minWidth(70);
        add(inferior).width(860).height(56).padTop(4);
    }

    private static Label valor(EstiloInterface estilo, String nome) {
        var label = estilo.valor("");
        label.getStyle().fontColor = EstiloInterface.LARANJA;
        label.setName(nome);
        return label;
    }

    public void atualizar(EstadoPartida estado) {
        var dados = HudDados.de(estado);
        pontos.setText(dados.pontos());
        dificuldade.setText(dados.dificuldade());
        movimentos.setText(dados.movimentos());
        tempo.setText(dados.tempo());
        passageiros.setText(dados.resgatados() + "/" + dados.total());
        picoDeVidas = Math.max(picoDeVidas, dados.vidas());
        int espacos = HudDados.espacosDeVida(dados.vidas(), picoDeVidas);
        for (int i = 0; i < vidas.length; i++) {
            vidas[i].setVisible(i < espacos);
            vidas[i].setDrawable(i < dados.vidas() ? cheio : vazio);
        }
    }

    /** Nova partida na mesma tela: esquece o pico de vidas da anterior. */
    public void reiniciar() { picoDeVidas = 0; }

    /** Para as verificações com janela: quantos corações estão cheios e quantos aparecem. */
    public int coracoesCheios() {
        int n = 0;
        for (Image v : vidas) if (v.isVisible() && v.getDrawable() == cheio) n++;
        return n;
    }

    public int coracoesVisiveis() {
        int n = 0;
        for (Image v : vidas) if (v.isVisible()) n++;
        return n;
    }
}
