# Missão Marte — Fase 6 (Android)

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
fase-06-android/
├── cliente/     Core + Desktop (:lwjgl3) + Android (:android), Gradle 8.7, LibGDX 1.12.1
│   ├── core/    Regras (model/service intactos), telas Scene2D, toque/swipe e cliente HTTP
│   ├── lwjgl3/  Launcher desktop, console preservado e smoke tests gráficos
│   └── android/ AndroidLauncher, AndroidManifest.xml, network_security_config e natives
├── servidor/    Spring Boot 4.1.2, Gradle 9.8.2, Java 17, H2 em arquivo
│   └── requests.http   chamadas de exemplo (REST Client / IntelliJ HTTP)
└── docs/        tutorial (fase-06-tutorial.md) e evidências (capturas)
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
.\gradlew.bat test                      # 27 testes
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
.\gradlew.bat test                      # 83 testes JUnit (core + lwjgl3)
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

## Documentação Didática

- [Tutorial da Fase 6 — Adaptação Multiplataforma para Android](docs/fase-06-tutorial.md)
- [Plano e registro da Fase 6](../../docs/plano-fase-06.md)

## Pendências

- **Capturas no Android:** as evidências em `docs/evidencias/` foram geradas no
  desktop. As capturas do jogo rodando em um celular Android (via depuração USB)
  serão anexadas em seguida.
