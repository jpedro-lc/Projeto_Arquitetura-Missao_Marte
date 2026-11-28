# Rodar o Missão Marte na apresentação

O jogo pronto para outro computador é um `.zip` com o jogo, um Java 17 embutido
e um atalho. **Não precisa instalar Java, Gradle nem Android Studio.** O jogo
fala com o servidor publicado no Render
(`https://missao-marte-servidor.onrender.com`), então o ranking global funciona
em qualquer rede com internet.

| Arquivo | Para quê | Tamanho |
|---|---|---|
| `MissaoMarte-windows.zip` | Jogo no Windows (64 bits) | ~80 MB |
| `MissaoMarte-android.apk` | Jogo no celular Android 5.0+ | ~32 MB |

## No computador da apresentação (Windows)

1. Baixe `MissaoMarte-windows.zip` (da *Release* no GitHub ou de um pendrive).
2. Clique com o botão direito → **Extrair tudo**. Não rode de dentro do `.zip`.
3. Na pasta extraída, clique duas vezes em **`Jogar Missao Marte.bat`**.
4. Se aparecer "O Windows protegeu o computador": **Mais informações** →
   **Executar assim mesmo**.

**Antes de começar a falar:** abra o jogo uns 2 minutos antes. O servidor
gratuito dorme depois de 15 min sem uso e leva até 1 minuto para acordar; o
jogo já chama o servidor ao abrir, e o login mostra "Conectando... pode levar
até 1 min." enquanto isso.

Sem internet, tudo funciona em **modo convidado** (ranking local na pasta do
jogo); só cadastro, login, ranking global e histórico precisam do servidor.

## No celular (Android)

1. Copie `MissaoMarte-android.apk` para o celular (cabo, Drive ou WhatsApp).
2. Abra o arquivo e permita "instalar apps desta fonte" quando o Android pedir.
3. O APK já aponta para o Render: funciona no Wi-Fi ou nos dados móveis, sem
   cabo nem `adb reverse`.

Com cabo e depuração USB, também dá para instalar pelo PC:
`adb install -r MissaoMarte-android.apk`.

## Gerar os arquivos de novo

Num computador com JDK 17 (e o Android SDK, para o APK), dentro de `cliente`:

```powershell
.\gradlew.bat :lwjgl3:zipApresentacao
.\gradlew.bat :android:assembleDebug "-Pmissaomarte.servidor=https://missao-marte-servidor.onrender.com"
```

Saídas: `lwjgl3\build\distributions\MissaoMarte-windows.zip` e
`android\build\outputs\apk\debug\android-debug.apk`. Para outro servidor:
`-PservidorPacote=https://...` no primeiro comando.

## Por que um `.bat` e não um `.exe`

O `jpackage` gera um `.exe`, mas ele não é assinado digitalmente. No Windows 11
com o **Smart App Control** ligado (como na máquina em que o pacote foi feito),
esse `.exe` é bloqueado sem opção de executar. O `.bat` abre o `javaw.exe` que o
`jlink` copia do JDK, o mesmo arquivo assinado pela Oracle, e passa pelo bloqueio.
Verificado em 28/11/2026: o `.zip` extraído numa pasta fora do projeto abriu a
janela do jogo com o Smart App Control ativo.

## Plano B: rodar pelo código-fonte

Se o `.zip` não puder ser usado, com JDK 17 instalado:

```powershell
git clone https://github.com/jpedro-lc/Projeto_Arquitetura-Missao_Marte.git
cd Projeto_Arquitetura-Missao_Marte\evolucao-missao-marte\fases\fase-07-evolucao-livre\cliente
.\gradlew.bat :lwjgl3:run "-Dmissaomarte.servidor=https://missao-marte-servidor.onrender.com"
```

A primeira execução baixa o Gradle e as dependências (precisa de internet e
alguns minutos).

## Lembretes

- O banco gratuito do Render expira por volta de **27/12/2026**; depois disso,
  contas e rankings somem.
- Contas criadas na apresentação ficam no ranking global. Use nomes de teste.
