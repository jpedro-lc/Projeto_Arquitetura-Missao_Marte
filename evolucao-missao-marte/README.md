# Evolução arquitetural — Missão Marte

Esta pasta reúne a evolução do jogo Java da equipe. A Fase 1 partiu da versão
SOLID existente; as entregas anteriores continuam preservadas.

## Onde estamos

- **Preparação:** concluída; caminhos, regras de trabalho e planejamento organizados.
- **Fase 1:** aprovada pelo usuário, implementada e validada; veja o
  [projeto de console](fases/fase-01-refatoracao/README.md) e o
  [tutorial da refatoração](fases/fase-01-refatoracao/docs/fase-01-tutorial.md).
- **Base dos fontes:** [`../missao2/src/solidexercicio10/`](../missao2/src/solidexercicio10/).
- **Referência histórica:** [`../missao2/src/exercicio10/`](../missao2/src/exercicio10/).

A pasta `missao2` é uma entrega anterior e não corresponde à Fase 2 desta evolução.
Não é necessário copiar o repositório, mover as entregas ou criar outro workspace.

## Documentos de trabalho

- [Prompt mestre](PROMPT_MESTRE.md): escopo das sete fases e regras de execução.
- [Plano da Fase 1](docs/plano-fase-01.md): diagnóstico e escopo aprovado.
  O documento foi preservado como registro da proposta anterior à implementação;
  o estado atual e os resultados estão neste índice e no README da fase.
- [Referencial de API, Gradle e LibGDX](tutorial-api-gradle-libgdx.md): material
  somente leitura para as Fases 2, 3 e 4; não é a especificação da Fase 1.

## Organização

```text
evolucao-missao-marte/
├── .gitignore
├── README.md
├── PROMPT_MESTRE.md
├── tutorial-api-gradle-libgdx.md
├── docs/
│   └── plano-fase-01.md
└── fases/
    ├── .gitkeep
    └── fase-01-refatoracao/
        ├── README.md
        ├── executar.sh
        ├── src/solidexercicio10/
        ├── tests/
        └── docs/fase-01-tutorial.md
```

Cada pasta de fase será criada somente depois da aprovação do respectivo plano.
A Fase 1 contém uma cópia refatorada dos fontes da versão SOLID; as seguintes receberão
os arquivos necessários da fase anterior concluída. Não copie rankings, classes
compiladas ou caches. Depois de criada, cada fase deve compilar sem depender dos
fontes da `missao2` ou de outras fases.

## Índice das fases

| Fase | Pasta prevista dentro de `fases/` | Situação |
|---|---|---|
| 1 — Refatoração arquitetural | `fase-01-refatoracao/` | Concluída: 162 verificações aprovadas |
| 2 — Gradle | `fase-02-gradle/` | Não iniciada |
| 3 — LibGDX | `fase-03-libgdx/` | Não iniciada |
| 4 — Interface gráfica | `fase-04-interface-grafica/` | Não iniciada |
| 5 — Servidor e ranking | `fase-05-servidor-ranking/` | Não iniciada |
| 6 — Android | `fase-06-android/` | Não iniciada |
| 7 — Evolução livre | `fase-07-evolucao-livre/` | Não iniciada |

Os comandos das fases seguintes serão registrados quando forem implementados e
verificados. A Fase 2 ainda não foi iniciada.

## Executar a Fase 1

Na raiz do workspace:

```bash
bash evolucao-missao-marte/fases/fase-01-refatoracao/executar.sh compilar
bash evolucao-missao-marte/fases/fase-01-refatoracao/executar.sh testar
bash evolucao-missao-marte/fases/fase-01-refatoracao/executar.sh jogar
```

A compilação passa com `-Xlint:all -Werror`. A suíte executa 162 verificações de
regras, imutabilidade, criação, persistência e console. A versão também foi
compilada e testada numa cópia independente fora do repositório. O ranking é
gerado somente no diretório da fase ao executar pelo script.

As regras e limitações preservadas estão no [README da fase](fases/fase-01-refatoracao/README.md).

## Ambiente verificado na preparação

Em 13/11/2026, o Java disponível é OpenJDK 25.0.5.0.3. O executável `javac` não
está no PATH, mas o compilador funciona com:

```bash
java -m jdk.compiler/com.sun.tools.javac.Main -version
```

As duas bases existentes compilaram sem avisos com `-Xlint:all`. Foram executados
menu, consulta de ranking vazio e saída, com artefatos isolados em `/tmp`.
Não foi realizada uma partida completa nessa preparação, nem encontrados testes
automatizados existentes nessas bases. Relatos de testes nos documentos antigos
não substituem testes executáveis ou uma nova validação.

A Fase 1 permanece em Java de console, sem Gradle ou dependências externas.
A seleção de versões para Gradle e LibGDX será feita no planejamento das fases
correspondentes; as versões do tutorial não foram impostas ao ambiente.

## Continuidade

A autorização recebida foi para a Fase 1 e foi atendida. A migração para Gradle
exige o planejamento e a aprovação específicos da Fase 2, conforme o prompt
mestre. A versão concluída na Fase 1 será sua origem.
