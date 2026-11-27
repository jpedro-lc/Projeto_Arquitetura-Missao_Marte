# Missão Marte — Fase 7 (Evolução livre)

> **Em andamento.** Plano aprovado em [`docs/plano-fase-07.md`](../../docs/plano-fase-07.md),
> entregue em três etapas, cada uma com seu pull request:
>
> | Etapa | Conteúdo | Situação |
> |---|---|---|
> | 7.1 | Servidor publicado no **Render** (Docker + PostgreSQL) | Pronta para o deploy ([`servidor/DEPLOY.md`](servidor/DEPLOY.md)) |
> | 7.2 | Jogo em **tempo real** (modo clássico por turnos mantido) | Não iniciada |
> | 7.3 | Modo **Infinito** e rankings por modo e dificuldade | Não iniciada |
>
> O restante deste README descreve a base herdada da Fase 6 e será reescrito ao
> fim da fase.

**Novidades da etapa 7.1:**
- `GET /api/saude` público (verificação de saúde do Render e "acordar" o servidor).
- Porta lida de `PORT`; banco PostgreSQL lido de `DATABASE_URL` (`UrlDoBanco`
  converte para JDBC). Sem `DATABASE_URL`, continua o H2 em arquivo.
- `servidor/Dockerfile` e `render.yaml` (na raiz do repositório) com o
  serviço web gratuito e o banco gratuito.
- O jogo acorda o servidor ao abrir e espera até 70 s pela resposta
  (antes 8 s), com o aviso "Conectando... pode levar até 1 min." no login.

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
.\gradlew.bat test                      # 33 testes
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
.\gradlew.bat test                      # 84 testes JUnit (core + lwjgl3)
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
