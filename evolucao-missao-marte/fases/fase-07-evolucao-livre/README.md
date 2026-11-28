# Missão Marte — Fase 7 (Evolução livre)

Evolução da Fase 6 com o **servidor na nuvem**, jogo em **tempo real**, modo
**Infinito**, **rankings por modo e dificuldade**, vidas com corações, direcional
na tela do celular e um **pacote para a apresentação** que roda sem instalar
nada. Plano aprovado: [`plano-fase-07.md`](../../docs/plano-fase-07.md);
tutorial: [`docs/fase-07-tutorial.md`](docs/fase-07-tutorial.md).
Resumo das novidades: [`RESUMO-FASE-07.md`](RESUMO-FASE-07.md).

> **Para a apresentação:** baixe a Release
> [v7.0-apresentacao](https://github.com/jpedro-lc/Projeto_Arquitetura-Missao_Marte/releases/tag/v7.0-apresentacao)
> (zip do Windows com Java embutido e APK). Passo a passo em [`APRESENTACAO.md`](APRESENTACAO.md).

| Etapa | PR | Conteúdo |
|---|---|---|
| 7.1 | #18 | Servidor no **Render** (Docker + PostgreSQL), `/api/saude`, cliente acorda o servidor |
| 7.2 | #19 | **Tempo real** como padrão; **Clássico** (turnos) mantido |
| 7.3 | #20 | Modo **Infinito** e **9 rankings** (modo × dificuldade, por pontos) |
| — | #21 | Vidas com **corações** desenhados por código |
| — | #22 | **Direcional e Embarcar** translúcidos no celular; **pacote da apresentação** |

## Estrutura

```text
fase-07-evolucao-livre/
├── APRESENTACAO.md   como rodar em outro computador e no celular
├── cliente/          core + lwjgl3 (desktop) + android; Gradle 8.7, LibGDX 1.12.1
├── servidor/         Spring Boot 4.1.2, Gradle 9.8.2; H2 local ou PostgreSQL na nuvem
│   ├── Dockerfile    imagem para o Render
│   └── DEPLOY.md     publicação no Render
└── docs/             tutorial e evidências (tempo-real, infinito, coracoes, direcional)
```

O `render.yaml` (Blueprint do Render) fica na raiz do repositório.

## Modos de jogo

Escolhidos na **Nova partida** (setas cima/baixo, `Tab` ou toque), junto com a
dificuldade.

| | Tempo real (padrão) | Infinito | Clássico |
|---|---|---|---|
| Ritmo | Contagem 3-2-1; tudo anda sozinho | Igual ao Tempo real | Uma jogada por vez (Fase 6) |
| Nave | 1 casa a cada 0,15 s **enquanto a direção estiver pressionada** | Igual | 1 casa por tecla/toque |
| Inimigos | Passo de 0,9 / 0,7 / 0,5 s (Fácil/Médio/Difícil) | Mais rápidos a cada setor (×0,92, mínimo 0,25 s) | A cada jogada |
| Pontos | 30/20/15 iniciais; −1 por segundo | Acumulam; recarga de 30/20/15 a cada setor | −1 por movimento |
| Embarque | Automático (C também vale) | Automático | Tecla C / botão |
| Colisão | −1 vida e 1,5 s de proteção | Igual | −1 vida |
| Fim | Vitória ao voltar à base com todos; derrota sem vidas ou pontos | Setor concluído → próximo setor; a corrida acaba sem vidas ou pontos | Igual à Fase 6 |

**Infinito, a cada setor:**
- +1 inimigo a cada 2 setores;
- +1 asteroide a cada 3 setores;
- o mapa cresce a cada 4 setores, até o raio 7;
- as vidas que sobraram passam adiante, com +1 vida nos setores 6, 11, 16…, até 5.

## Controles

| Ação | Teclado / controle (desktop) | Celular |
|---|---|---|
| Mover | `W A S D`, setas ou D-Pad (segurar no tempo real) | **Direcional em cruz** no canto esquerdo, por cima do mapa e translúcido; segurar mantém a nave andando |
| Embarcar | `C` / botão A | Botão **EMBARCAR** no canto direito |
| Pausa | `P` / Start | Botão Pausa; tecla Voltar; app em segundo plano |
| Abortar | `Q` / botão B | Botão Abortar |

No desktop, o rodapé tem os botões de texto das fases anteriores.
`-Dmissaomarte.toque=true` mostra o layout do celular para testar com o mouse.

## Rankings

São 9 tabelas, uma para cada modo (Tempo real, Infinito, Clássico) e
dificuldade, todas ordenadas por **pontos**. Tempo real e Clássico contam
vitórias. No Infinito conta toda corrida terminada, e a tabela mostra o setor
alcançado. A tela de ranking tem abas, tanto no ranking global (logado) quanto
no local (convidado).

## Servidor

**Publicado em:** `https://missao-marte-servidor.onrender.com`.
- Swagger em `/swagger-ui/index.html`.
- O plano gratuito dorme após 15 min sem uso.
- O banco expira por volta de 27/12/2026.
- Detalhes em [`servidor/DEPLOY.md`](servidor/DEPLOY.md).

| Rota | Fase 7 |
|---|---|
| `GET /api/saude` | Pública; usada pelo Render e para acordar o servidor |
| `POST /api/partidas` | `modo` opcional (padrão `CLASSICO`); `setor` só no Infinito, e obrigatório nele |
| `GET /api/ranking` | Sem parâmetros: igual à Fase 6. `?modo=&dificuldade=`: uma das 9 tabelas |
| `GET /api/partidas/minhas` | Traz `modo` e `setor` |

| Variável | Uso |
|---|---|
| `PORT` | Porta (o Render informa; padrão 8080) |
| `DATABASE_URL` | PostgreSQL (`postgresql://…`); sem ela, H2 em arquivo |
| `MISSAOMARTE_JWT_SEGREDO` | Segredo do token (≥ 32 bytes; no Render, gerado por ele) |
| `MISSAOMARTE_BIND` | Interface (`127.0.0.1` padrão; `0.0.0.0` no contêiner e no Wi-Fi) |

## Como executar pelo código

```powershell
# servidor local
cd servidor
.\gradlew.bat test                      # 41 testes
.\gradlew.bat bootRun

# cliente
cd cliente
.\gradlew.bat clean build               # 132 testes (gera o APK se houver Android SDK)
.\gradlew.bat :lwjgl3:run               # contra localhost:8080
.\gradlew.bat :lwjgl3:run "-Dmissaomarte.servidor=https://missao-marte-servidor.onrender.com"
.\gradlew.bat :android:installDebug "-Pmissaomarte.servidor=https://missao-marte-servidor.onrender.com"
```

Sem `-P`, o APK escolhe sozinho: `10.0.2.2` no emulador e `127.0.0.1` no celular
com `adb reverse tcp:8080 tcp:8080`, como na Fase 6.

**Verificações com janela real:**

| Tarefa | O que faz |
|---|---|
| `:lwjgl3:smokeDesktop` | Modo Clássico, roteiro da Fase 4 |
| `:lwjgl3:smokeTempoReal` | Relógio real: contagem, segurar D/A, embarque automático, pausa, vitória |
| `:lwjgl3:smokeInfinito` | Setor 1 → setor 2 → fim da corrida → aba Infinito do ranking |
| `:lwjgl3:smokeOnline` | Contra o servidor local, 83 passos |
| `:lwjgl3:zipApresentacao` | Gera o zip da apresentação |

## Arquitetura

- **Camadas:** `model` e `service` (regras) não importam `com.badlogic`
  (conferido por busca), e o tempo chega por parâmetro nas regras.
- **Strategy:** `RegrasDePartida` tem três implementações:
  - `RegrasClassicas`, um Adapter do `JogoService` das fases anteriores;
  - `TempoRealService`;
  - `InfinitoService`, que usa o tempo real como motor de cada setor.
- **Port and Adapter:** `EntradaJogo` (agora com `direcaoMantida()`) e
  `ServidorDoJogo` (agora com `acordar()`).
- Detalhes, diagramas e o passo a passo no
  [tutorial](docs/fase-07-tutorial.md).

## Limitações

- O PostgreSQL gratuito do Render expira (cerca de 27/12/2026).
- A pontuação é informada pelo cliente.
- O servidor não limita tentativas de login.
- Não foi testado: multitoque real (seta + Embarcar ao mesmo tempo), outro
  computador com o pacote, Docker local e controle Bluetooth no tempo real.
