# Relatório de Revisão Crítica - Refatoração SOLID (Missão Marte)

Este documento apresenta a análise crítica, a avaliação de decisões de projeto, os resultados de testes e as propostas de melhoria para a refatoração do projeto **Missão Marte**, migrado do código inicial para o pacote `solidexercicio10` conforme as diretrizes do tutorial SOLID.

---

## 1. Análise dos Princípios SOLID

### Princípio da Responsabilidade Única (SRP)
* **Local:** Pacote `solidexercicio10.presentation` (classe `MapaRenderer`)
* **Princípio relacionado:** SRP (Single Responsibility Principle)
* **Observação:** No código original, a classe `Main.java` acumulava múltiplas responsabilidades, como o loop do jogo, a leitura de entradas via console, o desenho do mapa em caracteres e o parseamento do arquivo JSON de ranking. A refatoração isolou a renderização do mapa e as mensagens de terminal na camada de apresentação (`presentation`).
* **Impacto para manutenção, testes ou evolução:** Reduz drasticamente a necessidade de alterar classes de serviço ou domínio caso haja mudanças no formato da interface ou no tipo de terminal.
* **Proposta:** Manter o isolamento estrito: a camada de visualização deve apenas receber os dados do estado da missão e exibi-los na tela, sem conter regras lógicas do jogo.
* **Prioridade:** Alta

---

### Princípio do Aberto/Fechado (OCP)
* **Local:** Pacote `solidexercicio10.model` e classe `JogoService`
* **Princípio relacionado:** OCP (Open/Closed Principle)
* **Observação:** No código original, a verificação do tipo de passageiro e a pontuação concedida ao embarcar utilizavam checagens condicionais explícitas baseadas em instâncias (`instanceof`). Na refatoração, o cálculo de pontuação e o símbolo correspondente foram encapsulados via polimorfismo nas subclasses de `Passageiro`.
* **Impacto para manutenção, testes ou evolução:** Facilita a introdução de novos tipos de passageiros (ex: `Cientista`, `Medico`) apenas estendendo a classe base `Passageiro`, sem modificar estruturas de decisão (`switch` ou `if/else`) no fluxo principal.
* **Proposta:** Garantir que métodos de fábrica (*factory methods*) ou enumerações controladas cuidem da instanciação de novos passageiros, evitando acoplamentos rígidos.
* **Prioridade:** Média

---

### Princípio da Substituição de Liskov (LSP)
* **Local:** Subclasses de `Passageiro` (`Professor`, `Engenheiro`, `Astronauta`)
* **Princípio relacionado:** LSP (Liskov Substitution Principle)
* **Observação:** As subclasses de `Passageiro` respeitam integralmente as pré e pós-condições da classe base. A nave e o serviço de missão tratam de forma uniforme qualquer objeto que estenda `Passageiro` durante o embarque e o cálculo de pontuação.
* **Impacto para manutenção, testes ou evolução:** Previne comportamentos inesperados ao iterar sobre coleções genéricas (`List<Passageiro>`), assegurando que qualquer subclasse possa substituir a classe base sem quebrar o sistema.
* **Proposta:** Assegurar que subclasses não lancem exceções não documentadas em contratos herdados e preservar a imutabilidade básica de nomes e coordenadas.
* **Prioridade:** Alta

---

### Princípio da Segregação de Interfaces (ISP)
* **Local:** Pacote `solidexercicio10.repository` (Interface `RankingRepository`)
* **Princípio relacionado:** ISP (Interface Segregation Principle)
* **Observação:** A interface de persistência de ranking expõe estritamente os contratos necessários para a aplicação: `salvar()`, `limpar()` e `listar()`. Não há métodos inflados ou desnecessários impostos às classes consumidoras.
* **Impacto para manutenção, testes ou evolução:** Impede que o `JogoService` ou outras classes fiquem acoplados a operações complexas de disco ou banco de dados que não utilizam.
* **Proposta:** Caso surjam demandas futuras por buscas avançadas ou ordenações complexas no ranking, criar uma interface segregada e especializada (ex: `RankingSearchableRepository`) em vez de expandir a interface principal.
* **Prioridade:** Média

---

### Princípio da Inversão de Dependência (DIP)
* **Local:** `JogoService` / `RankingRepository`
* **Princípio relacionado:** DIP (Dependency Inversion Principle)
* **Observação:** O serviço de jogo depende da abstração `RankingRepository` e não da implementação concreta baseada em manipulação de arquivos JSON (`RankingRepositoryJson`). A injeção da dependência ocorre via construtor na composição realizada em `Main.java`.
* **Impacto para manutenção, testes ou evolução:** Permite alternar facilmente entre o repositório em arquivo JSON, um repositório em memória (`RankingRepositoryMemory`) para testes rápidos, ou um banco relacional em produção sem alterar a regra de negócio central.
* **Proposta:** Centralizar todas as instanciações de infraestrutura e serviços concretos no ponto de entrada da aplicação (`Main.java`).
* **Prioridade:** Alta

