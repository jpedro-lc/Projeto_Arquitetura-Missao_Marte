package br.unifor.missaomarte.servidor.partida;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Ao iniciar: partidas anteriores à Fase 7 (modo vazio) passam a CLASSICO, para entrar no ranking certo. */
@Component
class MigracaoModo implements ApplicationRunner {
    private static final Logger LOG = LoggerFactory.getLogger(MigracaoModo.class);
    private final PartidaRepository partidas;

    MigracaoModo(PartidaRepository partidas) { this.partidas = partidas; }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int marcadas = partidas.marcarAntigasComoClassico();
        if (marcadas > 0) LOG.info("{} partida(s) antiga(s) marcada(s) como CLASSICO.", marcadas);
    }
}
