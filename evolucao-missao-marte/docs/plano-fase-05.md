# Plano aprovado da Fase 5 — Servidor e ranking global

**Autorização:** em 22/11/2026, o usuário respondeu “então aprovo o plano da fase 5”, após escolher seguir o enunciado do professor.

**Origem:** `fases/fase-04-interface-grafica/` (concluída, PRs #13 e #14 integrados).
**Destino:** `fases/fase-05-servidor-ranking/`.

**Orientação seguida:** o enunciado do professor para esta fase
(`T200-ProjArqSist/.../fase-05-servidor-ranking/docs/fase-05-enunciado.md`).
Por instrução do usuário em 22/11/2026, onde ele diverge do prompt mestre,
**o enunciado prevalece** e a divergência fica registrada (seção 9).

**Referencial:** o `tutorial-api-gradle-libgdx.md` não cobre servidor, JPA nem
autenticação. Uso a documentação oficial do Spring Boot, Spring Security,
Spring Data JPA, H2, OWASP (armazenamento de senhas) e a wiki do libGDX
(Networking e Scene2D), citada no tutorial da fase.

## 1. Diagnóstico do estado de origem

- O ranking é local: `RankingArquivoRepository` (lwjgl3) grava um JSON na
  máquina. Qualquer um pode editá-lo, cada computador tem o seu e não existe
  usuário.
- `JogoService` chama `ranking.salvar(...)` só na vitória. Derrotas não ficam
  registradas.
- A janela joga como "Piloto Anônimo", em dificuldade MÉDIO fixa. Não há tela
  de nova partida, ranking ou login (o ranking só aparece no console).
- `model`, `service` e `repository` não importam LibGDX. A apresentação consome
  apenas `EstadoPartida` e `ResultadoAcao`.
- 48 casos JUnit passam (incluindo as 162 verificações herdadas).
- Ambiente (22/11/2026): JDK 17.0.2 em `~/.jdks`. Não há Docker nem PostgreSQL,
  e o H2 em arquivo dispensa instalação.

## 2. Estrutura

```text
fase-05-servidor-ranking/
├── README.md
├── docs/fase-05-tutorial.md
├── cliente/      # Fase 4 evoluída: core + lwjgl3, wrapper Gradle 8.7, LibGDX 1.12.1
│   └── .gitignore  (build, .gradle, .idea, ranking local)
└── servidor/     # Spring Boot 4.1.2, wrapper Gradle 9.8.2, Java 17
    ├── .gitignore  (build, .gradle, .idea, dados)
    ├── requests.http
    └── src/main/java/br/unifor/missaomarte/servidor/
        ├── usuario/   auth/   partida/   ranking/   # controller, serviço, repositório, DTOs
        ├── seguranca/                               # BCrypt, JWT, regras de acesso, OpenAPI
        └── erro/                                    # ProblemDetail (RFC 9457)
```

São **dois builds Gradle independentes**, cada um com seu wrapper. O Spring
Boot 4.1 exige Gradle 8.14+ ou 9.x, e o cliente continua no 8.7 desde a Fase 2.
O servidor **não depende do código do cliente**: tem sua própria cópia do enum
de dificuldade, e o contrato entre os dois é HTTP/JSON.

## 3. Servidor

**Entidades JPA** (nunca saem pela API; as respostas usam DTOs `record`)
- `Usuario`: id, nome, nomeNormalizado (minúsculas, **índice único no banco**),
  senhaHash e criadoEm.
- `Partida`: id, usuario, dificuldade, resultado (`VITORIA`/`DERROTA`),
  pontuacao, duracaoSegundos, movimentos e registradaEm.

**API**

| Método e rota | Token | Resposta |
|---|---|---|
| `POST /api/usuarios` | não | 201 `{id, nome}`; 409 se o nome existe (qualquer caixa); 400 se inválido |
| `POST /api/auth/login` | não | 200 `{token, tipo: "Bearer", expiraEm}`; 401 genérico |
| `POST /api/partidas` | sim | 201 com a partida; o usuário vem **do token** (campo `usuario` no corpo é ignorado) |
| `GET /api/partidas/minhas?pagina=&tamanho=` | sim | Histórico do dono do token, mais recente primeiro, `tamanho` ≤ 50 |
| `GET /api/ranking` | sim | Top 10 (regra abaixo) |

**Ranking:** uma linha por jogador com a **melhor vitória**, e só entram
vitórias com pontuação > 0. Ordem: maior pontuação, depois menor duração,
depois a partida mais antiga. Limite de 10. A melhor partida de cada jogador
sai de uma subconsulta JPQL.

**Validação:**
- Nome: 3 a 20 caracteres (letras, números, `_` e `-`).
- Senha: 8 caracteres e ≤ 72 bytes.
- Enums válidos.
- Pontuação: -100 000 a 100 000.
- Duração: 0 a 86 400 s.
- Movimentos: 0 a 100 000.
- Campos ausentes geram 400, com `Integer`/`Long` e `@NotNull`.

Os erros saem como `ProblemDetail`, com a lista de campos inválidos.

**Segurança**
1. Senhas com BCrypt de custo ≥ 10, com sal diferente por conta. Nunca em
   texto, log, `toString` ou resposta.
2. JWT HS256 com `sub` = id do usuário e validade de 2 h, assinado e
   validado pelo resource server do Spring Security (`JwtEncoder`/`JwtDecoder`
   com a mesma chave).
3. O segredo vem de `MISSAOMARTE_JWT_SEGREDO` (≥ 32 bytes) e não entra no
   repositório. Sem a variável, o servidor gera um segredo aleatório e avisa no
   log.
4. **Tudo protegido por padrão.** Só cadastro, login, `/error` e a leitura do
   Swagger ficam liberados. API sem estado: sem sessão nem cookie, por isso o
   CSRF fica desligado.
5. Login com a **mesma resposta** para usuário inexistente e senha errada. Para
   o tempo de resposta não revelar quem existe, a senha é comparada com um hash
   falso quando o nome não existe.
6. O servidor escuta só em `127.0.0.1:8080`.

**Banco:** H2 em arquivo (`servidor/dados/`, pasta trocável por
`MISSAOMARTE_DADOS`), com `ddl-auto=update`. O tutorial explica por que isso
não serve para produção (Flyway). Os testes usam H2 em memória e propriedades
de teste com segredo fixo.

**Extras pedidos ou sugeridos pelo enunciado:**
- `requests.http` com chamadas de exemplo.
- Swagger UI (springdoc-openapi 3.2.x) com esquema `bearer`. Só a leitura da
  página é pública, e um teste confirma que as rotas seguem exigindo token.

**O servidor não executa a lógica do jogo.** A pontuação enviada pelo cliente
não pode ser provada; isso fica registrado como limitação.

## 4. Cliente LibGDX

**Camadas:** `model`, `service`, `repository` e o novo pacote `remoto`
continuam **sem `com.badlogic`**. O LibGDX fica em `presentation`, `screen`,
`MissaoMarteGame` e `lwjgl3`. `model` e `service` ficam **idênticos** aos da
Fase 4 (conferido por hash).

**Porta e adaptador** (Port and Adapter)
- `remoto/ServidorDoJogo` (porta): cadastrar, entrar, enviarPartida,
  carregarRanking e carregarHistorico. Todas assíncronas, com callback.
- Tipos de apoio sem LibGDX:
  - `Resposta`: valor **ou** mensagem de erro, com marca de "não autorizado".
  - `Retorno`: o callback.
  - `Sessao`: nome e token, só em memória, e o `toString` não mostra o token.
  - `PartidaConcluida`: montada a partir do `EstadoPartida` final; erro se a
    partida não terminou.
  - `PartidaRegistrada` e `LinhaRanking`.
- `presentation/rede/ServidorHttp` (adaptador):
  - Usa `Gdx.net.sendHttpRequest` com tempo limite.
  - Envia `Content-Type: application/json` e `Authorization: Bearer`.
  - Devolve **todo** resultado na thread de desenho via `Gdx.app.postRunnable`.
- `presentation/rede/JsonDoServidor`:
  - Só converte JSON, com `JsonWriter`/`JsonReader` do LibGDX, sem rede nem
    OpenGL.
  - Nunca concatena texto: uma senha `a"b\c` não quebra o JSON.
  - Extrai a mensagem de erro do `ProblemDetail` (primeiro erro de campo ou
    `detail`) ou usa um texto padrão por código. Nunca mostra o corpo bruto.
- O endereço vem de `-Dmissaomarte.servidor=` (padrão `http://localhost:8080`)
  e é montado no `Lwjgl3Launcher`.

**Modos** (decididos pela existência de uma `Sessao`)

| | Convidado | Logado |
|---|---|---|
| Menu | Iniciar, Ranking local, Limpar ranking local, Entrar ou criar conta, Instruções, Sair | Iniciar, Ranking global, Meu histórico, Sair da conta, Instruções, Sair |
| Nova partida | Digita o nome e escolhe a dificuldade | Usa o nome da conta; só escolhe a dificuldade |
| Fim de partida | Grava no ranking local (só vitória, como hoje) | Envia vitória **e** derrota ao servidor; o aborto não é enviado |
| Ranking | Local, síncrono | Global, assíncrono, indicando se o jogador está no Top 10 |

**Telas novas** (Scene2D, no estilo SpaceRage da Fase 4)
- **Entrar/Criar conta:**
  - Campos de nome e senha, com a senha mascarada.
  - Tab ou setas trocam de campo, F2 alterna entre entrar e criar conta, Enter
    confirma e Esc volta.
  - Ao criar a conta, o jogo entra automaticamente.
  - A regra do formulário (caracteres aceitos, limites, máscara e validação)
    fica em `FormularioLogin`, sem LibGDX e testável.
- **Nova partida:** nome (só para o convidado) e dificuldade.
- **Ranking** (local ou global) e **Meu histórico** (últimas 10 partidas com
  data e hora, resultado, dificuldade, pontuação, tempo e movimentos).
  - Estados visíveis: carregando, pronto e erro com "R tenta de novo". Nunca
    uma tela em branco.
- **Fim de partida logado:**
  - Mostra "Enviando…", "Resultado enviado" ou a falha com o motivo.
  - Os botões ficam desativados durante o envio, e R ou um botão permite tentar
    de novo.
- **Erros de rede e de sessão:**
  - Uma resposta 401 descarta a sessão, avisa e volta ao modo convidado.
  - Com o servidor desligado, aparece "Não foi possível conectar", sem
    exceção, e o modo convidado continua jogável.

**Ranking local só para convidado:**
- `JogoService` e `RankingRepository` não mudam.
- O launcher envolve o `RankingArquivoRepository` num **decorador**
  (`RankingSomenteConvidado`) que ignora a gravação quando há sessão.
- O envio ao servidor é feito pela apresentação, que já observa os resultados
  (mesmo mecanismo de áudio e efeitos).
- O console (`runConsole`) continua offline e inalterado.

## 5. Padrões e princípios

- **Port and Adapter:** `ServidorDoJogo` é a porta e `ServidorHttp` o
  adaptador. Os testes usam conversões puras, sem rede.
- **Repository** (Spring Data), **DTO** (records) e camadas controller → serviço
  → repositório no servidor.
- **Callback** (`Retorno`) e **State** (carregando/pronto/erro, convidado/logado).
- **Decorator** (`RankingSomenteConvidado`) e injeção de dependência pelo
  launcher e pelo Spring.
- **SRP/DIP/GRASP:** o servidor é o *Information Expert* do ranking; o cliente
  depende da abstração, não do HTTP.

O tutorial vai separar o que segue o padrão de fato do que apenas se parece
com ele, como o enunciado pede.

## 6. Versões

| Item | Versão | Observação |
|---|---|---|
| JDK | 17 | Nos dois aplicativos (`JAVA_HOME` e `PATH`) |
| Cliente | Gradle 8.7, LibGDX 1.12.1, JUnit 5.10.2 | Sem mudança; nenhuma dependência nova (rede e JSON vêm do LibGDX) |
| Servidor | Spring Boot **4.1.2**, Gradle wrapper **9.8.2**, `io.spring.dependency-management` 1.1.7 | Versões do enunciado. Já existe a 4.1.3 (correção da mesma linha), mas sigo o enunciado |
| Starters Boot 4 | `webmvc`, `data-jpa`, `security`, `security-oauth2-resource-server` e `validation`; testes com `webmvc-test`, `data-jpa-test` e `security-test` | Nomes novos do Boot 4 |
| Banco | H2 (versão do BOM do Boot) | Embutido |
| Swagger | springdoc-openapi-starter-webmvc-ui 3.2.x | Opcional no enunciado; incluído |

## 7. Testes e validação

**Servidor** (MockMvc com `@AutoConfigureMockMvc` e H2 em memória; cada teste
desfeito ao final):
- **Cadastro:** 201 sem senha na resposta; hash BCrypt no banco, com sal
  diferente para senhas iguais; 409 para o mesmo nome em outra caixa; 400 para
  dados inválidos; limite de 72 bytes.
- **Login:** sucesso; resposta idêntica para senha errada e para usuário
  inexistente.
- **Proteção:** 401 sem token, com token inválido e com token **expirado**;
  200 com token válido.
- **Partidas:** usuário do corpo ignorado; 400 para enum inválido ou valor fora
  da faixa; histórico só do dono, em ordem e paginado.
- **Ranking:** vazio; uma linha por jogador; derrotas e pontuação ≤ 0 ficam de
  fora; desempates por duração e antiguidade; limite de 10; sem dados sensíveis.
- **Swagger:** leitura pública e API ainda protegida.

**Cliente** (sem janela, OpenGL ou rede):
- Os 48 casos herdados continuam passando.
- Testes novos:
  - JSON: credenciais com aspas e barras, números negativos, token ausente,
    listas vazias.
  - Mensagens de erro: campo, `detail`, 401, 5xx e corpo que não é JSON.
  - `FormularioLogin`.
  - `PartidaConcluida`/`Sessao`/`Resposta`.
  - Formatação da data do histórico.
  - Decorador de ranking.
- O smoke gráfico continua passando com um servidor falso.

**Roteiro ponta a ponta** (os 16 passos da seção 9 do enunciado), com o
servidor real em `bootRun`:
- Testar a API via `curl`/`requests.http`.
- Jogar como convidado e logado com dois jogadores.
- Cenários de derrota, senha errada, servidor desligado com reenvio por R e
  sair da conta.
- Busca confirmando `com.badlogic` só nas camadas gráficas.
- **Hash** de `model`/`service` igual ao da Fase 4.
- `git status` sem alterações nas fases anteriores.
- **Capturas de tela** de entrada, cadastro, menu logado, fim de partida com
  envio, ranking global, histórico, senha errada e servidor desligado,
  geradas por um smoke contra o servidor real.

**Riscos**

| Risco | Mitigação |
|---|---|
| Pontuação falsificada pelo cliente | Limitação assumida (o servidor não joga). Só limites de faixa; a defesa real é exercício para a Fase 7 |
| Travar o desenho esperando a rede | Chamadas assíncronas com `postRunnable`; o render nunca espera a resposta |
| Partida enviada duas vezes | Envio controlado por estado; teste de encerrar, reiniciar e voltar ao menu |
| Senha ou token em log ou tela | `toString` seguro, nenhuma impressão do token, teste de resposta sem hash |
| Regras alteradas | Hash de `model`/`service` e as 162 verificações herdadas |
| Dois servidores no mesmo banco H2 | Documentado (arquivo bloqueado); testes não usam o arquivo real |

## 8. Entregas

- Pasta da fase com `cliente/` e `servidor/` (wrapper e `.gitignore` em cada).
- Testes JUnit 5 passando nos dois.
- `README.md` da fase com execução, controles, opções e API.
- `requests.http`.
- `docs/fase-05-tutorial.md` com os 12 itens do prompt mestre e as seções 8.1
  a 8.7 do enunciado: origem e destino, conceitos (REST, códigos HTTP, DTO, JPA,
  hash/sal/custo, BCrypt × Argon2id, JWT, `postRunnable`, Port and Adapter),
  segurança, decisões, passo a passo com problemas reais, evidências e
  próximos passos.
- Índice da evolução atualizado.
- Branch `fase-05-servidor-ranking` e PR, sem merge direto.

## 9. Divergências registradas

| Tema | Prompt mestre / nossa base | Decisão (enunciado prevalece) |
|---|---|---|
| Login | "Login obrigatório para jogar" | **Modo convidado mantido**: joga sem login com ranking local; com login, ranking global |
| Partida abortada | — | Não é enviada; o servidor só conhece VITORIA/DERROTA |
| Camadas | Enunciado: `domain`/`application`/`infrastructure`/`gdx` | Equivalentes na nossa base: `model`+`service` / `remoto`+`repository` / `lwjgl3` / `presentation`+`screen` |
| Namespace | Enunciado: `br.edu.unifor.missaomarte` | Mantido `br.unifor.missaomarte` (aprovado na Fase 3); servidor em `br.unifor.missaomarte.servidor` |
| Menu | Enunciado: teclas numéricas 1–5 | Botões Scene2D da Fase 4 (mouse), com Enter/Esc nos formulários |
| Nova partida | A Fase 4 não tinha tela de nome/dificuldade | Tela criada para cumprir "convidado digita o nome; logado escolhe a dificuldade" |
| Testes herdados | Enunciado cita 66 | Nossa Fase 4 tem 48 |
| `.idea/` na Fase 4 | Entrou por engano no commit 1d36791 | **Não altero a Fase 4** (o enunciado proíbe); a Fase 5 ignora `.idea/` |
| Spring Boot | 4.1.3 disponível | 4.1.2, conforme o enunciado |
