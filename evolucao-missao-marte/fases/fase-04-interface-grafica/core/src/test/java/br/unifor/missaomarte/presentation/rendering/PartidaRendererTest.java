package br.unifor.missaomarte.presentation.rendering;

import static org.junit.jupiter.api.Assertions.*;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.utils.Array;
import org.junit.jupiter.api.Test;

class PartidaRendererTest {
    @Test void passageiroEmCoordenadasNegativasNaoQuebraAnimacao() {
        // Regressão: (-5,-5) gerava tempo negativo e índice -1 no getKeyFrame.
        var quadros = new Array<Integer>(new Integer[] {1, 2, 3, 4});
        var animacao = new Animation<>(0.18f, quadros, Animation.PlayMode.LOOP_PINGPONG);
        for (int x = -10; x <= 10; x++) for (int y = -10; y <= 10; y++) {
            float defasagem = PartidaRenderer.defasagem(x, y);
            assertTrue(defasagem >= 0, "defasagem negativa em " + x + "," + y);
            assertNotNull(animacao.getKeyFrame(defasagem));
        }
    }
}
