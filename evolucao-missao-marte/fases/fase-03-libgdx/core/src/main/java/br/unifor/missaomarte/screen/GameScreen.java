package br.unifor.missaomarte.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import br.unifor.missaomarte.MissaoMarteGame;
import br.unifor.missaomarte.model.EstadoPartida.Situacao;
import br.unifor.missaomarte.presentation.ControladorPartida;
import br.unifor.missaomarte.presentation.EfeitosPartida;
import br.unifor.missaomarte.presentation.EstiloInterface;
import br.unifor.missaomarte.presentation.HudPartida;
import br.unifor.missaomarte.presentation.assets.RecursosJogo;
import br.unifor.missaomarte.presentation.rendering.PartidaRenderer;

/** HUD/ambiente/rodapé, com pausa sobre a mesma partida. */
public final class GameScreen extends ScreenAdapter {
    private final MissaoMarteGame game;
    private final ControladorPartida controlador;
    private final Stage stage = new Stage(new FitViewport(PartidaRenderer.LARGURA, PartidaRenderer.ALTURA));
    private final EfeitosPartida efeitos = new EfeitosPartida();
    private final Table overlay = new Table();
    private final Table resultado = new Table();
    private final Label tituloResultado;
    private final HudPartida hud;
    private boolean pausado;
    private float tempoVisual;
    private int geracao;

    public GameScreen(MissaoMarteGame game, ControladorPartida controlador, RecursosJogo recursos) {
        this.game = game;
        this.controlador = controlador;
        geracao = controlador.geracao();
        var estilo = new EstiloInterface(recursos);
        var renderer = new PartidaRenderer(recursos);
        var raiz = new Table();
        raiz.setFillParent(true);
        raiz.pad(10, 14, 10, 14);
        hud = new HudPartida(recursos);
        hud.atualizar(controlador.estado());
        raiz.add(hud).growX().height(139).row();
        Actor ambiente = new Actor() {
            @Override public void draw(Batch batch, float parentAlpha) {
                renderer.desenhar(batch, controlador.estado(), efeitos, getX(), getY(), getWidth(), getHeight(), tempoVisual);
            }
        };
        raiz.add(ambiente).grow().pad(4, 0, 6, 0).row();
        var rodape = new Table();
        rodape.setBackground(recursos.painel());
        rodape.pad(17, 24, 12, 24);
        var controles = new Table();
        controles.add(controle(recursos, 45, 278, "— MOVER", true)).expandX();
        controles.add(controle(recursos, 385, 178, "— TIRO", false)).expandX();
        controles.add(controle(recursos, 630, 94, "— PAUSA", true)).expandX();
        controles.add(controle(recursos, 805, 126, "— MENU", true)).expandX();
        rodape.add(controles).growX().row();
        var ajuda = estilo.texto("C EMBARCAR  •  Q ABORTAR  •  R REINICIAR APÓS O FIM  |  TIRO INDISPONÍVEL");
        ajuda.setColor(0.5f, 0.75f, 0.8f, 1);
        rodape.add(ajuda).padTop(12);
        raiz.add(rodape).growX().height(92);
        stage.addActor(raiz);

        resultado.setBackground(recursos.painel());
        resultado.setSize(700, 118);
        resultado.setPosition((PartidaRenderer.LARGURA - 700) / 2, 110);
        resultado.pad(14, 28, 14, 28);
        tituloResultado = estilo.valor("");
        tituloResultado.setAlignment(Align.center);
        resultado.add(tituloResultado).colspan(2).growX().padBottom(8).row();
        resultado.add(estilo.botao("REINICIAR", () -> { game.audio().selecionar(); game.iniciarPartida(); })).size(275, 48).padRight(12);
        resultado.add(estilo.botao("VOLTAR AO MENU", () -> { game.audio().selecionar(); game.mostrarMenu(); })).size(275, 48);
        resultado.setVisible(false);
        stage.addActor(resultado);

        overlay.setFillParent(true);
        overlay.setBackground(new TextureRegionDrawable(new TextureRegion(recursos.pixel())).tint(new Color(0, 0.015f, 0.035f, 0.86f)));
        var painel = new Table();
        painel.setBackground(recursos.painel());
        painel.pad(40, 70, 40, 70);
        painel.add(estilo.valor("MISSÃO EM PAUSA")).padBottom(16).row();
        painel.add(estilo.texto("A partida e o cronômetro estão congelados.")).padBottom(32).row();
        painel.add(estilo.botao("CONTINUAR", () -> { game.audio().selecionar(); game.continuarPartida(); })).size(360, 66).padBottom(12).row();
        painel.add(estilo.botao("REINICIAR PARTIDA", () -> { game.audio().selecionar(); game.iniciarPartida(); })).size(360, 66).padBottom(12).row();
        painel.add(estilo.botao("VOLTAR AO MENU", () -> { game.audio().selecionar(); game.mostrarMenu(); })).size(360, 66).padBottom(20).row();
        painel.add(estilo.texto("Voltar ao menu encerra a missão atual."));
        overlay.add(painel);
        overlay.setVisible(false);
        stage.addActor(overlay);
    }

    @Override public void show() { controlador.retomar(); Gdx.input.setInputProcessor(stage); }
    @Override public void render(float delta) {
        if (pausado) {
            if (game.entrada().pausaSolicitada() || game.entrada().ler().sair()) game.continuarPartida();
        } else {
            if (game.entrada().pausaSolicitada() || controlador.atualizar()) game.pausarPartida();
            if (!pausado) {
                if (geracao != controlador.geracao()) {
                    efeitos.limpar();
                    game.audio().iniciar();
                    geracao = controlador.geracao();
                }
                for (var novo : controlador.consumirResultados()) {
                    efeitos.receber(novo);
                    game.audio().processar(novo);
                }
                efeitos.atualizar(delta);
                tempoVisual += delta;
            }
        }
        hud.atualizar(controlador.estado());
        var situacao = controlador.estado().situacao();
        resultado.setVisible(!pausado && situacao != Situacao.EM_ANDAMENTO);
        tituloResultado.setText(switch (situacao) {
            case VITORIA -> "RESGATE CONCLUÍDO";
            case DERROTA -> "FIM DE MISSÃO";
            case ABORTADA -> "MISSÃO ABORTADA";
            default -> "";
        });
        ScreenUtils.clear(0.005f, 0.015f, 0.035f, 1);
        stage.act(Math.min(delta, 0.1f));
        stage.getViewport().apply();
        stage.draw();
    }

    public boolean pausado() { return pausado; }

    private static Table controle(RecursosJogo recursos, int x, int largura, String acao, boolean ativo) {
        var grupo = new Table();
        var imagem = new Image(new TextureRegion(recursos.textura(RecursosJogo.CONTROLES), x, 25, largura, 70));
        imagem.setScaling(Scaling.fit);
        var texto = new EstiloInterface(recursos).texto(acao);
        if (!ativo) {
            imagem.setColor(0.42f, 0.48f, 0.52f, 1);
            texto.setColor(0.42f, 0.48f, 0.52f, 1);
        }
        grupo.add(imagem).size(largura * 0.43f, 31).padRight(10);
        grupo.add(texto);
        return grupo;
    }
    public void exibirPausa(boolean mostrar) { pausado = mostrar; overlay.setVisible(mostrar); }
    public Stage stage() { return stage; }
    public int efeitosAtivos() { return efeitos.ativos().size(); }
    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() { game.pausarPartida(); }
    @Override public void hide() { if (Gdx.input.getInputProcessor() == stage) Gdx.input.setInputProcessor(null); }
    @Override public void dispose() { stage.dispose(); }
}
