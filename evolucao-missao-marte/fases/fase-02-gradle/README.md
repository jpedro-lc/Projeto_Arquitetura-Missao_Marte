# Fase 2 — Migração para Gradle

Aplicação de console da Fase 1 com build Gradle 8.7, Java 17 e DSL Groovy.
Os 23 fontes de produção e a suíte original foram preservados byte a byte.
O adaptador JUnit conecta as 162 verificações existentes à tarefa `test`.

Consulte o [tutorial](docs/fase-02-tutorial.md) para a explicação de cada
configuração, o antes/depois e o passo a passo da migração.

## Preparar o ambiente

É necessário um JDK 17. No Ubuntu amd64 deste ambiente, selecione-o no terminal:

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
java -version
javac -version
```

Em outra máquina, ajuste `JAVA_HOME` ao diretório real de seu JDK 17. O caminho
acima é um exemplo de ambiente, não uma configuração embutida no build.
O Java que inicia o Gradle e a toolchain do projeto são escolhas diferentes:
não inicie Gradle 8.7 com o Java 25 padrão e espere que a toolchain resolva isso.

O Wrapper baixa a versão correta do Gradle quando necessário. Não é preciso ter
um Gradle global instalado. Os downloads iniciais de Gradle e JUnit exigem rede;
depois, os artefatos já armazenados podem ser usados em modo `--offline`.

## Compilar, testar e jogar

Na raiz do repositório, entre na pasta desta fase:

```bash
cd evolucao-missao-marte/fases/fase-02-gradle
./gradlew --version
./gradlew build
./gradlew test --rerun-tasks
./gradlew run --console=plain
```

No Windows, com JDK 17 configurado, use `gradlew.bat` em lugar de `./gradlew`.
O script Windows está incluído, mas sua execução não foi validada neste ambiente Linux.

| Comando | Efeito |
|---|---|
| `./gradlew tasks` | Lista as tarefas disponíveis. |
| `./gradlew classes` | Compila/prepara a aplicação. |
| `./gradlew test` | Executa o adaptador JUnit e as 162 verificações internas. |
| `./gradlew build` | Compila, testa e gera JAR e distribuições; não abre o jogo. |
| `./gradlew run --console=plain` | Compila o necessário e abre o jogo; não executa testes automaticamente. |
| `./gradlew clean build` | Remove o `build/` anterior e reconstrói o projeto. |
| `./gradlew installDist` | Cria distribuição local com scripts e JAR. |
| `./gradlew distZip` | Cria ZIP da distribuição. |

O relatório fica em `build/reports/tests/test/index.html`. Ele registra **um
teste JUnit**, que chama a suíte existente e executa **162 verificações**. O
resultado detalhado da suíte aparece na saída do teste e no XML correspondente.

O ranking de `run` fica em `ranking-solid-exercicio10.json`, nesta pasta.
A tarefa encaminha a entrada do terminal ao `Scanner` e fixa seu diretório de
trabalho aqui, inclusive quando o projeto é selecionado pelo argumento `-p`.
Não há dependência dos fontes ou rankings das fases anteriores.

## Distribuição

```bash
./gradlew installDist
cd build/install/missao-marte-fase-02
bin/missao-marte-fase-02
```

O script instalado inicia a aplicação sem o Gradle. Mantenha a pasta `lib/`
junto de `bin/`. Como o caminho do ranking é relativo no código preservado,
ele fica no diretório de trabalho usado ao iniciar o script de distribuição.
Entrar na pasta da distribuição como acima mantém esse arquivo separado.

O ZIP está em `build/distributions/missao-marte-fase-02.zip`. O JAR isolado não
recebeu um manifesto especial para `java -jar`; use os scripts da distribuição.
JUnit e classes de teste não fazem parte da distribuição do jogo.

## Abrir no VS Code

Abra diretamente **esta pasta**, `fase-02-gradle/`, e importe seu build Gradle.
Selecione um JDK 17 para a JVM do Gradle e instale as extensões Java/Gradle, se
necessário. O runtime da extensão Java pode ser diferente da JVM do build.

As versões anteriores usam nomes completos de classes iguais. Reuni-las como
fontes de um único projeto Java provoca duplicidades. Esta fase é um projeto
Gradle separado; não inclua `missao2` ou Fase 1 como raízes de fontes dele.

## Validação em 14/11/2026

- Wrapper gerado pelo Gradle 8.7; SHA-256 do JAR e da distribuição conferidos
  contra a referência oficial. O Wrapper baixou e executou a distribuição.
- `clean build installDist distZip` concluído com Java 17.0.21.0.3.
- Compilação com `--release 17`, UTF-8, `-Xlint:all -Werror`.
- Relatório XML conferido: 1 teste JUnit, zero falhas, zero erros, zero skips;
  a suíte imprimiu `PASSOU: 162 verificações da Fase 1.`.
- Execução forçada dos testes para confirmar que não se tratava de tarefa omitida.
- Segundo build com compilação e testes `UP-TO-DATE`.
- `run` recebeu comandos de terminal, listou/limpou um ranking de teste da
  Fase 2, iniciou missão, apresentou o mapa, abortou e saiu. Não executou `test`.
- Script da distribuição executado; JAR/ZIP sem JUnit ou classes de teste.
- Cópia independente em diretório com espaços compilada e executada com
  `--offline`, utilizando os downloads já existentes em cache.
- Fontes de aplicação e suíte original idênticos à origem; arquivos existentes
  do repositório preservados por SHA-256, exceto o índice atualizado da evolução.

Não houve teste Windows nem uma partida aleatória manual até a vitória nesta
fase. A vitória é coberta pela suíte preservada. Neste executor, o sandbox
bloqueou o socket local do daemon; a validação foi feita com permissão de
execução fora dessa restrição, usando caches em `/tmp`.

## Limites desta etapa

Continuam o console, o namespace `solidexercicio10` e as regras da Fase 1,
inclusive suas limitações documentadas: mapas insuficientes, sorteio por
tentativas, inimigos sem fronteira, movimento após aborto e ranking delimitado
por `|` apesar da extensão `.json`. Não foram introduzidos LibGDX, servidor,
Android ou mudanças na arquitetura do domínio.
