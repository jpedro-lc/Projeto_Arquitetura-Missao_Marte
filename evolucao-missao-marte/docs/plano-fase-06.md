# Plano e registro da Fase 6 — Android

> **Registro retroativo.** A Fase 6 foi implementada pela equipe e integrada
> direto na `master` (commit `57712e2`, 24/11/2026), sem plano prévio nem pull
> request. Este documento registra, depois do fato, o escopo, as decisões e a
> validação, como fizemos nas fases anteriores. Os ajustes da revisão de
> 24/11/2026 entraram pela branch `fase-06-android-ajustes`, com pull request.

**Origem:** `fases/fase-05-servidor-ranking/`.
**Destino:** `fases/fase-06-android/`.
**Referencial:** o `tutorial-api-gradle-libgdx.md` não cobre Android (regra do
prompt mestre). A fase usa a documentação oficial do libGDX e do Android
Developers, citada no tutorial.

## 1. Pedido do prompt mestre

Adaptar o jogo para Android com LibGDX, mantendo:

- versão desktop;
- versão Android;
- código compartilhado;
- o mesmo servidor de ranking.

Também adaptar os controles ao toque. Controle Bluetooth é opcional.

## 2. Diagnóstico do estado de origem (Fase 5)

- O `core` já separava regras (`model`, `service`) e apresentação. A rede
  usava `Gdx.net` e `JsonWriter`/`JsonReader`, que funcionam no Android; a
  Fase 5 evitou `java.net.http` justamente por isso.
- A entrada passava pela porta `EntradaJogo`, mas só havia o teclado do
  desktop (`TecladoEntradaJogo`, em `lwjgl3`).
- O ranking local do convidado (`RankingArquivoRepository`) ficava no
  `lwjgl3` e gravava no diretório de trabalho, que no Android é somente leitura.

## 3. Escopo realizado

| Item | Onde |
|---|---|
| Módulo `:android` (AGP 8.3.2, `compileSdk`/`targetSdk` 34, `minSdk` 21), `AndroidLauncher`, manifesto com `INTERNET` e `network_security_config` (HTTP local liberado) | `cliente/android/` |
| `:android` só entra no build com Android SDK configurado; o desktop compila sem SDK | `cliente/settings.gradle` |
| Toque: botões no rodapé da partida e *swipe* no mapa, um turno por gesto (`ToqueEntradaJogo`) | `core/.../presentation/input/` |
| Controle físico: teclado (WASD e setas), D-Pad e botões A/B/Y/Start/Back (`ControleFisicoEntradaJogo`), combinados por `EntradaComposta` | `core/.../presentation/input/` |
| Teclado virtual nos campos de login e nova partida; botão Voltar do Android tratado | `core/.../screen/` |
| Ranking local sem LibGDX (`RankingLocalRepository`), gravando em `getFilesDir()` no Android | `core/.../repository/` |
| Servidor da Fase 5 com interface de escuta configurável (`MISSAOMARTE_BIND=0.0.0.0` para Wi-Fi) | `servidor/application.properties` |
| Endereço do servidor no APK: `-Pmissaomarte.servidor` ou automático (`EnderecoServidor`): emulador → `10.0.2.2`, celular USB → `127.0.0.1` com `adb reverse` | `android/build.gradle`, `AndroidLauncher` |

**Preservado:**
- `model` e `service` idênticos aos da Fase 5 (conferido por SHA-256).
- Os pacotes `model`, `service`, `repository` e `remoto` continuam sem
  `com.badlogic`.
- O console e o modo convidado continuam funcionando.

## 4. Versões

Java 17, Gradle 8.7 e LibGDX 1.12.1 no cliente. O Android Gradle Plugin é o
8.3.2, que exige Gradle 8.4 ou mais novo, então o 8.7 serve. O servidor
continua com Spring Boot 4.1.2 e Gradle 9.8.2. O build Android precisa da
plataforma `android-34` no SDK, e o AGP a baixa sozinho quando as licenças já
foram aceitas.

## 5. Padrões

- **Port and Adapter** na entrada: `EntradaJogo` com os adaptadores de toque e
  de controle físico.
- **Composite**: `EntradaComposta` junta os dois adaptadores.
- **Composition Root** por plataforma: `Lwjgl3Launcher` e `AndroidLauncher`.
- **Repository**: `RankingLocalRepository`.
- **Decorator**: `RankingSomenteConvidado`, herdado da Fase 5.

## 6. Validação (revisão de 24/11/2026, Windows 11, OpenJDK 17.0.2)

| Verificação | Resultado |
|---|---|
| `servidor`: `./gradlew test` | 27 testes, 0 falhas |
| `cliente`: `./gradlew clean build` | 83 testes, 0 falhas (70 da Fase 5 + 13 novos) |
| `:lwjgl3:smokeDesktop` (janela real, convidado) | Passou |
| `:lwjgl3:smokeOnline` contra o servidor real | Passou: 82 passos |
| `:android:assembleDebug` | APK gerado, com `INTERNET`, *cleartext* liberado e natives `arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64` |
| Endereço no APK | Vazio (automático) por padrão; `-P` grava o IP informado |
| `model`/`service` | SHA-256 igual ao da Fase 5 |
| Celular real (Galaxy S20 FE 5G, Android 13, USB + `adb reverse`) | Cadastro, login, partida por toque e *swipe*, envio da derrota e histórico lidos do servidor; capturas em `fases/fase-06-android/docs/evidencias/android/` |

## 7. Pendências e divergências

| Item | Situação |
|---|---|
| Plano antes da implementação e PR | Não houve; este registro e o PR de ajustes cobrem a revisão |
| Capturas no Android | Resolvido em 24/11/2026: 11 capturas num Galaxy S20 FE 5G por depuração USB |
| Endereço padrão no emulador | Corrigido na revisão: antes o APK levava sempre `127.0.0.1` e o comentário do código dizia o contrário |
| HTTP sem TLS no Android | Mantido para o ambiente didático/local; produção exigiria HTTPS |
