# Plano da Fase 2 — Migração para Gradle

**Estado:** proposta para aprovação; projeto da Fase 2 ainda não criado.
**Diagnóstico:** 13/11/2026.
**Origem:** `evolucao-missao-marte/fases/fase-01-refatoracao/`.
**Destino após aprovação:** `evolucao-missao-marte/fases/fase-02-gradle/`.

## 1. Diagnóstico da origem

A Fase 1 possui 23 fontes de aplicação, no pacote `solidexercicio10`, e uma
suíte executável `solidexercicio10.tests.TestesFase01`, com 162 verificações.
As responsabilidades já estão separadas entre modelo, serviço, apresentação e
repositório. A classe principal é `solidexercicio10.Main`.

O script `executar.sh` reúne os fontes de `src/` e `tests/`, compila com UTF-8,
`-Xlint:all -Werror` e executa o jogo ou o `main` da suíte. Não há build Gradle,
Wrapper, dependências externas ou relatório de testes padronizado nessa origem.

O ranking usa um caminho relativo ao diretório de execução. A Fase 2 precisará
continuar isolando seu arquivo, sem ler nem limpar rankings das versões anteriores.
Permanecem as limitações funcionais documentadas na Fase 1; a migração não as corrige.

## 2. Ambiente e versões propostas

| Item | Situação observada | Proposta |
|---|---|---|
| Java padrão deste processo | OpenJDK 25.0.5.0.3 | Não usar esse runtime para iniciar Gradle 8.7. |
| JDK adicional | OpenJDK 17.0.21.0.3 em `/usr/lib/jvm/java-17-openjdk-amd64` | Executar Gradle, compilar e testar com JDK 17. |
| Gradle | 8.7 instalado em `~/.local/opt/gradle-8.7/`, fora do PATH deste processo | Usar a instalação somente para gerar o Wrapper; depois usar `./gradlew`. |
| DSL | Referencial usa Groovy | `build.gradle` e `settings.gradle` em Groovy. |
| Testes | Suíte Java com `main`, sem JUnit | Adicionar adaptador com JUnit Jupiter 5.10.2, somente no escopo de testes. |

Verificações já realizadas, com caches e compilados somente em `/tmp`:

- Gradle 8.7 iniciou e informou JVM 17.0.21.0.3.
- Os 23 fontes da aplicação e a suíte compilaram com JDK 17, `--release 17`,
  UTF-8, `-Xlint:all` e `-Werror`.
- A suíte passou nas 162 verificações usando Java 17.

O runtime usado para iniciar o Gradle e a toolchain usada para compilar são
configurações distintas. Uma toolchain 17 no build não torna Gradle 8.7 compatível
com um processo iniciado em Java 25. As instruções explicarão como selecionar
`JAVA_HOME` no terminal e a JVM do Gradle no VS Code, sem versionar caminhos
absolutos específicos desta máquina.

As versões Java 17, Gradle 8.7 e DSL Groovy coincidem com o referencial. A adoção
de JUnit 5.10.2 é uma complementação proposta, não uma versão prescrita pelo
tutorial e nem uma alegação de ser a versão mais recente.

Fontes oficiais consultadas:

