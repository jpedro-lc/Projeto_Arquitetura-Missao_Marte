package br.unifor.missaomarte.servidor.partida;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PartidaRepository extends JpaRepository<Partida, Long> {
    List<Partida> findByUsuarioIdOrderByRegistradaEmDescIdDesc(Long usuarioId, Pageable pagina);

    /**
     * Melhor vitória de cada jogador: a partida p entra se nenhuma outra vitória q do mesmo jogador
     * for melhor (mais pontos; empate: menos tempo; empate: mais antiga; por fim, menor id).
     */
    @Query("""
            select p from Partida p join fetch p.usuario
            where p.resultado = br.unifor.missaomarte.servidor.partida.Resultado.VITORIA and p.pontuacao > 0
              and not exists (
                select q from Partida q
                where q.usuario = p.usuario
                  and q.resultado = br.unifor.missaomarte.servidor.partida.Resultado.VITORIA and q.pontuacao > 0
                  and (q.pontuacao > p.pontuacao
                    or (q.pontuacao = p.pontuacao and q.duracaoSegundos < p.duracaoSegundos)
                    or (q.pontuacao = p.pontuacao and q.duracaoSegundos = p.duracaoSegundos
                        and (q.registradaEm < p.registradaEm or (q.registradaEm = p.registradaEm and q.id < p.id)))))
            order by p.pontuacao desc, p.duracaoSegundos asc, p.registradaEm asc, p.id asc
            """)
    List<Partida> melhoresVitorias(Pageable limite);

    /**
     * Fase 7: Top 10 de um modo numa dificuldade, sempre por pontos, uma linha por jogador (mesmos desempates).
     * Clássico e Tempo real contam só vitórias; no Infinito toda corrida termina em derrota, então todas contam.
     */
    @Query("""
            select p from Partida p join fetch p.usuario
            where p.modo = :modo and p.dificuldade = :dificuldade and p.pontuacao > 0
              and (:todas = true or p.resultado = br.unifor.missaomarte.servidor.partida.Resultado.VITORIA)
              and not exists (
                select q from Partida q
                where q.usuario = p.usuario and q.modo = :modo and q.dificuldade = :dificuldade and q.pontuacao > 0
                  and (:todas = true or q.resultado = br.unifor.missaomarte.servidor.partida.Resultado.VITORIA)
                  and (q.pontuacao > p.pontuacao
                    or (q.pontuacao = p.pontuacao and q.duracaoSegundos < p.duracaoSegundos)
                    or (q.pontuacao = p.pontuacao and q.duracaoSegundos = p.duracaoSegundos
                        and (q.registradaEm < p.registradaEm or (q.registradaEm = p.registradaEm and q.id < p.id)))))
            order by p.pontuacao desc, p.duracaoSegundos asc, p.registradaEm asc, p.id asc
            """)
    List<Partida> melhores(@Param("modo") ModoJogo modo, @Param("dificuldade") Dificuldade dificuldade,
                           @Param("todas") boolean todas, Pageable limite);

    /** Partidas gravadas antes da Fase 7 não têm modo: eram do jogo por turnos. */
    @Modifying
    @Query("update Partida p set p.modo = br.unifor.missaomarte.servidor.partida.ModoJogo.CLASSICO where p.modo is null")
    int marcarAntigasComoClassico();
}
