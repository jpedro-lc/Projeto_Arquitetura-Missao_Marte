# Plano da Fase 1 — Refatoração arquitetural

**Estado:** proposta para aprovação; implementação não iniciada.
**Diagnóstico realizado em:** 13/11/2026.
**Destino após aprovação:** `evolucao-missao-marte/fases/fase-01-refatoracao/`.

## 1. Origem e objetivo

Evoluir a versão de console já refatorada em
[`../../missao2/src/solidexercicio10/`](../../missao2/src/solidexercicio10/),
sem refazer o jogo do zero. O Exercício 10 em
[`../../missao2/src/exercicio10/`](../../missao2/src/exercicio10/)
será referência histórica. Nenhuma dessas pastas será modificada.

A pasta `comparacoes/jogo-service/` foi removida por solicitação do usuário.
Ela não é fonte do projeto, nem uma fase concluída. A implementação deverá
partir dos fontes preservados na `missao2`.

O objetivo é deixar as regras utilizáveis sem terminal, mantendo uma aplicação
de console funcional. O namespace `solidexercicio10` será mantido. A cópia dos
fontes e a criação da pasta da fase só ocorrerão após aprovação deste plano.

## 2. Diagnóstico do código existente

A base SOLID contém 18 arquivos Java, organizados em quatro pacotes e `Main`:

| Parte | Responsabilidade e dependências observadas |
|---|---|
| `Main` | Cria `RankingService`, injeta `RankingRepository` em `JogoService` e abre `Scanner`. |
| `model` | Entidades, posições, dificuldade, embarque, movimento e colisões. `Missao` mantém as listas; `Nave` mantém vidas, capacidade e passageiros embarcados. |
| `service/JogoService` | Menu, entrada do jogador, criação aleatória do cenário, estado e regras da partida, renderização e coordenação do ranking. |
| `presentation/MapaRenderer` | Desenha o mapa no terminal, dependendo diretamente de `Missao` e das entidades. |
| `repository/RankingRepository` | Contrato já existente para salvar, listar e limpar ranking. |
| `repository/RankingService` | Implementação em arquivo, com leitura, escrita e ordenação. |

Pontos que já devem ser aproveitados:

- Herança e polimorfismo de `Passageiro`, com pontuação definida nas subclasses.
- Interfaces pequenas `Movel`, `Posicionavel` e `RankingRepository`.
- Entidades separadas da persistência e ausência de dependências externas.
- Renderização do mapa já extraída da classe principal.

Problemas a tratar nesta fase:

1. `JogoService` usa `Scanner`, `System.out` e `MapaRenderer`; regras e console
   ainda estão acoplados. Mudar a interface exige alterar o serviço.
2. O loop completo fica no serviço. Não há uma API pública para iniciar uma
   partida, processar uma ação e consultar seu estado sem atravessar menus.
3. Criação do cenário e sorteio de posições estão misturados à execução da
   partida. `Random`, `Math.random()` e relógio real dificultam cenários repetíveis.
4. `Nave.moverComLimites(char, ...)` conhece as letras do teclado. As teclas
   devem ser traduzidas pela apresentação para ações ou deslocamentos do domínio.
5. As listas mutáveis de `Missao` e `Nave` são expostas diretamente. A nova
   apresentação deve consumir dados de leitura e não modificar o estado do jogo.
6. `RankingService` é um repositório em arquivo apesar do nome genérico. Essa
   responsabilidade precisa ficar clara na implementação e na documentação.
7. Não foram encontrados testes automatizados executáveis nas bases. A revisão
   SOLID existente contém afirmações que precisam ser conferidas no código.

## 3. Comportamento de referência

A versão SOLID será a referência funcional desta fase. O original e seus READMEs
não devem ser usados para introduzir regras ausentes na base escolhida.

| Aspecto | Comportamento observado na base SOLID |
|---|---|
| Pontos iniciais | Fácil: 30; médio: 20; difícil: 15. |
| Movimento | `w/s/a/d`; cada tentativa custa um ponto e incrementa movimentos, inclusive na borda. |
| Embarque | `c`; depende da posição e capacidade; pontos vêm de `Passageiro.getPontuacao()`. |
| Pontos por tipo | Professor: 15; engenheiro: 20; astronauta: 10. |
| Vidas | Nave inicia com três; colisões retiram uma vida por turno com colisão. |
| Vitória | Todos os passageiros embarcados e nave de volta a `(0,0)`, com partida ainda ativa. |
| Ranking | Vitória salva piloto, pontos, dificuldade, passageiros e tempo; menu mostra até cinco entradas. |
| Persistência | Arquivo chamado `ranking-solid-exercicio10.json`, mas conteúdo separado por `\|`, não JSON. |
| Passageiros gerados | A fábrica atual alterna professores e engenheiros; não cria astronautas, embora a classe exista. |
| Quantidades | Fácil: 4 passageiros, 2 asteroides, 2 inimigos; médio: 5/2/2; difícil: 6/3/3. |

