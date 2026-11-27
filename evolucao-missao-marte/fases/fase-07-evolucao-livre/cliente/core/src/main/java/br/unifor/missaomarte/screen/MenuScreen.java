package br.unifor.missaomarte.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Align;
import br.unifor.missaomarte.MissaoMarteGame;
import br.unifor.missaomarte.presentation.EstiloInterface;
import br.unifor.missaomarte.presentation.assets.RecursosJogo;

/** Menu com dois modos (State): convidado usa o ranking local; logado usa o servidor. */
public final class MenuScreen extends TelaPainel {
    private final String aviso;
    private boolean instrucoes;

    public MenuScreen(MissaoMarteGame game, RecursosJogo recursos, String aviso) {
        super(game, recursos, 850);
        this.aviso = aviso;
        principal();
    }

    public void principal() {
        instrucoes = false;
        conteudo.clearChildren();
        conteudo.add(estilo.titulo("MISSÃO MARTE")).padBottom(6).row();
        var modo = game.sessao().logado()
                ? estilo.valor("PILOTO: " + game.sessao().sessao().nome())
                : estilo.valor("MODO CONVIDADO");
        modo.getStyle().fontColor = EstiloInterface.LARANJA;
        modo.setName("modo");
        conteudo.add(modo).padBottom(aviso == null ? 26 : 8).row();
        if (aviso != null) conteudo.add(aviso(aviso, EstiloInterface.CIANO)).padBottom(18).row();
        adicionar("INICIAR JOGO", game::mostrarNovaPartida);
        if (game.sessao().logado()) {
            adicionar("RANKING GLOBAL", game::mostrarRanking);
            adicionar("MEU HISTÓRICO", game::mostrarHistorico);
            adicionar("SAIR DA CONTA", game::sairDaConta);
        } else {
            adicionar("RANKING LOCAL", game::mostrarRanking);
            adicionar("LIMPAR RANKING LOCAL", () -> {
                game.jogo().limparRanking();
                game.mostrarMenu("Ranking local apagado.");
            });
            adicionar("ENTRAR OU CRIAR CONTA", game::mostrarLogin);
        }
        adicionar("INSTRUÇÕES", this::instrucoes);
        adicionar("SAIR", () -> Gdx.app.exit());
    }

    private void adicionar(String texto, Runnable acao) {
        conteudo.add(botao(texto, acao)).size(380, 54).padBottom(10).row();
    }

    public void instrucoes() {
        instrucoes = true;
        conteudo.clearChildren();
        conteudo.add(estilo.valor("COMO JOGAR")).padBottom(28).row();
        var texto = estilo.texto("Resgate todos os passageiros e retorne à BASE (0,0).\n\n"
                + "WASD / D-Pad / Toque ou Deslize  —  mover uma posição\n"
                + "C / Botão EMBARCAR  —  embarcar o passageiro na posição\n"
                + "Q / Botão ABORTAR  —  abortar a missão\n"
                + "P, ESC ou Botão PAUSA  —  pausar e abrir as opções\n"
                + "R ou REINICIAR  —  reiniciar depois do encerramento\n\n"
                + "Cada movimento consome um ponto. Colisões reduzem vidas.\n"
                + "Convidado: ranking local. Com conta: vitórias e derrotas vão\n"
                + "para o servidor e contam no ranking global.\n"
                + "Professor +15  /  Engenheiro +20  /  Astronauta +10");
        texto.setAlignment(Align.left);
        texto.setWrap(true);
        texto.setColor(Color.WHITE);
        conteudo.add(texto).width(720).padBottom(28).row();
        conteudo.add(botao("VOLTAR", this::principal)).size(300, 64);
    }

    public boolean emInstrucoes() { return instrucoes; }
}
