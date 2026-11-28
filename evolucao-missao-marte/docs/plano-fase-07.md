# Plano aprovado da Fase 7 — Evolução livre

**Autorização:** em 27/11/2026 o usuário respondeu às decisões do plano: "D1 -
confirmo. D2 - Pode usar o postgresql a apresentação é proxima semana. D3 -
Serão os 3 rankings nas 3 diferentes dificuldades, em cada ranking a ordem é
decidida pela quantidade de pontos. D4 - confirmo. D5 - Por enquanto não."
As respostas estão aplicadas no texto e resumidas na seção 10.

**Pedido do usuário (26/11/2026):**
1. Hospedar o servidor no **Render**.
2. O jogo deixa de ser por turno: a fase começa, os inimigos se mexem sozinhos.
3. **Modo Infinito:** o jogador escolhe a dificuldade e, a cada setor concluído,
   passa ao próximo, um pouco mais difícil.
4. Ranking global com **tabelas separadas** para o modo Infinito.

**Origem:** `fases/fase-06-android/` (concluída, PRs #16 e #17 integrados).
**Destino:** `fases/fase-07-evolucao-livre/`.

**Orientação seguida:** o prompt mestre (Fase 7: "as alterações deverão
respeitar a arquitetura desenvolvida") e o enunciado do professor
(`T200-ProjArqSist/.../fase-07-evolucao-livre/docs/fase-07-enunciado.md`).
Do enunciado aproveito as regras que valem para qualquer melhoria: regras sem
`com.badlogic`, tempo como parâmetro (nada de relógio do sistema nem
`Thread.sleep` nas regras), `Random` injetado, testes antigos preservados,
servidor só muda se a melhoria exigir, e seção honesta de "não verificado".
O enunciado cita "Aumento de dificuldade" e "Campanha de fases"; o modo
Infinito junta as duas ideias.

## 1. Diagnóstico do estado de origem

- **Turnos:** `JogoService.executar(Acao)` move a nave, depois os inimigos, e
  testa colisão. `ControladorPartida.atualizar()` só chama o serviço quando há
  uma tecla ou toque, então nada anda sozinho.
- **Entrada:** `EntradaJogo` entrega comandos discretos (um por toque). Para
  tempo real é preciso saber também **qual direção está pressionada**.
- **Partida:** dificuldade fixa por partida (`FabricaMissoes`), 3 vidas,
  pontos iniciais 30/20/15, −1 por movimento.
- **Servidor:** `Partida` guarda dificuldade, resultado, pontos, duração e
  movimentos. O ranking é um único Top 10 de vitórias. Não sabe o modo.
- **Banco:** H2 em arquivo (`./dados`). Escuta só `127.0.0.1:8080` por padrão;
  porta fixa, sem `PORT` nem Dockerfile.
- **Cliente HTTP:** tempo limite de 8 s. No Render gratuito o servidor dorme
  depois de 15 min e leva cerca de 1 min para acordar.
- **Ambiente:** não há Docker nem PostgreSQL nesta máquina.

## 2. Entregas em três etapas (um PR cada)

| Etapa | Conteúdo | Por que nesta ordem |
|---|---|---|
| **7.1 Servidor no Render** | Dockerfile, `render.yaml`, PostgreSQL, `/api/saude`, `PORT`, APK apontando para a nuvem | Resolve já o problema do celular sem PC; base estável para as próximas |
| **7.2 Tempo real** | Modo dinâmico como padrão; modo clássico (turnos) preservado como opção | Muda o núcleo do jogo; precisa estar pronto antes do Infinito |
| **7.3 Modo Infinito e rankings** | Setores com dificuldade crescente; ranking por modo no servidor e no cliente | Depende das duas etapas anteriores |

Cada etapa compila, passa nos testes e é integrada por PR antes da próxima.

## 3. Etapa 7.1 — Servidor no Render

**Servidor**
- `GET /api/saude` público, respondendo `{"status":"ok"}` (verificação de
  saúde do Render e "acordar" o servidor pelo jogo).
- `server.port=${PORT:8080}` e `server.address=${MISSAOMARTE_BIND:127.0.0.1}`
  (no contêiner: `0.0.0.0`).
- Perfil `nuvem` com **PostgreSQL** (driver `org.postgresql:postgresql`),
  lido de variáveis de ambiente. H2 continua sendo o padrão local e dos testes.
- `Dockerfile` em duas etapas (build com JDK 17, execução com JRE 17), como
  usuário sem privilégios.
- `render.yaml` (Blueprint): serviço web Docker gratuito, `rootDir` na pasta do
  servidor, `healthCheckPath: /api/saude`, `MISSAOMARTE_JWT_SEGREDO` gerado pelo
  próprio Render (`generateValue`), banco ligado por `fromDatabase`.
  Nenhum segredo no repositório.
- `DEPLOY.md` com o passo a passo e os limites.

**Cliente**
- APK gerado com `-Pmissaomarte.servidor=https://<serviço>.onrender.com`
  (o mecanismo de endereço da Fase 6 já aceita isso). Desktop continua em
  `localhost` por padrão.
- Ao abrir o menu, o jogo chama `/api/saude` para acordar o servidor e mostra
  "Conectando ao servidor..." enquanto ele acorda. Tempo limite das chamadas
  sobe para 60 s só nessa primeira chamada.

**O que depende de você** (eu não crio contas nem autorizo acessos):
1. Criar a conta no Render e conectar o GitHub ao Render.
2. Aplicar o Blueprint (New → Blueprint → este repositório). Eu deixo o
   `render.yaml` pronto e o passo a passo com capturas no `DEPLOY.md`.
3. Me passar o endereço gerado (`https://….onrender.com`) para eu gerar o APK
   e validar no celular.

**Limites do plano gratuito** (documentação oficial do Render):
- O serviço **dorme após 15 min** sem uso e leva cerca de 1 min para acordar.
- O disco do serviço é **efêmero**: H2 em arquivo perderia tudo a cada deploy,
  por isso o PostgreSQL.
- O **PostgreSQL gratuito do Render expira 30 dias** após a criação (mais 14
  dias para virar pago, senão é apagado). Ver decisão D2.

## 4. Etapa 7.2 — Tempo real

Regras novas em `service` (sem `com.badlogic`), recebendo o tempo decorrido
como parâmetro (`avancar(segundos)`), com `Random` injetado:

| Item | Tempo real (novo padrão) | Clássico (igual à Fase 6) |
|---|---|---|
| Início | Contagem de 3 s | Imediato |
| Nave | Anda 1 célula a cada 0,15 s **enquanto a direção estiver pressionada** | 1 célula por toque/tecla |
| Inimigos | Andam sozinhos: 0,9 s / 0,7 s / 0,5 s por passo (Fácil/Médio/Difícil) | Andam a cada jogada |
| Pontos | −1 por segundo | −1 por movimento |
| Embarque | Automático ao passar pelo passageiro | Botão/tecla Embarcar |
| Pausa | Sim; e automática quando o app vai para segundo plano | Sim |

- Classes previstas: `RelogioDePasso` (acumula tempo e dispara passos),
  `RegrasTempoReal` (valores por dificuldade) e `SessaoTempoReal` (orquestra;
  reaproveita `Missao`, `Nave`, colisões e `FabricaMissoes`).
- `JogoService` e o modo clássico **não mudam**: os testes antigos continuam
  passando sem edição.
- Entrada: `EntradaJogo` ganha a consulta da direção mantida; toque (botões
  segurados), teclado e controle físico implementam. Um toque que começa e
  termina no mesmo quadro ainda vale um passo.
- Tela Nova partida: escolha do modo (**Tempo real**, **Infinito**,
  **Clássico**) além da dificuldade.
- O servidor passa a receber o **modo** da partida (etapa 7.3), para o
  ranking não misturar modos.

## 5. Etapa 7.3 — Modo Infinito e rankings

**Regras** (`ProgressaoInfinita`, classe pura e testada). O jogador escolhe a
dificuldade inicial; o setor *n* (começando em 1) usa:

| Parâmetro | Regra por setor (valores iniciais, ajustáveis no balanceamento) |
|---|---|
| Inimigos | base da dificuldade + 1 a cada 2 setores |
| Asteroides | base + 1 a cada 3 setores |
| Passo dos inimigos | tempo base × 0,92ⁿ⁻¹, mínimo de 0,25 s |
| Mapa | cresce 1 célula de raio a cada 4 setores, até um máximo |
| Pontos | acumulam; ao começar cada setor soma o valor inicial da dificuldade (30/20/15) |
| Vidas | continuam de um setor para o outro; +1 vida a cada 5 setores (máximo 5) |

- Setor concluído = todos os passageiros embarcados e volta à base. Aparece
  "Setor *n* concluído" e o próximo começa após a contagem.
- Fim da corrida: vidas ou pontos chegam a zero (ou o jogador aborta, que não
  é enviado, como hoje). O resultado enviado é o **setor alcançado** e os
  **pontos acumulados**.

**Servidor**
- `Partida` ganha `modo` (`CLASSICO`, `TEMPO_REAL`, `INFINITO`) e `setor`
  (só no Infinito). Requisição sem `modo` vale `CLASSICO`, para manter o
  contrato antigo.
- `GET /api/ranking?modo=…&dificuldade=…`:
  - Um Top 10 para cada **modo** e cada **dificuldade** (3 × 3 tabelas),
    sempre ordenado por **pontos** (decisão D3).
  - **Clássico** e **Tempo real**: contam as vitórias (como hoje).
  - **Infinito**: conta toda corrida terminada (no Infinito não existe
    vitória); o setor alcançado aparece na tabela.
- Testes MockMvc para os filtros, o padrão `CLASSICO` e a validação do `setor`.
- Migração: `ddl-auto=update` adiciona as colunas; partidas antigas ficam como
  `CLASSICO`.

**Cliente**
- Tela de ranking com abas **Tempo real / Infinito / Clássico** e, em cada
  uma, as três dificuldades (Fácil, Médio, Difícil).
- Histórico mostra o modo e o setor.
- Ranking local do convidado também separado por modo.

## 6. Padrões e princípios

- **Strategy:** regras por modo (turnos × tempo real) atrás de uma interface de
  sessão de partida, para a tela não precisar saber qual é.
- **Factory:** `FabricaMissoes` recebe os parâmetros do setor em vez de só a
  dificuldade (a versão antiga continua funcionando).
- **Port and Adapter:** direção mantida na porta `EntradaJogo`; `/api/saude` na
  porta `ServidorDoJogo`.
- **Configuração externa (12-factor):** porta, banco e segredo vêm do ambiente.
- Camadas: `model` e `service` continuam sem `com.badlogic` (conferido por
  busca); tempo e `Random` sempre recebidos por parâmetro.

## 7. Versões

Sem troca de versões: Java 17, Gradle 8.7, LibGDX 1.12.1 e AGP 8.3.2 no
cliente; Spring Boot 4.1.2 e Gradle 9.8.2 no servidor. Acrescenta apenas o
driver PostgreSQL (versão gerenciada pelo Spring Boot). A imagem Docker usa o
Eclipse Temurin 17.

## 8. Testes e validação

- Todos os testes da Fase 6 (27 no servidor, 83 no cliente) passam sem edição,
  ou cada edição é justificada.
- Novos testes com **tempo simulado** e semente fixa: passos da nave e dos
  inimigos, −1 ponto por segundo, embarque automático, pausa, contagem inicial,
  progressão do Infinito (setores 1, 2, 5, 10 e o mínimo de 0,25 s), vidas
  carregadas, envio do setor.
- Servidor: `modo` e `setor`, rankings por modo e dificuldade, `/api/saude`
  público, perfil `nuvem` carregando sem H2.
- `smokeDesktop` e `smokeOnline` atualizados para os modos novos.
- Deploy: `/api/saude` e cadastro/login/ranking contra o endereço do Render.
- **Celular real** (Galaxy S20 FE, via USB): jogar os três modos contra o
  servidor no Render, com capturas.

## 9. Documentação

`fase-07-evolucao-livre/README.md`, `docs/fase-07-tutorial.md` (12 seções),
`servidor/DEPLOY.md`, evidências (capturas desktop e Android), atualização do
índice e do README da raiz, issue #12 em In Progress → Done.

## 10. Decisões confirmadas (27/11/2026)

| # | Decisão | Resposta do usuário |
|---|---|---|
| D1 | Manter o modo Clássico (turnos) como opção | Confirmado |
| D2 | Banco no Render | PostgreSQL gratuito do Render (a apresentação é na semana seguinte, antes dos 30 dias de expiração) |
| D3 | Rankings | Uma tabela por dificuldade em cada modo, ordenada por pontos |
| D4 | Infinito só no tempo real | Confirmado |
| D5 | Tiro e inimigo perseguidor | Fora desta fase por enquanto |

## 11. Registro da entrega (28/11/2026)

| Etapa | PR | Resultado |
|---|---|---|
| 7.1 Servidor no Render | #18 | No ar em `https://missao-marte-servidor.onrender.com`; cadastro, login, envio e ranking conferidos por `curl` |
| 7.2 Tempo real | #19 | Padrão do jogo; Clássico mantido; `smokeTempoReal` com relógio real |
| 7.3 Infinito e rankings | #20 | 9 tabelas por pontos; colunas `modo`/`setor` criadas no PostgreSQL e partida antiga migrada para `CLASSICO` |
| Corações | #21 | Pedido do usuário durante a fase; corrigiu o HUD que só mostrava 3 vidas |
| Pacote da apresentação e direcional | #22 | Pedidos do usuário: zip com Java embutido (`jlink` + `.bat`, porque o `.exe` do `jpackage` era bloqueado pelo Smart App Control) e controles translúcidos só no celular |

Release: [v7.0-apresentacao](https://github.com/jpedro-lc/Projeto_Arquitetura-Missao_Marte/releases/tag/v7.0-apresentacao).

**Validação final:** servidor com 41 testes e cliente com 132; `smokeDesktop`,
`smokeOnline` (83 passos), `smokeTempoReal` e `smokeInfinito` passando; APK
testado no Galaxy S20 FE contra o Render.

**Fora do plano original:** os corações, o direcional na tela e o pacote da
apresentação foram pedidos pelo usuário durante a fase, depois de aprovado o
plano, e cada um entrou por pull request. Tiro e inimigo perseguidor ficaram de
fora (decisão D5).

**Divergência em testes antigos:** `smokeDesktop` e `smokeOnline` passaram a
escolher "Clássico" antes de decolar, porque o padrão virou o tempo real. O
roteiro verificado continua o mesmo.