Diferenças já observadas frente ao Exercício 10 original: o ranking original é
JSON, filtra os cinco melhores ao salvar e pede confirmação para resetar. A
versão SOLID mantém entradas em arquivo delimitado e limpa sem confirmação.
Não haverá conversão automática ou restauração dessas diferenças nesta fase.

Detalhes a caracterizar e preservar nos testes, sem corrigir silenciosamente:

- Um comando inválido ainda provoca o turno de movimento dos inimigos.
- O comando de aborto ainda passa pelo movimento de inimigos e pelas colisões
  antes de o loop da partida terminar.
- Entrada vazia durante a partida não processa um turno.
- Inimigos não possuem limitação de fronteira no movimento atual.
- Sorteio pode esgotar as tentativas; mapas pequenos podem não comportar todas
  as entidades. A mensagem após o laço de passageiros não recupera essa exceção.
- A leitura de `Scanner.nextLine()` pode lançar exceção em fim de entrada.

O console novo poderá encerrar de forma limpa em fim de entrada. Essa é a única
mudança de tratamento de entrada prevista, sem alterar regras da partida.

## 4. Escopo e arquivos afetados

Após aprovação, copiar os 18 fontes da base para o projeto independente da fase.
Refatorar a cópia de forma incremental, mantendo os modelos existentes.

| Arquivo ou grupo no destino | Alteração proposta |
|---|---|
| `src/solidexercicio10/Main.java` | Compor dependências e iniciar a apresentação de console. |
| `service/JogoService.java` | Coordenar regras; expor início, ação e consulta de estado; retirar leitura, impressão e renderização. |
| Novo `service/FabricaMissoes.java` | Extrair configuração e criação do cenário; permitir aleatoriedade fornecida externamente. |
| Novo `presentation/JogoConsole.java` | Receber menu, prompts, tradução de teclas, mensagens, estatísticas e loop do console. |
| `presentation/MapaRenderer.java` | Renderizar dados de leitura, sem alterar a partida. |
| Novos tipos `model/Acao`, `model/EstadoPartida` e `model/ResultadoAcao` | Representar ações, estado visível e eventos estruturados, usando enums/records quando apropriado. |
| `model/Nave.java` e `model/Missao.java` | Retirar a interpretação de teclas da nave, proteger consultas às coleções e permitir sorteio controlado do movimento de inimigos. Manter regras de movimento, embarque e colisão. |
| `repository/RankingRepository.java` e `RankingEntry.java` | Preservar contrato e dados, com ajustes somente se necessários para os tipos de leitura. |
| `repository/RankingService.java` | Renomear a cópia para `RankingArquivoRepository`, deixando explícita a implementação em arquivo. Preservar o formato e a ordenação. |
| `tests/` | Criar testes de regras e testes de integração do console/ranking, sem bibliotecas externas. |
| `README.md` e `executar.sh` da fase | Registrar e automatizar compilação, testes e execução independente. |
| `docs/fase-01-tutorial.md` | Explicar a transformação real e permitir reprodução manual. |

Os tipos de leitura não devem entregar objetos mutáveis do domínio para a
apresentação: posições, valores e listas de dados de leitura serão suficientes.
Não é necessário criar uma classe por evento; os tipos relacionados podem ser
agrupados quando isso tornar o código mais simples.

Na fase aprovada, a organização será:

```text
fase-01-refatoracao/
├── README.md
├── executar.sh
├── src/solidexercicio10/
│   ├── Main.java
│   ├── model/
│   ├── presentation/
│   ├── repository/
│   └── service/
├── tests/
└── docs/
    └── fase-01-tutorial.md
```

## 5. Princípios e padrões propostos

- **SRP e alta coesão:** console apresenta, fábrica cria e serviço coordena.
- **GRASP Controller:** `JogoService` recebe ações da aplicação, sem representar
  a tela. A apresentação traduz a interação em chamadas ao serviço.
- **GRASP Information Expert:** posição, capacidade, vidas, embarque e colisões
  continuam próximos dos objetos que já possuem esses dados.
- **DIP e injeção pelo construtor:** manter `RankingRepository`; fornecer relógio
  e aleatoriedade para evitar dependências ocultas e permitir testes repetíveis.
- **Fábrica simples:** concentrar a montagem de `Missao`, sem criar uma hierarquia
  de Abstract Factory ou chamar a extração de um padrão mais complexo.
- **OCP e LSP:** manter a pontuação polimórfica dos passageiros; não substituir
  essa regra por decisões de tipo no serviço.
- **ISP:** manter contratos pequenos; não criar interfaces apenas para envolver
  cada chamada de `Scanner`, impressão ou classe concreta.

Não introduzir Singleton, barramento de eventos ou framework de injeção.

## 6. Sequência de implementação após aprovação