- [Matriz de compatibilidade Java/Gradle](https://docs.gradle.org/current/userguide/compatibility.html).
- [Notas do Gradle 8.7](https://docs.gradle.org/8.7/release-notes.html).
- [Guia do JUnit 5.10.2 — integração com Gradle](https://docs.junit.org/5.10.2/user-guide/#running-tests-build-gradle).

## 3. Escopo e estrutura proposta

Após aprovação, copiar somente os fontes necessários da Fase 1 e reorganizar
as cópias no layout convencional do plugin Java. Não alterar os pacotes ou as
regras. Não copiar `out/`, caches, rankings ou o tutorial da fase anterior.

```text
fase-02-gradle/
├── .gitignore
├── README.md
├── settings.gradle
├── build.gradle
├── gradlew
├── gradlew.bat
├── gradle/wrapper/
│   ├── gradle-wrapper.jar
│   └── gradle-wrapper.properties
├── src/main/java/solidexercicio10/
│   └── ...os 23 fontes da aplicação
├── src/test/java/solidexercicio10/tests/
│   ├── TestesFase01.java
│   └── RegrasJogoTest.java
└── docs/
    └── fase-02-tutorial.md
```

O script de compilação manual da Fase 1 não será copiado: o Wrapper será o ponto
de entrada dos comandos desta etapa. O projeto será único, sem módulos `core`
ou `lwjgl3`, sem LibGDX, servidor ou Android.

## 4. Arquivos e alterações propostas

| Arquivo ou grupo | Alteração |
|---|---|
| Fontes de aplicação | Copiar `src/solidexercicio10/` para `src/main/java/solidexercicio10/`, preservando conteúdo. |
| Suíte existente | Copiar para `src/test/java/solidexercicio10/tests/`, preservando as verificações. |
| Novo `RegrasJogoTest` | Um método JUnit chama `TestesFase01.main(...)`; uma falha continua fazendo o teste falhar. |
| `settings.gradle` | Nomear o projeto `missao-marte-fase-02`; não incluir fases anteriores como subprojetos. |
| `build.gradle` | Aplicar plugins `java` e `application`; configurar Java 17, compilação, testes, entrada e execução. |
| Wrapper | Gerar com a distribuição oficial 8.7; versionar scripts, JAR e propriedades, com checksum oficial da distribuição. |
| `.gitignore` | Ignorar artefatos e caches locais; permitir o JAR oficial do Wrapper no controle de versão. |
| README e tutorial da fase | Registrar comandos verificados, ambiente, decisões e transformação reproduzível. |
| Índice `evolucao-missao-marte/README.md` | Atualizar situação, links e comandos da Fase 2 após validação. |

Configuração do build:

- `application.mainClass = 'solidexercicio10.Main'`.
- Toolchain de Java 17 e compilação com `options.release = 17`, UTF-8,
  `-Xlint:all -Werror`.
- `mavenCentral()` como repositório das dependências de teste.
- JUnit BOM 5.10.2, Jupiter em `testImplementation` e launcher em
  `testRuntimeOnly`, com versões alinhadas pelo BOM e `useJUnitPlatform()`.
- Nenhuma dependência externa de produção e nenhum plugin externo necessário.
- `run.standardInput = System.in` para o jogo receber comandos do terminal.
- Diretório de trabalho de `run` definido como a pasta da Fase 2.
- `test` executa o adaptador e gera relatório HTML/XML. O relatório terá um
  teste JUnit que executa 162 verificações internas; não serão anunciados 162
  métodos JUnit individuais.
- `build` inclui testes. Não aceitar `NO-SOURCE` ou ausência de testes executados
  como validação da suíte.
- Empacotamento pelas tarefas do plugin `application`, com scripts de execução
  e distribuição (`installDist`/`distZip`). Não mudar o comportamento do ranking.

## 5. Princípios e decisões arquiteturais

Preservar a arquitetura da Fase 1 e sua injeção de dependências. A mudança está
na construção do projeto, não em novos padrões no domínio. Não introduzir uma
hierarquia de classes ou framework de injeção para justificar a migração.

O pequeno adaptador JUnit mantém a suíte reaproveitável e permite ao Gradle e
ao editor identificar um teste real. Não reescrever as verificações em centenas
de métodos nesta fase. O layout padrão reduz configuração de caminhos e facilita
a importação da Fase 2 como projeto próprio pelo VS Code.

Na IDE, orientar a abertura direta da pasta `fase-02-gradle/` e a importação de
seu build. Não reunir `missao2` e todas as fases como raízes de fontes do mesmo
projeto Java, pois compartilham nomes completos de classes. Não editar caches
externos do editor nem fontes das versões anteriores.

## 6. Referencial utilizado e adaptações

Usar o arquivo [tutorial-api-gradle-libgdx.md](../tutorial-api-gradle-libgdx.md)
pelos títulos das seções, pois a numeração interna é inconsistente:

- **Parte 1 – O que é uma API?**: contextualização das APIs Java existentes.
- **História e Origem do Gradle**, **Versões do Gradle**, **O que é um Build Tool?**
  e **Gradle vs Maven**: papel da ferramenta e seleção de versão.
- **Conceitos Fundamentais do Gradle**: tasks, DAG, daemon, build cache e
  configuration cache; distinguir `UP-TO-DATE` de reutilização por build cache.
- **DSL: Groovy vs Kotlin**, **Estrutura de um `build.gradle`**, **Dependências
  e seus Escopos**, **O Gradle Wrapper em Detalhes**, **Ciclo de Vida de um Build
  Gradle** e **Comandos Gradle Essenciais**: configuração e execução reais da fase.
- **Projeto Multi-módulo (libGDX)**: somente uma prévia conceitual da Fase 3.
- **Visão Geral do Ambiente**, **Passo 1 – Instalar o JDK 17**, **Passo 2 – Instalar
  o Visual Studio Code**, **Passo 3 – Instalar as Extensões no VS Code**,
  **Passo 4 – Instalar o Gradle (Opcional)**, **Passo 5 – Abrir o Projeto no VS Code**
  e **Executando o Jogo no VS Code**: adaptar para Linux e o projeto de console.
- **Parte 9 – Executando e Testando**: terminal, painel Gradle e depuração.

Diferenças que o tutorial da fase deverá esclarecer:

1. O exemplo do referencial usa LibGDX, launcher e namespace distintos; nesta
   etapa usamos a arquitetura existente e `solidexercicio10.Main`.
2. O material apresenta comandos Windows e projeto multi-módulo; esta etapa
   valida Linux, fornece Wrapper Windows e mantém um único projeto.
3. O exemplo declara dependências gráficas; aqui somente JUnit é acrescentado,
   no escopo de testes. Não copiar dependências de LibGDX ou repositórios extras.
4. A origem usa `src/` e `tests/` e testes por `main`; a migração usará o layout
   padrão e um adaptador JUnit. O referencial não especifica esse adaptador;
   complementá-lo com a documentação oficial do JUnit.
5. O referencial mostra `sourceCompatibility`/`targetCompatibility`; propõe-se
   toolchain e `--release 17` para controlar também as APIs disponíveis.
6. O Java padrão ainda é 25 neste processo, mas o JDK 17 e o Gradle 8.7 foram
   encontrados e validados. Documentar a seleção explícita do runtime.

Não alterar o referencial nem reproduzir trechos longos. No tutorial da fase,
usar o link relativo `../../../tutorial-api-gradle-libgdx.md` e incluir os 12
itens exigidos no prompt mestre.

## 7. Riscos e validação

| Risco | Validação/mitigação |
|---|---|
| Gradle 8.7 iniciar com Java 25 | Selecionar JDK 17 antes de iniciar e conferir `./gradlew --version`. |
| `test` dar sucesso sem executar a suíte | Adaptador JUnit; verificar relatório com teste executado e saída das 162 verificações. |
| Entrada do console terminar imediatamente | Ligar `System.in` e testar menu, comandos e saída no terminal. |
| Ranking afetar outra fase | Fixar diretório de `run` e usar rankings temporários nos testes. |
| Dependências ou Wrapper não baixarem | Relatar falhas de rede; não substituir por arquivos fictícios nem ocultar testes não executados. |
| IDE misturar versões | Abrir/importar o projeto da Fase 2 separadamente e documentar seu layout. |
| Artefatos e caches entrarem no Git | Conferir arquivos preparados e manter somente fontes/configuração/documentação/Wrapper. |

Após implementação, executar com o Wrapper e JDK 17:

```bash
./gradlew --version
./gradlew tasks
./gradlew clean build
./gradlew test --rerun-tasks
./gradlew run --console=plain
./gradlew installDist
./gradlew distZip
```

Conferir o relatório de testes, dependências de runtime sem JUnit e o lançamento
pela distribuição. Repetir um build para demonstrar verificações de atualização,
sem confundir isso com build cache. Validar a cópia independente em outro
diretório, preservação das versões anteriores e ausência de fases futuras.
Wrapper Windows será incluído, mas sua execução só poderá ser declarada testada
se houver um ambiente Windows disponível.

## 8. Critérios de conclusão

- Wrapper 8.7 oficial e build Groovy executáveis com JDK 17.
- Aplicação compila, recebe comandos e mantém as regras da Fase 1.
- Task `test` executa a suíte existente por JUnit e gera relatório verificável.
- Build e distribuição funcionam sem depender da instalação global do Gradle
  ou de fontes das outras versões, após os downloads iniciais necessários.
- Ranking da fase isolado; nenhum cache, compilado ou ranking anterior copiado.
- Tutorial didático e README com comandos efetivamente verificados.
- `missao1/`, `missao2/`, Fase 1 e referencial teórico intactos.
- Fase 3 não iniciada.

## 9. Git e aprovação

Depois da aprovação deste plano, trabalhar em branch própria, proposta
`fase-02-gradle`, com commits por etapa. Preparar um pull request para revisão
ao concluir; não integrar diretamente na `master` sem aprovação do usuário.

A regra de execução 8 do [prompt mestre](../PROMPT_MESTRE.md) exige aprovação
explícita do plano antes de criar a pasta da fase, copiar fontes ou implementar
o build. A autorização anterior cobriu somente a Fase 1. Este documento registra
o planejamento da Fase 2 e ainda não altera o jogo.
