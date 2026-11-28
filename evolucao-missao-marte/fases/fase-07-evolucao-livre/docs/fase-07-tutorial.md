# Tutorial da Fase 7 — Evolução livre: nuvem, tempo real, modo Infinito e rankings

## 1. Introdução

A Fase 6 terminou com o Missão Marte rodando no desktop e no Android, com o
mesmo `core` e o mesmo servidor de ranking. Dois pontos ficaram em aberto:

- o servidor só existia no PC do aluno, então o celular dependia de cabo USB e
  `adb reverse`;
- o jogo continuava **por turnos**: nada se movia enquanto o jogador não
  apertava uma tecla.

A Fase 7 é de **evolução livre**. O prompt mestre pede melhorias que
"respeitem a arquitetura desenvolvida", e o enunciado do professor acrescenta
regras de camadas, de testes e de documentação. As melhorias escolhidas pela
equipe, com o plano aprovado em 27/11/2026
([`plano-fase-07.md`](../../../docs/plano-fase-07.md)), foram:

1. **Servidor na nuvem (Render)**, com PostgreSQL.
2. **Jogo em tempo real** como padrão, mantendo o modo **Clássico** (por turnos).
3. **Modo Infinito**: setores em sequência, cada um um pouco mais difícil.
4. **Rankings separados** por modo e dificuldade (9 tabelas, por pontos).

Depois vieram ajustes pedidos durante a fase:

- **vidas com corações**;
- **direcional e botão Embarcar na tela do celular**;
- um **pacote para a apresentação**, que roda em outro computador sem instalar nada.

