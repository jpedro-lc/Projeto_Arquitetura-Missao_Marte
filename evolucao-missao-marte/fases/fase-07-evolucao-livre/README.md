# Missão Marte — Fase 7 (Evolução livre)

> **Em andamento.** Plano aprovado em [`docs/plano-fase-07.md`](../../docs/plano-fase-07.md),
> entregue em três etapas, cada uma com seu pull request:
>
> | Etapa | Conteúdo | Situação |
> |---|---|---|
> | 7.1 | Servidor publicado no **Render** (Docker + PostgreSQL) | No ar em `https://missao-marte-servidor.onrender.com` ([`servidor/DEPLOY.md`](servidor/DEPLOY.md)) |
> | 7.2 | Jogo em **tempo real** (modo clássico por turnos mantido) | Pronta (ver abaixo) |
> | 7.3 | Modo **Infinito** e rankings por modo e dificuldade | Pronta (ver abaixo) |
>
> O restante deste README descreve a base herdada da Fase 6 e será reescrito ao
> fim da fase.

> **Para a apresentação:** jogo pronto para outro Windows (Java embutido, sem
> instalar nada) e APK apontando para a nuvem: veja [`APRESENTACAO.md`](APRESENTACAO.md).

**Novidades da etapa 7.1:**
- `GET /api/saude` público (verificação de saúde do Render e "acordar" o servidor).
- Porta lida de `PORT`; banco PostgreSQL lido de `DATABASE_URL` (`UrlDoBanco`
  converte para JDBC). Sem `DATABASE_URL`, continua o H2 em arquivo.
- `servidor/Dockerfile` e `render.yaml` (na raiz do repositório) com o
  serviço web gratuito e o banco gratuito.
- O jogo acorda o servidor ao abrir e espera até 70 s pela resposta
  (antes 8 s), com o aviso "Conectando... pode levar até 1 min." no login.

**Novidades da etapa 7.2 (tempo real):**

| | Tempo real (padrão) | Clássico (turnos, igual à Fase 6) |
|---|---|---|
| Início | Contagem 3, 2, 1, VAI! | Imediato |
| Nave | 1 casa a cada 0,15 s **enquanto a direção estiver pressionada** (teclado, D-Pad ou botão na tela) | 1 casa por toque/tecla |
| Inimigos | Andam sozinhos: 0,9 s / 0,7 s / 0,5 s por passo (Fácil/Médio/Difícil) | A cada jogada |
| Pontos | −1 por segundo | −1 por movimento |
| Embarque | Automático ao passar pelo passageiro (a tecla C continua valendo) | Tecla C / botão Embarcar |
| Colisão | −1 vida e 1,5 s de proteção | −1 vida |

- O modo é escolhido na **Nova partida** (setas cima/baixo ou `Tab`, ou toque).
- Regras em `service/TempoRealService` (sem `com.badlogic`), com o tempo do
  quadro recebido por parâmetro e limitado a 0,1 s; `RitmoTempoReal` guarda os
  valores de balanceamento.
- **Strategy:** `RegrasDePartida` é o contrato dos modos; `RegrasClassicas`
  adapta o `JogoService` das fases anteriores, que só ganhou o getter
  `ranking()` para o tempo real gravar no mesmo ranking local.
- Pausa (botão, `P` ou app em segundo plano) congela o tempo real.
- Desde a etapa 7.3, o servidor recebe o modo e cada modo tem seus rankings.
- Os roteiros `smokeDesktop` e `smokeOnline` agora escolhem **Clássico** antes
  de decolar (o padrão mudou) e continuam verificando o modo por turnos.
- Verificação com janela e relógio reais: `.\gradlew.bat :lwjgl3:smokeTempoReal`
  (capturas em [`docs/evidencias/tempo-real/`](docs/evidencias/tempo-real/)).

**Novidades da etapa 7.3 (Infinito e rankings):**

