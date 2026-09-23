# Relatório de Revisão Crítica - Refatoração SOLID (Missão Marte Unifor)

Este documento apresenta a análise crítica, avaliação de decisões de projeto, resultados de testes e propostas de melhoria para a refatoração do projeto **Missão Marte Unifor**, migrado do código monolítico inicial para o pacote `solidexercicio10`.

---

## 1. Análise dos Princípios SOLID

### Princípio da Responsabilidade Única (SRP)
* **Local:** Pacote `solidexercicio10.presentation` (classe `MapaRenderer`)
* **Princípio relacionado:** SRP (Single Responsibility Principle)
* **Observação:** No código monolítico original, a classe `Main.java` cuidava do loop do jogo, leitura de input, desenho do mapa em caracteres ASCII e parseamento de arquivo JSON do ranking. A refatoração isolou o desenho do mapa e as mensagens de terminal na camada de apresentação (`presentation`).
* **Impacto para manutenção, testes ou evolução:** Reduz drasticamente a necessidade de alterar classes de serviço ou domínio se mudarmos o formato de interface (por exemplo, de terminal para uma GUI em JavaFX ou Swing).
* **Proposta:** Manter o isolamento estrito: a camada de visualização apenas recebe os dados do estado da missão e exibe na tela, sem realizar operações lógicas do jogo.
* **Prioridade:** Alta

---

### Princípio do Aberto/Fechado (OCP)
* **Local:** Pacote `solidexercicio10.model` e classe `JogoService`
* **Princípio relacionado:** OCP (Open/Closed Principle)
* **Observação:** A verificação do tipo de passageiro e a pontuação concedida ao embarcar utilizavam verificações condicionais explícitas (`instanceof`) no código original. Com a refatoração, o método de pontuação e comportamento foi encapsulado no polimorfismo das subclasses da classe base `Passageiro`.
* **Impacto para manutenção, testes ou evolução:** Permite a adição de novos tipos de passageiros (ex: `Cientista`, `Medico`) apenas estendendo a classe base `Passageiro`, sem a necessidade de modificar estruturas `switch` ou `if/else` dentro do loop principal da partida.
* **Proposta:** Garantir que factory methods ou enumerações controladas cuidem da instanciação dos novos passageiros sem acoplar a lógica de negócio à criação de instâncias.
* **Prioridade:** Média

---

### Princípio da Substituição de Liskov (LSP)
* **Local:** Subclasses de `Passageiro` (`Professor`, `Engenheiro`, `Astronauta`)
* **Princípio relacionado:** LSP (Liskov Substitution Principle)
* **Observação:** As subclasses de `Passageiro` não alteram nem violam as pré/pós-condições da classe base. A nave e o serviço de missão tratam qualquer objeto que estenda `Passageiro` de forma uniforme ao realizar o embarque e calcular a pontuação.
* **Impacto para manutenção, testes ou evolução:** Evita comportamentos inesperados ao iterar sobre coleções de passageiros (`List<Passageiro>`), garantindo que qualquer subclasse possa ser substituída sem quebrar o fluxo.
* **Proposta:** Evitar que subclasses lancem exceções não esperadas em métodos herdados de `Passageiro` e manter os contratos de imutabilidade de nome e coordenadas funcionais.
* **Prioridade:** Alta

---

### Princípio da Segregação de Interfaces (ISP)
* **Local:** Pacote `solidexercicio10.repository` (Interface `RankingRepository`)
* **Princípio relacionado:** ISP (Interface Segregation Principle)
* **Observação:** A interface de persistência de ranking fornece estritamente as operações necessárias para a aplicação: `carregar()`, `salvar()` e `resetar()`. Não há métodos desnecessários impostos às classes consumidoras ou implementadoras.
* **Impacto para manutenção, testes ou evolução:** Impede que o `JogoService` ou a classe `Main` fiquem acoplados a operações desnecessárias de banco de dados/arquivos.
* **Proposta:** Se no futuro houver necessidade de buscas avançadas ou ordenações complexas no ranking, criar uma interface especializada (ex: `RankingSearchableRepository`) em vez de inflar a interface principal.
* **Prioridade:** Média

---

### Princípio da Inversão de Dependência (DIP)
* **Local:** `JogoService` / `RankingRepositoryJson`
* **Princípio relacionado:** DIP (Dependency Inversion Principle)
* **Observação:** O serviço do jogo e o fluxo da partida dependem da abstração `RankingRepository` e não da implementação concreta que manipula arquivos JSON (`RankingRepositoryJson`). A injeção da dependência é feita via construtor na composição em `Main.java`.
* **Impacto para manutenção, testes ou evolução:** Permite substituir a persistência baseada em arquivo JSON por um repositório em memória (`RankingRepositoryMemory`) para testes unitários ou por um banco de dados relacional em produção sem alterar uma linha da regra do jogo.
* **Proposta:** Manter todas as instanciações de serviços concretos centralizadas no ponto de entrada (`Main.java` / classe de configuração).
* **Prioridade:** Alta

---

## 2. Decisão do Tutorial com a qual Concordo

