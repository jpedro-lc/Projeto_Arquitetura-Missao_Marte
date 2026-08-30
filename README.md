# 🚀 Missão Marte

**Repositório da Equipe:**  
[https://github.com/jpedro-lc/Projeto_Arquitetura-Missao_Marte.git](https://github.com/jpedro-lc/Projeto_Arquitetura-Missao_Marte.git)

| Nome do Integrante | Matrícula / Usuário Git | Exercícios / Funcionalidades Desenvolvidas |
| :--- | :--- | :--- |
| **José Lucas Queiroz Bastos** | 2517303 <br> Git: `XLILzezim` | Questões 1, 4, 7 e elaboração do arquivo PDF |
| **João Pedro Lima Carvalho** | 2510511 <br> Git: `jpedro-lc` | Questões 2, 5, 8 e 10 |
| **Gleison Gomes Fraga Filho** | 2517333 <br> Git: `gleisongomes-creator` | Questões 3, 6 e 9 |

---

## 🌌 O Enredo (Lore)

A humanidade olha para as estrelas, mas o nosso próximo grande passo está no planeta vermelho. O **Missão Marte** coloca o jogador no comando de uma nave espacial com um objetivo crítico e inadiável: alcançar Marte.  

No entanto, a jornada não é simples. Para que a colonização seja um sucesso, não basta apenas chegar lá; é obrigatório reunir a tripulação essencial, resgatando especialistas (como o Professor e o Engenheiro) perdidos pelo espaço. Tudo isso enquanto a nave navega por campos de asteroides mortais que ameaçam destruir a missão a qualquer instante.

---

## 🎮 Funcionalidades e Mecânicas

- **Navegação 2D:** Controle tático da nave em um mapa bidimensional.
- **Sistema de Pontuação e Gerenciamento:**  
  - Cada movimento realizado consome recursos, custando **1 ponto**.
  - O resgate de passageiros é vital e recompensador, rendendo **+10 pontos** a cada embarque.
- **Coleta de Especialistas:** A missão só tem sucesso com a tripulação certa. Resgate o *Professor* e o *Engenheiro* no decorrer da rota.
- **Campos de Asteroides:** Obstáculos dinâmicos que exigem precisão. Evite o contato a todo custo para manter a integridade da nave.

### 📸 Telas do Jogo

---

## 🏗️ Arquitetura e Orientação a Objetos (POO)

O código foi construído de forma nativa em **Java**, sem uso de frameworks externos, utilizando fortemente os pilares da Programação Orientada a Objetos para garantir um código limpo e modular:

- **Herança:** Compartilhamento de atributos e comportamentos comuns entre entidades do mapa.
- **Polimorfismo:** Tratamento dinâmico de colisões e interações dependendo do objeto (ganho de pontos ao encontrar um passageiro vs. impacto ao atingir um asteroide).
- **Encapsulamento:** Proteção das regras de negócio, coordenadas e pontuação por meio de modificadores de acesso e métodos seguros.
- **Abstração:** Modelagem focada nas características essenciais da nave e do mapa.

---

## ⚙️ Como Executar o Projeto

Como o projeto utiliza Java puro, a execução é simples e direta. Recomendamos o uso do **IntelliJ IDEA**.

### Pré-requisitos
- [Java Development Kit (JDK)](https://www.oracle.com/java/technologies/downloads/) instalado na máquina.
- [IntelliJ IDEA](https://www.jetbrains.com/idea/download/) (Community ou Ultimate).

### Passo a passo

1. **Clone este repositório em sua máquina local:**
   ```bash
   git clone [https://github.com/jpedro-lc/Projeto_Arquitetura-Missao_Marte.git](https://github.com/jpedro-lc/Projeto_Arquitetura-Missao_Marte.git)
   ```

2. **Abra o projeto na IDE:**
   - Abra o IntelliJ IDEA e selecione **Open**.
   - Navegue até a pasta do projeto clonado e confirme.

3. **Indexação:**
   - Aguarde a IDE indexar os arquivos do projeto.

4. **Execução:**
   - No painel lateral esquerdo (*Project*), navegue até encontrar o arquivo principal (geralmente `Main.java` ou `MissaoMarte.java` que contém o método `public static void main`).
   - Clique com o botão direito sobre o arquivo e selecione **Run 'NomeDaClasse.main()'**, ou simplesmente clique no botão de "Play" verde ao lado da declaração do método.
   - O jogo será executado diretamente no terminal/console da IDE.
