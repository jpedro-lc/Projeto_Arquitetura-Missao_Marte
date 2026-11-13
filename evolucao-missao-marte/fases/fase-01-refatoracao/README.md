# Fase 1 — Refatoração arquitetural

Jogo Java de console evoluído a partir de `missao2/src/solidexercicio10/`.
Esta versão é independente: não lê fontes, bibliotecas ou rankings das entregas
anteriores. Mantém o pacote `solidexercicio10` e não usa Gradle ou frameworks.

O plano foi aprovado pelo usuário antes da criação desta fase. O
[tutorial](docs/fase-01-tutorial.md) mostra a transformação e os motivos das decisões.

## Compilar, testar e jogar

Na raiz do workspace:

```bash
bash evolucao-missao-marte/fases/fase-01-refatoracao/executar.sh compilar
bash evolucao-missao-marte/fases/fase-01-refatoracao/executar.sh testar
bash evolucao-missao-marte/fases/fase-01-refatoracao/executar.sh jogar
```

Ou, dentro desta pasta:

```bash
bash executar.sh testar
bash executar.sh jogar
```

É necessário Bash e um JDK. A execução foi validada com OpenJDK 25.0.5.0.3.
O script usa `javac` quando disponível ou o módulo `jdk.compiler` do Java.
Compila com UTF-8, `-Xlint:all` e `-Werror`, sem baixar dependências.
Os fontes usam recursos disponíveis no Java 17, mas a execução em Java 17 não
foi validada. A distribuição local não oferece `--release 17`; o script gera
classes para a versão do compilador em uso.

O script muda para a pasta desta fase antes de executar o jogo. Assim, o arquivo
`ranking-solid-exercicio10.json` fica aqui, separado dos rankings anteriores.
Se executar `Main` diretamente pela IDE, configure esta pasta como diretório de
trabalho. Compilados ficam em `out/classes/` e são ignorados pelo Git.

## Controles e regras preservadas

- `w/s/a/d`: mover; `c`: embarcar passageiro na posição; `q`: abortar a partida.
- Fácil inicia com 30 pontos; médio com 20; difícil com 15.
- Cada tentativa de movimento custa um ponto, inclusive se bloqueada na borda.
- Professor rende 15 pontos; engenheiro, 20; astronauta, 10.
- A nave inicia com três vidas. Para vencer, embarque todos e retorne a `(0,0)`
  antes de perder vidas ou pontos.
- O menu mostra os cinco melhores, salva vitórias e permite limpar o ranking.

## Organização e mudança principal

```text
src/solidexercicio10/
├── Main.java                       # Composição das dependências
├── model/                          # Entidades, ações e dados de leitura
│   ├── Acao.java
│   ├── EstadoPartida.java
│   ├── ResultadoAcao.java
│   └── ...entidades existentes
├── service/
│   ├── JogoService.java            # Regras e coordenação de um turno
│   └── FabricaMissoes.java         # Montagem e sorteio do cenário
├── presentation/
│   ├── JogoConsole.java            # Menu, teclado e mensagens
│   └── MapaRenderer.java           # Desenho a partir do estado de leitura
└── repository/
    ├── RankingRepository.java
    ├── RankingArquivoRepository.java
    └── RankingEntry.java
tests/solidexercicio10/tests/TestesFase01.java
```

`JogoService` não depende de `Scanner`, impressão ou apresentação. Recebe uma
`Acao` e retorna `ResultadoAcao`, contendo eventos e uma fotografia imutável da
partida. `JogoConsole` traduz as teclas e apresenta esses dados.
Relógio e aleatoriedade podem ser fornecidos nos testes. A implementação do
ranking foi renomeada para esclarecer seu papel; o contrato foi preservado.

## Validação realizada em 13/11/2026

- Cópia inicial dos 18 fontes compilada e executada isoladamente.
- 19 verificações de caracterização na cópia antes da refatoração: movimento,
  entidades, embarque, pontos, colisão, fábrica e formato do ranking.
- Suíte final: **162 verificações aprovadas**, com cenários controlados.
- Compilação final com todos os avisos ativados e tratados como erro.
- Testes cobrem regras, prioridade de derrota/vitória, snapshots imutáveis,
  repetibilidade, ranking temporário e fluxo completo de vitória no console.
- Inspeção interativa do terminal: menu, mapa de tamanho 3, movimento, tentativa
  de embarque sem passageiro, retorno, aborto e saída.
- Cópia desta fase para outro diretório, com espaços no nome, compilada e
  testada sem as outras pastas do repositório.
- Integridade de `missao1/`, `missao2/`, prompt, plano e referencial conferida
  por SHA-256; nenhuma fase posterior foi criada.

Não foi jogada manualmente uma partida aleatória até a vitória. Vitória e
persistência foram verificadas por testes automatizados, com cenários conhecidos.
Não foram executadas versões gráficas, Android, servidor ou Java 17.

## Limitações preservadas por escopo

Refatorar não significa mudar as regras existentes. Por isso:

- Comando inválido e aborto ainda provocam movimento de inimigos e colisões.
- Inimigos ainda podem sair da área visível do mapa.
- Mapas pequenos podem não comportar todas as entidades e lançar exceção.
  O sorteio também pode esgotar tentativas; não foi substituído por outro algoritmo.
- A geração continua criando professores e engenheiros, sem astronautas.
- Apesar da extensão `.json`, o ranking usa linhas delimitadas por `|`, como na
  base SOLID. Não há migração do JSON do Exercício 10 original.
- Tratamento de arquivo corrompido, nomes com `|` e recuperação de erros de
  gravação permanecem limitados pelo repositório legado. Gravação é síncrona.
- A proteção das listas do domínio não torna todas as entidades imutáveis.
  A fronteira com a apresentação usa records e cópias, sem expor essas entidades.

A mudança de entrada aprovada foi encerrar o console sem exceção quando a entrada
termina, inclusive durante a configuração de uma partida.