- **Modo Infinito** (tempo real): o jogador escolhe a dificuldade e joga setores
  em sequência. Concluir um setor (todos os passageiros e volta à base) leva ao
  próximo, com nova contagem; pontos e vidas passam adiante.

  | A cada setor *n* | Regra (`ProgressaoInfinita`) |
  |---|---|
  | Inimigos | +1 a cada 2 setores |
  | Asteroides | +1 a cada 3 setores |
  | Passo dos inimigos | × 0,92 por setor, mínimo 0,25 s |
  | Mapa | +1 casa de raio a cada 4 setores, até 7 |
  | Pontos | recarga de 30/20/15 (Fácil/Médio/Difícil) ao entrar no setor |
  | Vidas | as que sobraram; +1 nos setores 6, 11, 16... (máximo 5) |

  A corrida termina quando acabam as vidas ou os pontos; o resultado guardado é
  a **pontuação** e o **setor alcançado**. Abortar não registra, como nos outros modos.
- **Rankings:** 9 tabelas, uma para cada **modo** (Tempo real, Infinito,
  Clássico) e **dificuldade**, sempre ordenadas por **pontos** (decisão D3).
  Tempo real e Clássico contam vitórias; no Infinito conta toda corrida.
  A tela de ranking tem abas (setas também trocam); o local do convidado segue
  as mesmas abas.
- **Servidor:** `Partida` ganhou `modo` e `setor`; `GET /api/ranking?modo=…&dificuldade=…`.
  Sem parâmetros, devolve o ranking antigo (contrato das Fases 5 e 6).
  Requisição sem `modo` vale `CLASSICO`; `setor` só é aceito (e é obrigatório)
  no Infinito. Partidas gravadas antes ficam como `CLASSICO` (`MigracaoModo`).
- Histórico mostra o modo (com o setor no Infinito).
- Verificação com janela real: `.\gradlew.bat :lwjgl3:smokeInfinito`
  (capturas em [`docs/evidencias/infinito/`](docs/evidencias/infinito/)).

**Vidas com corações:** o HUD troca as mini-naves por corações pixel-art
desenhados por código (`DesenhoCoracao`, sem imagem de terceiros). Cheios são
vidas; vazios, as perdidas. Aparecem até 5 (o máximo do Infinito), o que também
corrige o HUD antigo, que só tinha 3 espaços. Capturas em
[`docs/evidencias/coracoes/`](docs/evidencias/coracoes/).

## Base herdada da Fase 6

O jogo LibGDX evoluiu para um ecossistema **multiplataforma (Desktop e Android)**
reutilizando o mesmo módulo compartilhado (`:core`) e conectado por **API REST**
ao servidor **Spring Boot independente** (cadastro, login JWT, histórico de
partidas e ranking global Top 10).

- **Logado:** cada vitória ou derrota é enviada ao servidor (tanto no Desktop quanto no Android).
- **Convidado** (sem entrar): o jogo funciona offline com o ranking local (`ranking-solid-exercicio10.json` no Desktop ou `getFilesDir()` no Android).
- **Controles multiplataforma (Fase 6):** suporte simultâneo a **toque nos botões da tela**, **gestos de deslize (*swipe*) no mapa**, **teclado (`WASD`)** e **controle Bluetooth (D-Pad e botões A/B/Y/Start)**.

> **Divergência registrada:** o prompt mestre pede login obrigatório. Seguimos o
> enunciado do professor, que mantém o modo convidado.

## Estrutura

```text
fase-07-evolucao-livre/
├── cliente/     Core + Desktop (:lwjgl3) + Android (:android), Gradle 8.7, LibGDX 1.12.1
│   ├── core/    Regras (model/service intactos), telas Scene2D, toque/swipe e cliente HTTP
│   ├── lwjgl3/  Launcher desktop, console preservado e smoke tests gráficos
│   └── android/ AndroidLauncher, AndroidManifest.xml, network_security_config e natives
├── servidor/    Spring Boot 4.1.2, Gradle 9.8.2, Java 17, H2 local ou PostgreSQL na nuvem
│   ├── Dockerfile      imagem para o Render
│   ├── DEPLOY.md       passo a passo da publicação
│   └── requests.http   chamadas de exemplo (REST Client / IntelliJ HTTP)
└── docs/        tutorial e evidências da Fase 7
```

São dois builds Gradle independentes (`cliente/` e `servidor/`), cada um com seu
wrapper. O módulo `:android` é incluído automaticamente pelo Gradle quando o
Android SDK está configurado na máquina (`ANDROID_HOME`, `ANDROID_SDK_ROOT` ou
`cliente/local.properties`), mantendo o build desktop 100% funcional mesmo em
máquinas sem Android SDK.