1. Copiar somente fontes necessários, sem ranking, compilados ou caches, e
   confirmar que a cópia compila e inicia isoladamente.
2. Criar cenários de caracterização do comportamento de referência antes das
   mudanças principais. Não usar a documentação antiga como prova de testes.
3. Extrair a fábrica e permitir relógio e sorteio controlados nos testes.
4. Extrair o console e introduzir ações e resultados, mantendo a ordem do turno:
   comando, movimento de inimigos, colisão, esgotamento e vitória.
5. Adaptar a leitura do estado, a renderização e a composição em `Main`.
6. Clarificar o repositório em arquivo, mantendo contrato e compatibilidade do
   formato utilizado pela base SOLID.
7. Executar validações, escrever o tutorial com exemplos reais e atualizar o
   índice em `evolucao-missao-marte/README.md` com comandos verificados.

## 7. Riscos e limites de escopo

| Risco | Como reduzir |
|---|---|
| Trocar a ordem do turno e alterar resultados | Testar colisão, combustível, aborto e vitória com cenários conhecidos. |
| Duplicar regras entre console e serviço | Console apenas traduz entrada e apresenta estado/eventos. |
| Contaminar ranking ou entregas anteriores | Arquivos da fase em seu próprio diretório; testes com repositório em memória e arquivos temporários. |
| Testes dependerem de sorte ou tempo real | Injetar relógio e fonte de aleatoriedade; usar cenários explícitos. |
| A apresentação alterar o modelo | Consultas de leitura sem acesso mutável às entidades. |
| Refatoração virar reescrita | Reaproveitar entidades e regras; extrair responsabilidades em etapas compiláveis. |

Não fazem parte desta fase: Gradle, LibGDX, sprites, servidor, login, JPA,
Android, novas armas, novas regras de vitória, conversão de ranking ou troca de
namespace. Correções de geração de mapas e mudanças na movimentação de inimigos
devem ser propostas separadamente se forem necessárias; não estão autorizadas
implicitamente por uma refatoração.

## 8. Ambiente e validação

Ambiente observado: OpenJDK 25.0.5.0.3; compilação disponível pelo módulo
`jdk.compiler`, mesmo sem `javac` no PATH. A Fase 1 não instalará ferramentas
ou dependências. O script deverá aceitar tanto `javac` quanto a chamada
`java -m jdk.compiler/com.sun.tools.javac.Main`.

**Validação já executada na preparação:**

- Exercício 10 original: 10 fontes compilados com `-Xlint:all`, sem avisos;
  menu, ranking vazio e saída passaram.
- Base SOLID: 18 fontes compilados com `-Xlint:all`, sem avisos; menu,
  ranking vazio e saída passaram.
- Compilados e diretório de execução foram isolados em `/tmp`.
- Não foram encontrados testes automatizados nas bases. Não foram validados
  partida completa, vitória ou persistência durante esta preparação.

**Validação prevista da fase implementada:**

- Compilar todos os fontes e testes somente com arquivos da pasta da fase.
- Testar pontos por dificuldade, quatro direções, bordas e custo por tentativa.
- Testar embarque válido, sem passageiro, nave cheia e bônus polimórfico.
- Testar colisão com asteroide e inimigo, perda das três vidas e pontos zerados.
- Testar resgate de todos longe da base, retorno à origem e gravação única da
  vitória, incluindo tempo e passageiros.
- Testar aborto, comando inválido, entrada vazia e reinício de partida.
- Testar salvar, listar, ordenar e limpar ranking em arquivo temporário no
  formato atual; não tocar nos arquivos da `missao2`.
- Executar fluxo do console com entradas roteirizadas e uma verificação manual
  da apresentação, relatando o que não for possível executar.
- Confirmar ausência de `Scanner`, `System.out` e renderização no serviço e nas
  regras. Verificar que a apresentação não modifica a partida diretamente.

## 9. Critérios de conclusão

- [ ] Jogo de console compila e executa de forma independente na pasta da fase.
- [ ] Serviço pode ser usado e testado sem terminal.
- [ ] Regras de referência preservadas; divergências documentadas.
- [ ] Testes relevantes passam e resultados são informados.
- [ ] Ranking da fase isolado e formato existente preservado.
- [ ] Tutorial contém os 12 itens exigidos no prompt, exemplos antes/depois e
  diagramas quando úteis.
- [ ] Instruções de execução/teste verificadas e índice atualizado.
- [ ] Entregas anteriores e referencial teórico permanecem intactos.
- [ ] Nenhuma fase posterior foi iniciada.

## 10. Aprovação necessária para implementar

A preparação e este plano estão disponíveis para revisão. A regra de execução 8
do [prompt mestre](../PROMPT_MESTRE.md) exige aprovação explícita do plano antes
de criar a pasta da Fase 1 ou copiar/refatorar os fontes. A solicitação atual
autoriza a limpeza e a preparação, que foram realizadas fora das pastas de fase.