* **Decisão:** Isolamento da Persistência do Ranking atrás da interface `RankingRepository` (Camada Repository).
* **Benefício:** No código inicial, o parseamento de JSON e a escrita em disco com `java.nio.file` eram feitos por métodos estáticos dentro da classe `Main`. Além de misturar I/O com fluxo de menu, a testabilidade era praticamente impossível sem criar/deletar arquivos reais em disco durante a execução dos testes. Com a interface `RankingRepository`, foi possível isolar os erros de I/O em uma classe própria e mockar a persistência durante testes das regras de vitória e pontuação.

---

## 3. Decisão com a qual NÃO Concordo (ou Implementaria de Outra Forma)

* **Decisão:** Sugestão de divisão excessiva de pacotes/classes de leitura do Console na camada de apresentação (`presentation`) para uma aplicação de terminal simples.
* **Justificativa:** Para um projeto deste porte (jogo de console simplificado), criar interfaces e múltiplas abstrações para a leitura das entradas via `Scanner` adiciona um nível de indireção desnecessário (*overengineering*). A leitura via `Scanner` no console já é uma abstração direta do sistema operacional.
* **Abordagem alternativa:** Manter uma única classe utilitária de entrada/saída de console (`ConsoleUi` ou `TerminalView`) manipulando o `Scanner` diretamente, evitando interfaces e fábricas de input que não agregam valor prático e aumentam a quantidade de arquivos para manter.

---

## 4. Propostas de Melhorias Adicionais

1. **Implementação de `RankingRepositoryMemory` para Testes Unitários:**
   * **Descrição:** Criar uma implementação de repositório em memória sem dependência do sistema de arquivos.
   * **Impacto:** Testes automatizados executam instantaneamente e sem efeitos colaterais no disco.
   * **Prioridade:** Média

2. **Remoção de Código Duplicado na Verificação de Posições do Mapa:**
   * **Descrição:** Encapsular a lógica de validação de posições válidas/ocupadas (nave, passageiros, asteroides e inimigos) em uma classe utilitária do domínio ou dentro do próprio objeto `Mapa`.
   * **Impacto:** Evita duplicação de loops em `criarNovaMissao`, `desenharMapa` e movimentação das entidades.
   * **Prioridade:** Baixa

---

## 5. Testes Realizados e Resultados

Os testes cobriram tanto o comportamento funcional quanto a integridade do ciclo de vida da aplicação:

| Caso de Teste | Descrição | Resultado Esperado | Resultado Obtido | Status |
| :--- | :--- | :--- | :--- | :--- |
| **CT01 - Início de Missão** | Iniciar nova missão escolhendo nome, dificuldade e tamanho do mapa | Mapa gerado corretamente dentro das dimensões escolhidas | Mapa instanciado com os limites e entidades configurados | **PASSOU** |
| **CT02 - Movimentação e Limites** | Mover a nave contra a borda do mapa (`w/a/s/d`) | A nave não deve ultrapassar as coordenadas limite (`minX`, `maxX`, etc.) | Movimento bloqueado na borda mantendo a nave dentro da grade | **PASSOU** |
| **CT03 - Embarque de Passageiros** | Mover até a posição do passageiro e enviar comando `c` | Passageiro embarca na nave, soma pontos e desaparece do mapa | Passageiro coletado com sucesso e pontuação atualizada | **PASSOU** |
| **CT04 - Detecção de Colisão** | Colidir a nave com Asteroide (`#`) ou Inimigo (`X`) | Reduzir 1 vida da nave. Encerrar jogo com Game Over se vidas chegar em 0 | Vida deduzida corretamente e término de partida se zerado | **PASSOU** |
| **CT05 - Condição de Vitória** | Coletar todos os passageiros e retornar à base `(0,0)` | Exibir mensagem de sucesso, pedir registro de score e atualizar ranking | Partida concluída com sucesso e score avaliado para o Top 5 | **PASSOU** |
| **CT06 - Persistência do Ranking** | Concluir partida com pontuação alta e verificar `ranking.json` | O ranking deve ser ordenado por pontuação e salvo no arquivo JSON | Arquivo `ranking.json` atualizado e lido corretamente no menu 2 | **PASSOU** |
| **CT07 - Reset do Ranking** | Executar a opção 3 do menu e confirmar a operação | Arquivo de ranking apagado/limpo e lista zerada no menu | Histórico limpo com sucesso | **PASSOU** |

---

## 6. Quadro Geral de Prioridades das Melhorias

| Melhoria / Observação | Local / Classe | Princípio Relacionado | Prioridade |
| :--- | :--- | :--- | :--- |
| Separação do Renderizador de UI | `presentation/ConsoleUi` | **SRP** | **Alta** |
| Inversão de Dependência na Persistência | `repository/RankingRepository` | **DIP** | **Alta** |
| Polimorfismo nos Passageiros sem `instanceof` | `model/Passageiro` | **OCP / LSP** | **Alta** |
| Criação de `RankingRepositoryMemory` para Testes | `repository/` | **DIP / Testabilidade** | **Média** |
| Simplificação do manuseio de I/O no Console | `presentation/` | **ISP / Pragmatismo** | **Média** |
| Refatoração de verificação de coordenadas de mapa | `model/Missao` | **Clean Code** | **Baixa** |