## Como executar

### 1. Servidor (Terminal 1)

```powershell
cd servidor
.\gradlew.bat test                      # 41 testes
.\gradlew.bat bootRun --console=plain
```

- Por padrão escuta em `127.0.0.1:8080` (acessível pelo Desktop em `localhost:8080`, pelo celular USB com `adb reverse tcp:8080 tcp:8080` em `127.0.0.1:8080` e pelo Emulador Android em `10.0.2.2:8080`).
- Para jogar em um **celular físico via Wi-Fi (sem cabo USB)**, inicie o servidor escutando na rede local:
  ```powershell
  $env:MISSAOMARTE_BIND="0.0.0.0"; .\gradlew.bat bootRun --console=plain
  ```

### 2. Cliente Desktop (Terminal 2)

```powershell
cd cliente
.\gradlew.bat test                      # 129 testes JUnit (core + lwjgl3)
.\gradlew.bat :lwjgl3:run --console=plain
```

### 3. Cliente Android (Dispositivo Físico via USB/Wi-Fi ou Emulador)

Requer Android SDK (`compileSdk 34`) configurado em `ANDROID_HOME` ou `cliente/local.properties`:

```powershell
cd cliente
# 1. Celular físico via cabo USB (redireciona a porta 8080 pelo cabo):
adb reverse tcp:8080 tcp:8080
.\gradlew.bat :android:assembleDebug
.\gradlew.bat :android:installDebug

# 2. Emulador Android: o mesmo APK detecta o emulador e usa 10.0.2.2 (localhost da máquina):
.\gradlew.bat :android:installDebug

# 3. Celular físico na mesma rede Wi-Fi (substitua pelo IP da máquina):
.\gradlew.bat :android:assembleDebug -Pmissaomarte.servidor=http://192.168.1.15:8080
```

| Onde | Opção | Efeito |
|---|---|---|
| Cliente Desktop | `-Dmissaomarte.servidor=http://localhost:8080` | Endereço do servidor no desktop (padrão) |
| Cliente Android | `-Pmissaomarte.servidor=http://IP:8080` | Força o endereço do servidor no APK. Sem a opção, é automático: `10.0.2.2:8080` no emulador e `127.0.0.1:8080` no celular (com `adb reverse`) |
| Servidor | variável `MISSAOMARTE_BIND` | Interface de rede (`127.0.0.1` padrão; `0.0.0.0` para LAN/Wi-Fi) |
| Servidor | variável `MISSAOMARTE_JWT_SEGREDO` | Segredo do token (≥ 32 bytes) |
| Servidor | variável `MISSAOMARTE_DADOS` | Pasta do banco H2 (padrão `servidor/dados`) |

## Telas e controles (Desktop, Toque e Bluetooth)

| Ação na Partida | Teclado | Toque na Tela (Android / Mouse) | Controle Bluetooth / Gamepad |
|---|---|---|---|
| Mover nave | `W`, `A`, `S`, `D` ou Setas | Botões `ESQ`, `CIMA`, `BAIXO`, `DIR` ou **deslizar (swipe)** no mapa | `D-Pad` (Cima, Baixo, Esquerda, Direita) |
| Embarcar passageiro | `C` | Botão `EMBARCAR (C)` | Botão `A` |
| Abortar missão | `Q` | Botão `ABORTAR (Q)` | Botão `B` |
| Pausar / Continuar | `P` ou `ESC` | Botão `PAUSA (P)` ou botão Voltar do Android | Botão `START` ou `BACK` |
| Reiniciar após fim | `R` | Botão `REINICIAR` | Botão `Y` |

- **Entrar/Criar conta e Nova Partida:** no Android, tocar nas caixas de texto abre automaticamente o teclado virtual (*soft keyboard*), e o botão Voltar retorna ao menu.

## Documentação

- [Plano aprovado da Fase 7](../../docs/plano-fase-07.md)
- [Publicar o servidor no Render](servidor/DEPLOY.md)
- Base: [README](../fase-06-android/README.md) e [tutorial](../fase-06-android/docs/fase-06-tutorial.md) da Fase 6
- O tutorial da Fase 7 (12 seções) será escrito ao fim da fase.