---

## 2. Decisão do Tutorial com a qual Concordamos

* **Decisão:** Isolamento da Persistência do Ranking através da interface `RankingRepository` (Camada Repository).
* **Benefício:** No código inicial, o parseamento de JSON e as operações de I/O de arquivos com `java.nio.file` eram acoplados diretamente dentro de métodos estáticos na classe `Main`. Além de misturar regras de infraestrutura com o fluxo de menus, isso tornava a automação de testes inviável sem corromper arquivos reais em disco. O uso da interface viabilizou o isolamento do I/O e permitiu simular a persistência de forma limpa durante os testes unitários.

---

## 3. Propostas de Melhorias Adicionais

1. **Remoção de Código Duplicado na Verificação de Posições do Mapa:**
   * **Descrição:** Encapsular a lógica de validação de posições e checagem de ocupação por entidades (nave, passageiros, asteroides, inimigos) dentro de métodos utilitários do domínio ou da classe `Missao`.
   * **Impacto:** Reduz a repetição de blocos de varredura (*loops*) em etapas de criação, renderização e movimentação.
   * **Prioridade:** Baixa

---

## 4. Testes Realizados e Resultados

A suíte de testes contemplou a verificação do comportamento funcional e a integridade de ponta a ponta do ciclo de vida do jogo:

| Caso de Teste | Descrição | Resultado Esperado | Resultado Obtido | Status |
| :--- | :--- | :--- | :--- | :--- |
| **CT01 - Início de Missão** | Iniciar nova missão definindo nome do piloto, dificuldade e limites do mapa | Mapa gerado perfeitamente dentro das dimensões selecionadas | Instanciado com limites e entidades corretamente posicionados | **PASSOU** |
| **CT02 - Movimentação e Limites** | Tentar mover a nave além das bordas do mapa (`w/a/s/d`) | Bloquear o deslocamento para fora das coordenadas limite | Movimentação restrita às fronteiras estabelecidas da grade | **PASSOU** |
| **CT03 - Embarque de Passageiros** | Posicionar a nave sobre um passageiro e emitir o comando `c` | Recolher o passageiro, somar a pontuação correspondente e removê-lo do mapa | Passageiro coletado com sucesso e score atualizado | **PASSOU** |
| **CT04 - Detecção de Colisão** | Colidir a nave contra um Asteroide (`#`) ou Inimigo (`X`) | Decrementar uma vida da nave e encerrar por Game Over se vidas chegarem a zero | Vida subtraída corretamente e partida finalizada sob esgotamento | **PASSOU** |
| **CT05 - Condição de Vitória** | Coletar todos os passageiros e retornar com segurança à base `(0,0)` | Exibir mensagem de sucesso, solicitar registro de pontuação e atualizar o ranking | Partida concluída e pontuação computada para o Top 5 | **PASSOU** |
| **CT06 - Persistência do Ranking** | Concluir com pontuação elevada e validar o arquivo `ranking.json` | Ordenar as pontuações e armazená-las no arquivo correspondente em disco | Arquivo de ranking gravado e lido corretamente pelo menu | **PASSOU** |
| **CT07 - Reset do Ranking** | Disparar a opção 3 do menu e confirmar a ação de limpeza | Apagar o histórico e zerar a listagem exibida | Histórico limpo com sucesso | **PASSOU** |

---

## 5. Quadro Geral de Prioridades das Melhorias

| Melhoria / Observação | Local / Classe | Princípio Relacionado | Prioridade |
| :--- | :--- | :--- | :--- |
| Isolamento do Renderizador de UI | `presentation/MapaRenderer` | **SRP** | **Alta** |
| Inversão de Dependência na Persistência | `repository/RankingRepository` | **DIP** | **Alta** |
| Emprego de Polimorfismo nos Passageiros (sem `instanceof`) | `model/Passageiro` | **OCP / LSP** | **Alta** |
| Criação de `RankingRepositoryMemory` para Testes | `repository/` | **DIP / Testabilidade** | **Média** |
| Simplificação do manuseio de I/O no Console | `presentation/` | **ISP / Pragmatismo** | **Média** |
| Refatoração de checagem de coordenadas no mapa | `model/Missao` | **Clean Code** | **Baixa** |