A entrega foi feita em PRs pequenos (#18 a #22), um por etapa, todos integrados
na `master`.

## 2. Objetivos de aprendizagem

- Publicar um serviço Spring Boot em contêiner, com a configuração vinda do
  ambiente (porta, banco, segredo), sem segredos no repositório.
- Transformar um jogo por turnos em tempo real **sem ler o relógio nas regras**:
  o tempo chega por parâmetro, e assim as regras são testáveis com tempo simulado.
- Aplicar o padrão **Strategy** para ter vários modos de jogo atrás de um
  contrato, e o **Adapter** para reaproveitar o `JogoService` antigo sem
  alterá-lo.
- Evoluir uma API pública sem quebrar os clientes antigos: campos novos
  opcionais, valores padrão e rota antiga preservada.
- Separar layout por plataforma (celular × desktop) sem duplicar a lógica da
  partida.
- Medir o impacto de cada mudança: testes antigos preservados, testes novos e
  o que ficou sem verificação.

## 3. Conceitos teóricos

**Configuração externa (12-factor).** O mesmo artefato roda em lugares
diferentes porque o que muda entre eles (porta, banco, segredo) vem de variáveis
de ambiente: `server.port=${PORT:8080}`, `DATABASE_URL`,
`MISSAOMARTE_JWT_SEGREDO`. O Render injeta essas variáveis, e localmente valem os
padrões.

**Contêiner em duas etapas.** O `Dockerfile` compila com o JDK e executa com só o
JRE e o JAR. A imagem final fica menor e roda com um usuário sem privilégios.

**Disco efêmero.** No plano gratuito do Render, o disco do serviço é apagado a
cada deploy. Um H2 em arquivo perderia contas e rankings, e por isso o banco na
nuvem é PostgreSQL.

**Laço de jogo com passo fixo.** O quadro (`render(delta)`) dura um tempo
variável. As regras acumulam esse tempo e disparam **passos** em intervalos
fixos: 0,15 s para a nave e 0,9, 0,7 ou 0,5 s para os inimigos. Um quadro longo,
como na volta do segundo plano, é limitado a 0,1 s para não virar um "salto".

**Strategy.** `RegrasDePartida` é o contrato que a tela usa e tem três
implementações (Clássico, Tempo real e Infinito). A tela não sabe qual está
usando.

**Adapter.** `RegrasClassicas` faz o `JogoService` das Fases 1 a 6 caber no
contrato novo, sem mudar o comportamento dele.

**Compatibilidade de contrato.** Para uma API usada por clientes antigos:

- os campos novos são opcionais, com padrão (`modo` ausente vale `CLASSICO`);
- a rota antiga continua respondendo igual (`GET /api/ranking` sem parâmetros);
- os dados antigos são migrados (`MigracaoModo`).

**Desenho por código.** Os assets novos (corações e setas) saem de padrões de
pixels em classes Java puras. Não há imagem de terceiros, e os desenhos podem ser
testados sem janela.

Referências: documentação do Render (*Deploy for Free*, *Blueprint YAML
Reference*), Spring Boot (*Externalized Configuration*), libGDX (*Scene2D*,
*Pixmap*, *Application type*) e `jlink`/`jpackage` do JDK 17. O referencial
`tutorial-api-gradle-libgdx.md` não cobre nuvem nem tempo real.

## 4. Problemas encontrados no código original (Fase 6)

| Problema | Consequência |
|---|---|
| Servidor só local (`127.0.0.1:8080`), porta fixa, H2 em arquivo | Celular só jogava online com cabo e `adb reverse`; não dava para publicar |
| Cliente HTTP com tempo limite de 8 s | No Render gratuito o servidor leva ~1 min para acordar: o primeiro login falharia |
| `ControladorPartida.atualizar()` só chamava o serviço com tecla | Nada se movia sozinho |
| `EntradaJogo` só entregava comandos discretos | Não havia como saber "qual direção está pressionada agora" |
| `Partida` sem modo; um único Top 10 | Partidas de modos diferentes se misturariam no ranking |
| HUD com 3 espaços de vida fixos | No Infinito (até 5 vidas) a 4ª e a 5ª nunca apareceriam |
| Botões de direção como texto no rodapé | No celular, pequenos e longe do polegar |

## 5. Arquitetura antes da transformação

```mermaid
flowchart LR
  subgraph cliente
    GS[GameScreen] --> CP[ControladorPartida]
    CP --> JS[JogoService<br/>por turnos]
    CP --> EJ[EntradaJogo<br/>comandos discretos]
    GS --> SH[ServidorHttp]
  end
  SH -- "HTTP local (cabo / Wi-Fi)" --> SRV[Spring Boot<br/>H2 em arquivo<br/>127.0.0.1:8080]
```

Um único modo, uma única tabela de ranking e o servidor preso ao PC.

## 6. Passo a passo da implementação

Etapas na ordem em que foram feitas, com os **problemas reais** de cada uma.

**7.1 — Servidor no Render (PR #18)**
1. Copiei a Fase 6 para `fase-07-evolucao-livre/`, só com os arquivos versionados.
2. Criei o `GET /api/saude` público, a porta em `PORT` e o driver PostgreSQL. A
   `UrlDoBanco` converte `postgresql://usuário:senha@host/banco` (formato do
   Render) para o JDBC que o Spring exige.
3. Escrevi o `Dockerfile`, o `render.yaml` (Blueprint, na raiz do repositório) e
   o `DEPLOY.md`. O segredo do JWT é gerado pelo Render (`generateValue`).
4. No cliente: `acordar()` chama `/api/saude` ao abrir o jogo, o tempo limite
   passou de 8 s para 70 s e o login avisa "Conectando... pode levar até 1 min.".
5. *Problema:* esta máquina não tem Docker, então a imagem foi montada pela
   primeira vez no próprio Render. O deploy funcionou de primeira, e conferi
   cadastro, login, envio e ranking por `curl` contra a URL pública.

**7.2 — Tempo real (PR #19)**
1. `RegrasDePartida` (Strategy), `RegrasClassicas` (Adapter), `TempoRealService`
   e `RitmoTempoReal`.
2. `EntradaJogo.direcaoMantida()`, implementada no teclado, no controle e no toque.
3. `ControladorPartida.atualizar(segundos)`. A Nova partida ganhou a escolha do
   modo, e a contagem 3-2-1 aparece na tela.
4. *Problema:* os primeiros testes usavam quadros de 0,125 s, maiores que o
   limite de 0,1 s, e cada passo era cortado. Troquei para 1/16 s, que é exato em
   binário e fica abaixo do limite.
5. *Problema:* com o tempo real como padrão, `smokeDesktop` e `smokeOnline`
   (roteiros do modo por turnos) passaram a falhar. Esses roteiros agora clicam
   em **Clássico** antes de decolar, e o que eles verificam não mudou. Essa é a
   única edição em testes antigos.
6. *Problema:* o clique do teste acertava o botão de modo em (115, 23), porque a
   tela ainda não tinha sido diagramada. O `clicar()` dos testes agora valida o
   layout antes de medir o botão.
7. *Problema:* a contagem cobria a nave e foi movida para o terço de cima do mapa.
   E os inimigos nos cantos do mapa às vezes não se moviam, porque ficavam presos
   na borda. Eles saíram dos cantos, e a verificação passou a olhar a partida
   inteira.

**7.3 — Infinito e rankings (PR #20)**
1. `ProgressaoInfinita` (classe pura) e `InfinitoService`, que usa o
   `TempoRealService` como motor de cada setor (`iniciarSetor`).
2. Servidor: `modo` e `setor` em `Partida`, a rota
   `GET /api/ranking?modo=&dificuldade=` e a `MigracaoModo`.
3. Cliente: envio do modo e do setor, ranking com abas (local e global) e
   histórico com o modo.
4. *Problema:* no Render, as colunas novas precisavam ser criadas numa tabela que
   já tinha dados. Deixei `modo` anulável para o `ddl-auto=update` funcionar, e a
   migração preenche `CLASSICO`. Conferido após o deploy: a partida de teste
   antiga apareceu como `CLASSICO`.

**Ajustes (PRs #21 e #22)**
1. Corações (`DesenhoCoracao`): ao implementar, achei o bug do HUD com só 3
   espaços.
2. Pacote da apresentação. *Problema:* o `.exe` do `jpackage` não é assinado e o
   **Smart App Control** do Windows 11 o bloqueou. Troquei por `jlink` + um `.bat`
   que abre o `javaw.exe` original, assinado pela Oracle. Testei o zip extraído
   fora do projeto, com o bloqueio ativo.
3. Direcional na tela. A primeira versão, em colunas laterais, encolhia o mapa.
   A pedido do usuário, ficou **só no celular**, translúcido e por cima do mapa,
   e o desktop voltou ao rodapé antigo.

## 7. Código antes e depois

**Controlador: o tempo do quadro passa a mover a partida.**

```java
// Antes (Fase 6): nenhum quadro vira turno sozinho
public boolean atualizar() {
    var comando = entrada.ler();
    ...
    } else if (comando.acao() != Acao.VAZIA && estado.situacao() == Situacao.EM_ANDAMENTO) {
        var resultado = jogo.executar(comando.acao());
```

```java
// Depois (Fase 7): o modo decide; no tempo real, os segundos do quadro andam a partida
public boolean atualizar(float segundos) {
    ...
    } else if (regras.modo() == ModoJogo.CLASSICO) {
        if (comando.acao() != Acao.VAZIA && emAndamento) registrar(regras.executar(comando.acao()));
    } else if (emAndamento) {
        Acao direcao = entrada.direcaoMantida();
        if (!direcao.movimento() && comando.acao().movimento()) direcao = comando.acao(); // toque rápido
        var resultado = regras.avancar(segundos, direcao);
```

**Passo fixo nas regras (sem relógio do sistema).**

```java
desdePassoNave = Math.min(ritmo.passoNave(), desdePassoNave + dt);
if (direcaoMantida.movimento() && desdePassoNave >= ritmo.passoNave()) { /* move 1 casa */ desdePassoNave = 0; }
acumuladoInimigos += dt;
while (acumuladoInimigos >= ritmo.passoInimigos()) { acumuladoInimigos -= ritmo.passoInimigos(); missao.moverInimigos(random, limite); }
```

**Servidor: configuração vinda do ambiente.**

```properties
# Antes
server.port=8080
# Depois
server.port=${PORT:8080}
```

```java
// ServidorApplication: DATABASE_URL (Render) vira spring.datasource.* antes de subir o Spring
UrlDoBanco.paraSpring(System.getenv("DATABASE_URL")).forEach(System::setProperty);
```

**HUD: de mini-naves que somem para corações cheios e vazios.**

```java
// Antes: 3 imagens; perder vida = esconder a imagem
for (int i = 0; i < vidas.length; i++) vidas[i].setVisible(i < dados.vidas());
// Depois: até 5; as perdidas aparecem vazias
vidas[i].setVisible(i < HudDados.espacosDeVida(dados.vidas(), picoDeVidas));
vidas[i].setDrawable(i < dados.vidas() ? cheio : vazio);
```

## 8. Explicação das alterações

**Onde ficou cada parte.** As regras do jogo ficam em `model` e `service`, sem
LibGDX (conferido por busca). A apresentação, com telas, desenho e entrada, fica
em `presentation` e `screen`. Abaixo, cada arquivo das camadas de regras e dados
que foi criado ou alterado, com o motivo:

| Arquivo | Novo/alterado | Por quê |
|---|---|---|
| `model/ModoJogo` | novo | Os três modos |
| `model/ResultadoAcao` | +`SETOR_CONCLUIDO` | Evento do Infinito (som e efeito) |
| `model/Nave` | +construtor com vidas | Vidas passam de um setor para o outro |
| `service/RegrasDePartida` | novo | Contrato (Strategy) dos modos |
| `service/RegrasClassicas` | novo | Adapter do `JogoService` |
| `service/TempoRealService`, `RitmoTempoReal` | novos | Regras e valores do tempo real |
| `service/InfinitoService`, `ProgressaoInfinita` | novos | Corrida de setores e dificuldade crescente |
| `service/FabricaMissoes` | +`criarSetor` | Obstáculos extras **depois** dos sorteios originais: `criar()` gera os mesmos mapas (teste compara) |
| `service/JogoService` | +getter `ranking()` | O tempo real grava no mesmo ranking local. Nenhuma regra mudou |
| `repository/*` | modo e setor | Ranking local com abas; arquivos antigos (6 colunas) continuam legíveis |
| `remoto/*` | modo e setor | Contrato com o servidor. Construtores antigos mantidos |

**Decisões.**
- **Clássico mantido (D1):** preserva os testes e o comportamento das fases
  anteriores, como pede o enunciado.
- **Infinito só no tempo real (D4).** A corrida só registra no ranking quando
  termina em derrota. Abortar não registra, como nos outros modos.
- **Rankings (D3):** uma tabela por modo e dificuldade, por pontos. No Infinito
  contam todas as corridas, porque ali não existe vitória.
- **Proteção de 1,5 s após colisão:** sem ela, um inimigo parado sobre a nave
  tiraria as 3 vidas em menos de 2 s.
- **Controles na tela só no Android:** no desktop o teclado é o principal. Por
  cima do mapa e translúcidos, eles não encolhem o mapa.

**Padrões: o que segue o padrão de fato e o que só se parece.** São de fato:

- **Strategy:** a tela troca o modo sem `if` sobre o tipo.
- **Adapter:** `RegrasClassicas` só traduz chamadas.
- **Port and Adapter:** `EntradaJogo` e `ServidorDoJogo`, cada um com adaptadores
  por plataforma.
- **Decorator:** `RankingSomenteConvidado`, herdado da Fase 5.

O `InfinitoService` usa o `TempoRealService` por **composição**. Ele parece um
Decorator, mas não é um, porque muda o significado do resultado: a vitória vira
`SETOR_CONCLUIDO`.

## 9. Arquitetura resultante

```mermaid
flowchart LR
  subgraph cliente["cliente (desktop / Android)"]
    GS[GameScreen] --> CP[ControladorPartida]
    CP -->|Strategy| RP{{RegrasDePartida}}
    RP --> RC[RegrasClassicas] --> JS[JogoService<br/>Fases 1-6]
    RP --> TR[TempoRealService]
    RP --> IS[InfinitoService] -->|motor de cada setor| TR
    IS --> PI[ProgressaoInfinita]
    CP --> EJ{{EntradaJogo<br/>+ direcaoMantida}}
    EJ --> TQ[ToqueEntradaJogo<br/>direcional no celular]
    EJ --> CF[ControleFisicoEntradaJogo]
    GS --> SH[ServidorHttp<br/>acordar + 70 s]
  end
  SH -- HTTPS --> R[Render: contêiner Docker<br/>Spring Boot 4.1]
  R --> PG[(PostgreSQL<br/>Render)]
```

API depois da Fase 7:

| Rota | Mudança |
|---|---|
| `GET /api/saude` | Nova, pública |
| `POST /api/partidas` | `modo` (opcional, padrão `CLASSICO`) e `setor` (só no Infinito, e obrigatório nele) |
| `GET /api/ranking` | Sem parâmetros: igual à Fase 6. `?modo=&dificuldade=`: uma das 9 tabelas |
| `GET /api/partidas/minhas` | Resposta traz `modo` e `setor` |

## 10. Como executar e testar

```powershell
# servidor (local)
cd servidor
.\gradlew.bat test                                  # 41 testes
.\gradlew.bat bootRun

# cliente
cd cliente
.\gradlew.bat clean build                           # 132 testes + APK (com Android SDK)
.\gradlew.bat :lwjgl3:run                           # desktop contra localhost
.\gradlew.bat :lwjgl3:run "-Dmissaomarte.servidor=https://missao-marte-servidor.onrender.com"
.\gradlew.bat :lwjgl3:smokeDesktop :lwjgl3:smokeTempoReal :lwjgl3:smokeInfinito
.\gradlew.bat :lwjgl3:smokeOnline                   # exige o servidor local no ar
.\gradlew.bat :lwjgl3:zipApresentacao               # pacote para outro Windows
```

**Evidências (28/11/2026, Windows 11, OpenJDK 17.0.2):**

| Verificação | Resultado |
|---|---|
| Servidor `gradlew test` | 41 testes, 0 falhas (27 da Fase 6 + 14) |
| Cliente `gradlew clean build` | 132 testes, 0 falhas (83 da Fase 6 + 49) |
| `smokeDesktop` (clássico, janela real) | Passou |
| `smokeOnline` contra o servidor local | Passou, 83 passos |
| `smokeTempoReal` (relógio real: contagem, segurar D/A, embarque automático, pausa, vitória) | Passou (rodou várias vezes) |
| `smokeInfinito` (setor 1 → setor 2 → fim → aba Infinito) | Passou |
| Render: `/api/saude`, cadastro, login, envio, ranking por modo | OK por `curl`; partida antiga migrada para `CLASSICO` |
| `com.badlogic` em `model/service/repository/remoto` | Nenhum |
| Zip da apresentação extraído fora do projeto, Smart App Control ativo | Janela do jogo abriu |
| Galaxy S20 FE (Android 13), APK apontando para o Render | Controles translúcidos sobre o mapa; partida jogada até a vitória pelo toque |

Capturas: [`evidencias/tempo-real/`](evidencias/tempo-real/),
[`evidencias/infinito/`](evidencias/infinito/),
[`evidencias/coracoes/`](evidencias/coracoes/) e
[`evidencias/direcional/`](evidencias/direcional/).

## 11. Exercícios para os alunos

1. **Tiro:** acrescente a ação `ATIRAR` com munição limitada como mais uma
   implementação de regra no `TempoRealService`, sem tocar no Clássico. Teste com
   tempo simulado.
2. **Inimigo perseguidor:** extraia o movimento dos inimigos para uma interface
   (Strategy) e crie um que se aproxime da nave. Onde entra o `Random` injetado?
3. **Ranking honesto:** hoje o servidor confia na pontuação que o cliente envia.
   Proponha (sem implementar) como o servidor poderia validar uma partida.
4. **Migração versionada:** troque o `ddl-auto=update` por Flyway e escreva a
   migração que cria `modo` e `setor`.
5. **Controle analógico:** substitua o direcional por um *joystick* virtual
   (`Touchpad` do Scene2D) e compare a jogabilidade.

## 12. Conclusão

A Fase 7 levou o jogo para a nuvem, mudou o núcleo de turnos para tempo real e
criou um modo novo com rankings próprios. Os 27 testes do servidor e os 83 do
cliente herdados da Fase 6 continuaram passando. A única edição em testes antigos
foi nos roteiros com janela, que passaram a escolher o modo Clássico porque o
padrão mudou.

**O que não foi verificado:**
- multitoque real (segurar uma seta e tocar em Embarcar ao mesmo tempo), porque o
  `adb` não simula dois dedos;
- outro computador e outra versão do Windows com o pacote, e o aviso do
  SmartScreen em máquinas sem Smart App Control;
- a imagem Docker local (sem Docker nesta máquina; só o build do Render);
- controle Bluetooth com o tempo real;
- a partida do Infinito por muitos setores no aparelho (o balanceamento a partir
  do setor 5 só foi coberto por testes de unidade);
- áudio no celular.

**Limitações e próximos passos:**
1. O PostgreSQL gratuito do Render **expira por volta de 27/12/2026**. Depois
   disso é preciso um plano pago ou outro banco.
2. A pontuação continua informada pelo cliente e não é verificável.
3. Não há limite de tentativas de login nem monitoramento no servidor publicado.
4. As telas de menu e ranking não foram adaptadas ao modo retrato.
5. O balanceamento do Infinito (8% por setor, mínimo de 0,25 s) foi escolhido
   sem testes com jogadores.
