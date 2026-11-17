package br.unifor.missaomarte.presentation.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import br.unifor.missaomarte.model.EstadoPartida;
import br.unifor.missaomarte.presentation.EfeitosPartida;
import br.unifor.missaomarte.presentation.assets.RecursosJogo;

/** Converte coordenadas discretas em posições gráficas; não avança regras. */
public final class PartidaRenderer {
    public static final float LARGURA = 1280;
    public static final float ALTURA = 800;
    private final RecursosJogo recursos;
    private final Animation<TextureRegion> explosao;
    private final Animation<TextureRegion> propulsor;
    private final TextureRegion professor;
    private final TextureRegion engenheiro;
    private final TextureRegion astronauta;

    public PartidaRenderer(RecursosJogo recursos) {
        this.recursos = recursos;
        var frames = new Array<TextureRegion>();
        for (int i = 1; i <= 11; i++) frames.add(new TextureRegion(recursos.textura(RecursosJogo.explosao(i))));
        explosao = new Animation<>(0.08f, frames, Animation.PlayMode.NORMAL);
        frames = new Array<>();
        for (int i = 1; i <= 5; i++) frames.add(new TextureRegion(recursos.textura(RecursosJogo.propulsor(i))));
        propulsor = new Animation<>(0.08f, frames, Animation.PlayMode.LOOP);
        professor = passageiro(RecursosJogo.PROFESSOR);
        engenheiro = passageiro(RecursosJogo.ENGENHEIRO);
        astronauta = passageiro(RecursosJogo.ASTRONAUTA);
    }

    private TextureRegion passageiro(String caminho) {
        // Recorte do personagem; o PNG fornecido contém margem transparente extensa.
        return new TextureRegion(recursos.textura(caminho), 85, 45, 230, 290);
    }

    public void desenhar(Batch batch, EstadoPartida estado, EfeitosPartida efeitos,
                         float x, float y, float largura, float altura, float tempoVisual) {
        fundo(batch, recursos, x, y, largura, altura);
        batch.setColor(0.015f, 0.025f, 0.065f, 0.2f);
        batch.draw(recursos.pixel(), x, y, largura, altura);
        batch.setColor(Color.WHITE);
        int lado = estado.limite() * 2 + 1;
        float passoX = (largura - 110) / lado;
        float passoY = (altura - 90) / lado;
        float tamanho = Math.min(62, Math.min(passoX, passoY) * 1.25f);
        float origemX = x + largura / 2;
        float origemY = y + altura / 2;
        batch.setColor(0.1f, 0.7f, 0.85f, 0.22f);
        batch.draw(recursos.pixel(), origemX - tamanho / 2, origemY - tamanho / 2, tamanho, tamanho);
        batch.setColor(Color.WHITE);
        recursos.pequena().setColor(0.3f, 0.9f, 1, 0.7f);
        recursos.pequena().draw(batch, "BASE", origemX - 25, origemY - tamanho / 2 - 6);
        for (var a : estado.asteroides()) {
            desenharImagem(batch, new TextureRegion(recursos.textura(RecursosJogo.ASTEROIDE)),
                    origemX + a.x() * passoX, origemY + a.y() * passoY, tamanho);
        }
        for (var p : estado.passageiros()) {
            TextureRegion regiao = switch (p.tipo()) {
                case "Engenheiro" -> engenheiro;
                case "Astronauta" -> astronauta;
                default -> professor;
            };
            desenharImagem(batch, regiao, origemX + p.x() * passoX, origemY + p.y() * passoY, tamanho * 0.95f);
        }
        for (var i : estado.inimigos()) {
            desenharImagem(batch, new TextureRegion(recursos.textura(RecursosJogo.INIMIGO)),
                    origemX + i.x() * passoX, origemY + i.y() * passoY, tamanho);
        }
        if (estado.nave().vidas() > 0) {
            float nx = origemX + estado.nave().x() * passoX;
            float ny = origemY + estado.nave().y() * passoY;
            desenharImagem(batch, propulsor.getKeyFrame(tempoVisual), nx, ny - tamanho * 0.2f, tamanho);
            desenharImagem(batch, new TextureRegion(recursos.textura(RecursosJogo.NAVE)), nx, ny, tamanho * 1.1f);
        }
        for (var efeito : efeitos.ativos()) {
            if (efeito.tipo() == EfeitosPartida.Tipo.EXPLOSAO) {
                desenharImagem(batch, explosao.getKeyFrame(efeito.idade()), origemX + efeito.x() * passoX,
                        origemY + efeito.y() * passoY, tamanho * 2);
            } else {
                batch.setColor(1, 1, 1, 1 - efeito.idade() / efeito.duracao());
                desenharImagem(batch, new TextureRegion(recursos.textura(RecursosJogo.RESGATADO)),
                        origemX + efeito.x() * passoX, origemY + efeito.y() * passoY,
                        tamanho * (1.7f + efeito.idade()));
                batch.setColor(Color.WHITE);
            }
        }
        recursos.pequena().setColor(Color.WHITE);
    }

    private static void desenharImagem(Batch batch, TextureRegion imagem, float x, float y, float tamanho) {
        float escala = tamanho / Math.max(imagem.getRegionWidth(), imagem.getRegionHeight());
        float largura = imagem.getRegionWidth() * escala;
        float altura = imagem.getRegionHeight() * escala;
        batch.draw(imagem, x - largura / 2, y - altura / 2, largura, altura);
    }

    public static void fundo(Batch batch, RecursosJogo recursos, float x, float y, float largura, float altura) {
        Texture imagem = recursos.textura(RecursosJogo.FUNDO);
        float aspectoImagem = (float) imagem.getWidth() / imagem.getHeight();
        float aspectoTela = largura / altura;
        float u = Math.min(1, aspectoTela / aspectoImagem);
        float v = Math.min(1, aspectoImagem / aspectoTela);
        batch.setColor(Color.WHITE);
        batch.draw(imagem, x, y, largura, altura, (1 - u) / 2, (1 + v) / 2, (1 + u) / 2, (1 - v) / 2);
    }
}
