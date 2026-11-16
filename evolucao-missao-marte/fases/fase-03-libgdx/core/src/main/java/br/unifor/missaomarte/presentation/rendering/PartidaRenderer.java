package br.unifor.missaomarte.presentation.rendering;

import java.util.List;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import br.unifor.missaomarte.model.EstadoPartida;
import br.unifor.missaomarte.model.EstadoPartida.Situacao;
import br.unifor.missaomarte.model.ResultadoAcao.Evento;
import br.unifor.missaomarte.presentation.assets.RecursosJogo;

/** Somente desenho: posições, colisões e pontuação já vieram do serviço. */
public final class PartidaRenderer {
    public static final float LARGURA = 1100;
    public static final float ALTURA = 760;
    private static final float MAPA_X = 32;
    private static final float MAPA_Y = 112;
    private static final float MAPA_TAMANHO = 584;

    private final SpriteBatch batch;
    private final BitmapFont fonte;
    private final RecursosJogo recursos;
    private final GlyphLayout medida = new GlyphLayout();

    public PartidaRenderer(SpriteBatch batch, BitmapFont fonte, RecursosJogo recursos) {
        this.batch = batch;
        this.fonte = fonte;
        this.recursos = recursos;
    }

    public void desenhar(EstadoPartida estado, List<Evento> eventos) {
        batch.begin();
        fundo();
        painel(0, 0, LARGURA, ALTURA, 0.015f, 0.025f, 0.065f, 0.58f);
        texto("MISSÃO MARTE", 32, 735, 1.6f, Color.CYAN);
        texto("RESGATE ESPACIAL  /  FASE 03", 680, 732, 1, Color.LIGHT_GRAY);
        mapa(estado);
        informacoes(estado, eventos);
        texto("BASE (0,0)  /  Resgate todos os passageiros e retorne.", 32, 83, 1, Color.WHITE);
        texto("P Professor  +15     E Engenheiro  +20     A Astronauta  +10", 32, 57, 1, Color.LIGHT_GRAY);
        texto("Uma tecla = uma ação. Sem comandos, a missão não avança turnos.", 32, 29, 1, Color.CYAN);
        batch.setColor(Color.WHITE);
        fonte.setColor(Color.WHITE);
        fonte.getData().setScale(1);
        batch.end();
    }

    private void fundo() {
        Texture imagem = recursos.textura(RecursosJogo.FUNDO);
        float aspectoImagem = (float) imagem.getWidth() / imagem.getHeight();
        float aspectoTela = LARGURA / ALTURA;
        float faixaU = Math.min(1, aspectoTela / aspectoImagem);
        float faixaV = Math.min(1, aspectoImagem / aspectoTela);
        batch.setColor(Color.WHITE);
        batch.draw(imagem, 0, 0, LARGURA, ALTURA,
                (1 - faixaU) / 2, (1 + faixaV) / 2,
                (1 + faixaU) / 2, (1 - faixaV) / 2);
    }

    private void mapa(EstadoPartida e) {
        int lado = e.limite() * 2 + 1;
        float celula = MAPA_TAMANHO / lado;
        painel(MAPA_X, MAPA_Y, MAPA_TAMANHO, MAPA_TAMANHO, 0.02f, 0.04f, 0.1f, 0.82f);
        for (int i = 0; i <= lado; i++) {
            painel(MAPA_X + i * celula, MAPA_Y, 1, MAPA_TAMANHO, 0.3f, 0.7f, 0.9f, 0.2f);
            painel(MAPA_X, MAPA_Y + i * celula, MAPA_TAMANHO, 1, 0.3f, 0.7f, 0.9f, 0.2f);
        }
        float centro = e.limite() * celula;
        painel(MAPA_X + centro + 2, MAPA_Y + centro + 2, celula - 3, celula - 3,
                0.05f, 0.7f, 0.8f, 0.35f);
        rotuloCelula("B", 0, 0, e.limite(), celula, Color.CYAN, 0.18f);

        for (var a : e.asteroides()) sprite(RecursosJogo.ASTEROIDE, a.x(), a.y(), e.limite(), celula);
        for (var p : e.passageiros()) {
            String inicial = p.tipo().substring(0, 1);
            rotuloCelula(inicial, p.x(), p.y(), e.limite(), celula, Color.YELLOW, 0.5f);
        }
        for (var i : e.inimigos()) sprite(RecursosJogo.INIMIGO, i.x(), i.y(), e.limite(), celula);
        sprite(RecursosJogo.NAVE, e.nave().x(), e.nave().y(), e.limite(), celula);
    }

