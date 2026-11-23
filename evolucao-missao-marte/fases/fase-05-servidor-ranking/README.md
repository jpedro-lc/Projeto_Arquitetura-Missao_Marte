# Missão Marte — Fase 5 (Servidor e ranking global)

O jogo LibGDX da Fase 4 agora conversa por **API REST** com um servidor
**Spring Boot independente**, que oferece cadastro, login (JWT), histórico de
partidas e ranking global (Top 10).

- **Logado:** cada vitória ou derrota é enviada ao servidor.
- **Convidado** (sem entrar): o jogo funciona como na Fase 4, com o ranking local.

> **Divergência registrada:** o prompt mestre pede login obrigatório. Seguimos o
> enunciado do professor, que mantém o modo convidado. Veja o
> [plano aprovado](../../docs/plano-fase-05.md).

## Estrutura

```text
fase-05-servidor-ranking/
├── cliente/     Fase 4 evoluída: core + lwjgl3, Gradle 8.7, LibGDX 1.12.1
├── servidor/    Spring Boot 4.1.2, Gradle 9.8.2, Java 17, H2 em arquivo
│   └── requests.http   chamadas de exemplo (REST Client / IntelliJ HTTP)
└── docs/        tutorial e evidências (capturas)
```

São dois builds Gradle independentes, cada um com seu wrapper. O Boot 4.1
exige Gradle 8.14+ ou 9.x, e o servidor não depende do código do jogo.

## Requisitos

- JDK 17 em `JAVA_HOME` **e** primeiro no `PATH`. Nesta máquina:
  `~/.jdks/openjdk-17.0.2`.
- Não é preciso instalar Gradle nem banco: os wrappers baixam o Gradle e o H2
  roda dentro do servidor.

## Como executar

Terminal 1, **servidor** (porta 8080, aceita só conexões da própria máquina):

```bash
cd servidor
./gradlew test                      # 27 testes
./gradlew bootRun --console=plain
```

Terminal 2, **cliente**:

```bash
cd cliente
./gradlew clean build               # 70 testes
./gradlew :lwjgl3:run --console=plain
```

No PowerShell, use `.\gradlew.bat`. Com o servidor desligado, o jogo continua
em modo convidado.

| Onde | Opção | Efeito |
|---|---|---|
| Cliente | `-Dmissaomarte.servidor=http://localhost:8080` | Endereço do servidor (este é o padrão) |
| Servidor | variável `MISSAOMARTE_JWT_SEGREDO` | Segredo do token (≥ 32 bytes). Sem ela, um segredo aleatório é gerado a cada início e os tokens deixam de valer ao reiniciar |
| Servidor | variável `MISSAOMARTE_DADOS` | Pasta do banco H2 (padrão `servidor/dados`, ignorada pelo Git) |

## Telas e controles

| Tela | Convidado | Logado |
|---|---|---|
| Menu | Iniciar, Ranking local, Limpar ranking local, Entrar ou criar conta, Instruções, Sair | Iniciar, Ranking global, Meu histórico, Sair da conta, Instruções, Sair |
| Nova partida | Digita o nome; setas mudam a dificuldade; Enter decola | Usa o nome da conta; só escolhe a dificuldade |
| Fim de partida | Vitória vai para o ranking local | Envia vitória e derrota; mostra a posição no Top 10; se falhar, R ou "Tentar de novo" |
| Ranking e histórico | Ranking local | Global e histórico (últimas 10), com carregando, pronto e erro; R recarrega após erro |

- **Entrar/Criar conta:** a senha aparece com asteriscos. Tab ou setas trocam
  o campo, F2 alterna entre entrar e criar conta, Enter confirma e Esc volta.
  Ao criar a conta, o jogo entra automaticamente.
- **Partida:** os controles são os da Fase 4.
- **Sessão expirada (401):** o jogo volta ao modo convidado e avisa.

## API

| Método e rota | Token | Resposta |
|---|---|---|
| `POST /api/usuarios` | não | 201 `{id, nome}`, 409 (nome existe, sem diferenciar caixa) ou 400 |
| `POST /api/auth/login` | não | 200 `{token, tipo, expiraEm}` ou 401 genérico |
| `POST /api/partidas` | sim | 201; o dono vem do token |
| `GET /api/partidas/minhas?pagina=0&tamanho=10` | sim | Histórico, mais recente primeiro (tamanho ≤ 50) |
| `GET /api/ranking` | sim | Top 10: a melhor vitória (pontos > 0) de cada jogador; empate por menor tempo e depois pela mais antiga |

Erros saem no formato `ProblemDetail`. A documentação interativa fica em
<http://localhost:8080/swagger-ui.html>: crie a conta, faça login, clique em
**Authorize** e cole o token.

## Validação (22/11/2026)

- Servidor com 27 testes MockMvc e cliente com 70 testes JUnit (48 da Fase 4 + 22 novos).
- `smokeDesktop` (convidado) e `smokeOnline`, com 82 passos contra o servidor
  real, passaram e geraram 14 [capturas](docs/evidencias/).
- `model` e `service` têm o mesmo SHA-256 da Fase 4. Nenhum `com.badlogic`
  aparece em `model`, `service`, `repository` ou `remoto`.
- O log do servidor não contém senhas nem tokens.

Detalhes no [tutorial](docs/fase-05-tutorial.md).

## Limitações conhecidas

- O servidor **não executa a lógica do jogo**: a pontuação vem do cliente e só
  é validada por faixa.
- Sem HTTPS, sem limite de tentativas de login, sem renovação ou revogação de
  token e sem fila de reenvio offline.
- O ranking global não é separado por dificuldade.
- O console (`:lwjgl3:runConsole`) continua offline, com o ranking local.
