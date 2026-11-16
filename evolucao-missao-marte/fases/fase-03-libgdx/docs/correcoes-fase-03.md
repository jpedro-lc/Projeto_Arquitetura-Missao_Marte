# Correções da Fase 3 — Limites e encerramento

Solicitação do usuário após a entrega da Fase 3: corrigir inimigos saindo da tela
e movimentos após o encerramento. A correção foi feita em branch própria,
`correcao-fase-03`, a partir da principal com o PR #2 integrado. Fases 1/2,
originais e o referencial permanecem intactos. A Fase 4 não foi iniciada.

## Inimigos fora do mapa

Antes, Missao sorteava dx/dy e chamava `Inimigo.mover`, sem informar limites.
Uma posição (5,5) podia virar (6,6) em um mapa de limite 5. A apresentação apenas
deixava de desenhar a entidade fora da grade; ela continuava existindo no domínio.

Agora `JogoService` passa o limite da partida a `Missao.moverInimigos(random,
limite)`. Esta chama `Inimigo.moverComLimites`, restringindo cada eixo ao intervalo
`[-limite, limite]`. Ao tentar sair, o inimigo permanece na borda naquele eixo;
o deslocamento permitido no outro eixo continua acontecendo. Não há teleporte,
novo sorteio ou dependência de pixels/LibGDX nas regras.

## Encerramento e contagem de movimentos

O bloqueio de novas ações após vitória/derrota/aborto já existia em JogoService
e na coordenação gráfica. Os testes confirmaram que ele congela movimentos,
pontos, posições, vidas, tempo e gravações no ranking.

O problema reproduzido estava no próprio comando de aborto: ele encerrava a
partida, mas o método seguia movendo inimigos e verificando colisões. Uma colisão
fatal podia inclusive substituir ABORTADA por DERROTA.

Agora, depois de marcar ABORTADA e gerar ABORTOU, o serviço retorna o resultado
imediatamente. Q não movimenta inimigos, não verifica colisões nem conta movimento.
Não foi necessário adicionar outro contador ou bloquear desenho da tela.

O último movimento que provoca vitória ou derrota continua contando uma vez,
pois aconteceu durante a partida. Os comandos seguintes são ignorados. Tentativas
de mover a nave contra uma borda continuam contando e consumindo um ponto;
essa regra é diferente de ações depois do fim e não foi alterada nesta correção.

## Evidência e testes

Os 17 casos novos de JogoServiceRegressaoTest foram executados antes da mudança:
11 falharam, reproduzindo saída das bordas e processamento de colisão após aborto.
Os casos de congelamento após fim já passaram antes da mudança.

Após a correção:

- 12 casos de bordas/cantos/retorno ao interior, com limites 1, 2, 5 e 12.
- Aborto ao lado de inimigo e sobre colisão fatal sem mudar posições ou vidas.
- Todas as ações ignoradas após vitória, derrota por vidas e aborto.
- Derrota por pontos: o último movimento conta uma vez e 100 comandos seguintes
  não mudam a partida.
- Total: **32 casos JUnit aprovados**. O adaptador desktop executa 162 checagens
  herdadas, com expectativas dos bugs atualizadas apenas nesta fase.
- Janela real: inimigo inicialmente na borda, 210 frames, duas mudanças de
  resolução, vitória, aborto, reinício, comandos depois do fim e descarte.

Com JDK 17, na raiz da Fase 3:

```bash
./gradlew clean build
./gradlew :lwjgl3:smokeDesktop
./gradlew :lwjgl3:installDist
./gradlew :lwjgl3:run
```

O build comum não exige display; smokeDesktop exige janela/OpenGL. A entrada
desse smoke é determinística e injetada. A verificação não equivale a um teste
manual do teclado físico. Se uma janela antiga estiver aberta, feche e execute
novamente a aplicação da pasta `fase-03-libgdx` para carregar as classes corrigidas.