    private void sprite(String caminho, int x, int y, int limite, float celula) {
        // Guarda de desenho; limites e coordenadas são responsabilidade do domínio.
        if (Math.abs(x) > limite || Math.abs(y) > limite) return;
        Texture imagem = recursos.textura(caminho);
        float escala = celula * 0.83f / Math.max(imagem.getWidth(), imagem.getHeight());
        float largura = imagem.getWidth() * escala;
        float altura = imagem.getHeight() * escala;
        batch.setColor(Color.WHITE);
        batch.draw(imagem, MAPA_X + (x + limite + 0.5f) * celula - largura / 2,
                MAPA_Y + (y + limite + 0.5f) * celula - altura / 2, largura, altura);
    }

    private void rotuloCelula(String valor, int x, int y, int limite, float celula, Color cor, float altura) {
        fonte.getData().setScale(1.2f);
        medida.setText(fonte, valor);
        texto(valor, MAPA_X + (x + limite + 0.5f) * celula - medida.width / 2,
                MAPA_Y + (y + limite + altura) * celula + medida.height / 2, 1.2f, cor);
    }

    private void informacoes(EstadoPartida e, List<Evento> eventos) {
        float x = 652;
        painel(x - 16, 112, 448, 584, 0.025f, 0.045f, 0.09f, 0.94f);
        texto(e.piloto(), x, 670, 1.25f, Color.WHITE);
        texto("Dificuldade " + e.dificuldade() + "  /  Mapa " + (2 * e.limite() + 1) + " x " + (2 * e.limite() + 1),
                x, 642, 1, Color.LIGHT_GRAY);
        texto("PONTOS", x, 593, 1, Color.LIGHT_GRAY);
        texto(Integer.toString(e.pontos()), x, 563, 2, Color.CYAN);
        texto("Vidas: " + e.nave().vidas() + "     Movimentos: " + e.movimentos(), x, 515, 1.1f, Color.WHITE);
        texto("A bordo: " + e.nave().passageiros().size() + "/" + e.nave().capacidade()
                + "     Restantes: " + e.passageiros().size(), x, 482, 1.1f, Color.WHITE);
        texto("Posição: (" + e.nave().x() + ", " + e.nave().y() + ")     Tempo: " + e.segundos() + "s",
                x, 449, 1, Color.LIGHT_GRAY);
        painel(x, 424, 410, 1, 0.2f, 0.5f, 0.7f, 0.6f);
        texto("CONTROLES", x, 399, 1, Color.CYAN);
        texto("W  cima       S  baixo", x, 369, 1.1f, Color.WHITE);
        texto("A  esquerda   D  direita", x, 339, 1.1f, Color.WHITE);
        texto("C  embarcar   Q  abortar", x, 309, 1.1f, Color.WHITE);
        texto("R  reiniciar após o fim   Esc  sair", x, 278, 1, Color.LIGHT_GRAY);
        texto(situacao(e.situacao()), x, 237, 1.1f,
                e.situacao() == Situacao.EM_ANDAMENTO ? Color.CYAN : Color.YELLOW);
        if (!eventos.isEmpty()) {
            fonte.getData().setScale(1);
            fonte.setColor(Color.LIGHT_GRAY);
            fonte.draw(batch, mensagem(eventos.get(eventos.size() - 1)), x, 204, 408, Align.left, true);
        }
    }

    private String situacao(Situacao situacao) {
        return switch (situacao) {
            case EM_ANDAMENTO -> "Missão em andamento";
            case VITORIA -> "Resgate concluído! Pressione R.";
            case DERROTA -> "Missão encerrada. Pressione R.";
            case ABORTADA -> "Missão abortada. Pressione R.";
        };
    }

    private String mensagem(Evento evento) {
        return switch (evento.tipo()) {
            case SEM_PASSAGEIRO -> "Nenhum passageiro nesta posição.";
            case EMBARCOU -> evento.nome() + " embarcou! +" + evento.valor() + " pontos.";
            case NAVE_CHEIA -> "A nave atingiu a capacidade máxima.";
            case COMANDO_INVALIDO -> "Comando inválido.";
            case ABORTOU -> "O piloto abortou a missão.";
            case COLISAO -> "Colisão! Vidas restantes: " + evento.valor();
            case NAVE_DESTRUIDA -> "A nave foi destruída.";
            case SEM_PONTOS -> "Os pontos se esgotaram.";
            case RETORNAR_BASE -> "Todos a bordo. Retorne à base (0,0).";
            case VITORIA -> "Passageiros resgatados e pontuação registrada.";
        };
    }

    private void texto(String texto, float x, float y, float escala, Color cor) {
        fonte.getData().setScale(escala);
        fonte.setColor(cor);
        fonte.draw(batch, texto, x, y);
    }

    private void painel(float x, float y, float largura, float altura, float r, float g, float b, float a) {
        batch.setColor(r, g, b, a);
        batch.draw(recursos.pixel(), x, y, largura, altura);
        batch.setColor(Color.WHITE);
    }
}
