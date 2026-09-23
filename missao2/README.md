# Missão Marte Unifor — Refatoração SOLID

## Alunos

- **José Lucas Queiroz Bastos** — Matrícula: 2517303
- **João Pedro Lima Carvalho** — Matrícula: 2510511
- **Gleison Gomes Fraga Filho** — Matrícula: 2517333

---

## Como compilar

O projeto foi desenvolvido em Java.

A partir do diretório raiz do projeto, execute:

```bash
mkdir -p out
javac -d out $(find solidexercicio10 -name "*.java")
```

O comando irá compilar todos os arquivos `.java` do projeto e colocar os arquivos compilados no diretório `out`.

É necessário possuir o JDK instalado no computador.

Para verificar a instalação do Java:

```bash
java -version
```

e:

```bash
javac -version
```

---

## Como executar

Após a compilação, execute a classe principal com:

```bash
java -cp out solidexercicio10.Main
```

A aplicação será iniciada no terminal.

---

## Alterações realizadas

O projeto original apresentava diversas responsabilidades concentradas na classe `Main`, incluindo o controle do fluxo do jogo, apresentação do mapa, gerenciamento das entidades e persistência do ranking.

A refatoração teve como objetivo separar essas responsabilidades e aplicar os princípios SOLID.

### Separação das responsabilidades

A estrutura foi reorganizada nos seguintes pacotes:

```text
solidexercicio10
├── model
├── presentation
├── repository
└── service
```

O pacote `model` concentra as entidades e elementos relacionados ao domínio do jogo, como:

- `Asteroide`
- `Astronauta`
- `Dificuldade`
- `Engenheiro`
- `EntidadeMapa`
- `Inimigo`
- `Missao`
- `Movel`
- `Nave`
- `Passageiro`
- `PosicaoNivel`
- `Professor`

O pacote `service` concentra as regras e operações principais da aplicação, principalmente na classe `JogoService`.

O pacote `presentation` ficou responsável pela apresentação da aplicação no terminal, principalmente por meio da classe `MapaRenderer`.

O pacote `repository` ficou responsável pela persistência do ranking, contendo:

- `RankingEntry`
- `RankingRepository`
- `RankingService`

### Aplicação do SRP

A responsabilidade de apresentação do mapa foi separada das regras de negócio por meio da classe `MapaRenderer`.

Com isso, a lógica responsável por desenhar o mapa não precisa permanecer junto das regras de execução da missão.

### Aplicação do OCP

O comportamento relacionado aos diferentes tipos de passageiros foi organizado utilizando herança e polimorfismo.

As classes `Professor`, `Engenheiro` e `Astronauta` são especializações de `Passageiro`.

Dessa forma, novos tipos de passageiros podem ser adicionados por meio de novas subclasses, reduzindo a necessidade de modificar a lógica principal do jogo.

### Aplicação do LSP

As subclasses de `Passageiro` podem ser utilizadas onde um objeto do tipo `Passageiro` é esperado.

A lógica do jogo pode trabalhar com a abstração `Passageiro` sem precisar depender diretamente de cada tipo concreto.

### Aplicação do ISP

A persistência do ranking foi definida por meio da interface `RankingRepository`, contendo somente as operações necessárias para o gerenciamento do ranking.

As operações relacionadas ao ranking são separadas da implementação responsável pelo armazenamento dos dados.

### Aplicação do DIP

O serviço responsável pelas regras do jogo utiliza a abstração `RankingRepository` em vez de depender diretamente da implementação concreta da persistência.

A implementação concreta utilizada atualmente é `RankingService`.

Essa organização permite substituir futuramente a forma de armazenamento do ranking sem precisar alterar diretamente as regras principais do jogo.

---

## Decisões de projeto

### Separação da persistência do ranking

Uma das principais decisões foi retirar a responsabilidade de leitura e escrita do ranking da classe principal e colocá-la no pacote `repository`.

A interface:

```java
RankingRepository
```

define as operações necessárias para o gerenciamento do ranking, enquanto:

```java
RankingService
```

é responsável pela implementação da persistência.

Essa decisão reduz o acoplamento entre as regras do jogo e o mecanismo utilizado para armazenar os dados.

### Separação da apresentação

Foi criada a classe:

```java
MapaRenderer
```

no pacote `presentation`.

Sua responsabilidade é apresentar o estado do mapa no terminal, evitando que essa responsabilidade fique misturada com as regras do jogo.

### Utilização de polimorfismo para passageiros

Foi decidido utilizar a classe `Passageiro` como abstração para os diferentes tipos de passageiros.

As especializações:

```text
Passageiro
├── Professor
├── Engenheiro
└── Astronauta
```

permitem representar diferentes comportamentos sem concentrar todas as verificações de tipo em uma única classe.

### Evitar abstrações desnecessárias para entrada no console

Não foi criada uma grande quantidade de interfaces ou classes apenas para encapsular a leitura do `Scanner`.

Como o projeto é uma aplicação de terminal de pequeno porte, foi considerada mais adequada uma abordagem simples para a entrada e saída de dados, evitando uma quantidade excessiva de abstrações que não trariam benefícios proporcionais ao tamanho do projeto.

---

## Limitações que permanecem

Apesar da refatoração, algumas limitações permanecem no projeto:

- A aplicação continua sendo executada por meio do terminal.
- A persistência do ranking ainda depende de arquivo.
- Não existe atualmente uma implementação de `RankingRepository` em memória para testes.
- Os testes automatizados podem ser ampliados.
- Algumas verificações relacionadas às posições e ocupação do mapa ainda podem ser melhor centralizadas.
- A aplicação ainda não possui uma interface gráfica.
- A persistência poderia futuramente ser substituída por um banco de dados sem alterar as principais regras do jogo, mas essa implementação ainda não faz parte desta versão.

---

## Diagramas UML

Os diagramas UML do projeto estão disponíveis no diretório:

```text
docs/uml/
```

### Diagrama de Classes

O diagrama de classes representa a estrutura estática do sistema.

Ele apresenta as principais classes do projeto, seus atributos, métodos e relacionamentos, incluindo as relações entre:

```text
Passageiro
Professor
Engenheiro
Astronauta
Nave
Missao
JogoService
RankingRepository
RankingService
```

O objetivo é demonstrar como as classes estão organizadas e como ocorre a relação entre as entidades e os componentes responsáveis pela execução do jogo.

### Diagrama de Pacotes

O diagrama de pacotes representa a organização da versão refatorada do projeto.

São apresentados os principais pacotes:

```text
solidexercicio10
├── model
├── presentation
├── repository
└── service
```

O diagrama mostra as dependências entre esses pacotes e permite visualizar a separação das responsabilidades realizada durante a refatoração.

### Diagrama de Arquitetura/Dependências

Caso presente no diretório de diagramas, esse diagrama representa de forma geral o relacionamento entre as principais partes da aplicação, destacando a separação entre apresentação, regras de negócio, modelo e persistência.
