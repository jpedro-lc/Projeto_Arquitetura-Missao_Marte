# 🚀 Missão Marte

Jogo em Java desenvolvido na disciplina de **Projeto e Arquitetura de Sistemas
(UNIFOR)**. Começou como um jogo de console e foi evoluindo, fase a fase, até
virar um jogo gráfico em tempo real com LibGDX, servidor de ranking publicado na
nuvem (Spring Boot no Render) e versão Android, sempre preservando as regras do
jogo e aplicando princípios SOLID e padrões de projeto.

> **Jogar agora:** baixe a [Release da apresentação](https://github.com/jpedro-lc/Projeto_Arquitetura-Missao_Marte/releases/tag/v7.0-apresentacao):
> zip do Windows (Java embutido, é só extrair e clicar em `Jogar Missao Marte.bat`) e APK do Android.

**Repositório:** [jpedro-lc/Projeto_Arquitetura-Missao_Marte](https://github.com/jpedro-lc/Projeto_Arquitetura-Missao_Marte)
· **Acompanhamento:** [GitHub Project "Missão Marte — Evolução Arquitetural"](https://github.com/users/jpedro-lc/projects/16)

| Desktop (LibGDX) | Android (celular real) |
|---|---|
| ![Partida no desktop](evolucao-missao-marte/fases/fase-07-evolucao-livre/docs/evidencias/tempo-real/05-inimigos-andando.png) | ![Partida no Android](evolucao-missao-marte/fases/fase-07-evolucao-livre/docs/evidencias/direcional/02-celular-elementos-visiveis-atras-das-setas.png) |

---

## 👥 Equipe

| Integrante | Matrícula / Usuário Git | Contribuições nas missões 1 e 2 |
| :--- | :--- | :--- |
| **José Lucas Queiroz Bastos** | 2517303 <br> Git: `XLILzezim` | Questões 1, 4, 7 e elaboração do arquivo PDF |
| **João Pedro Lima Carvalho** | 2510511 <br> Git: `jpedro-lc` | Questões 2, 5, 8 e 10 |
| **Gleison Gomes Fraga Filho** | 2517333 <br> Git: `gleisongomes-creator` | Questões 3, 6 e 9 |

---

## 🌌 O jogo

A humanidade olha para as estrelas, e o próximo grande passo é o planeta
vermelho. O jogador comanda uma nave que precisa resgatar a tripulação
essencial (astronautas, o **Professor** e o **Engenheiro**) perdida pelo espaço,
desviando de **asteroides** e **inimigos**, e voltar à base antes que acabem
as vidas ou os pontos.

- **Três modos:**
  - **Tempo real** (padrão): os inimigos andam sozinhos e a nave anda enquanto a
    direção está pressionada.
  - **Infinito**: setores em sequência, cada um mais difícil, com pontos e vidas
    acumulados.
  - **Clássico**: por turnos, como nas primeiras fases.
- **Pontuação:** a partida começa com 30, 20 ou 15 pontos (Fácil, Médio,
  Difícil). No tempo real perde 1 ponto por segundo; no clássico, 1 por
  movimento. Cada passageiro embarcado soma a sua pontuação. Chegar a zero é
  derrota.
- **Vidas (corações):** a nave tem 3; colidir com asteroide ou inimigo custa uma.
  No Infinito há vida extra a cada 5 setores, até 5.
- **Vitória:** embarcar todos os passageiros e retornar à base. No Infinito,
  isso leva ao próximo setor.
- **Rankings:** 9 tabelas, uma para cada modo e dificuldade, por pontos. São
  globais (servidor na nuvem) para quem tem conta e locais para convidados.

### 🎮 Controles

| Ação | PC (teclado ou controle) | Celular |
|---|---|---|
| Mover | `W A S D`, setas ou D-Pad (segurar no tempo real) | Direcional em cruz, translúcido sobre o mapa (segurar mantém a nave andando) |
| Embarcar | `C` / botão A | Botão **EMBARCAR** sobre o mapa |
| Pausar | `P` / Start | Botão Pausa ou tecla Voltar |
| Abortar | `Q` / botão B | Botão Abortar |

O servidor gratuito dorme depois de 15 min sem uso. O primeiro login pode levar
até 1 minuto, e o jogo avisa na tela enquanto conecta.

---

## 🧭 Evolução arquitetural

O trabalho foi conduzido em fases, cada uma em sua própria pasta, com plano
aprovado antes da implementação, tutorial didático e integração por pull
request. O guia completo, com os comandos de cada fase, está em
[`evolucao-missao-marte/README.md`](evolucao-missao-marte/README.md).

| Fase | O que entregou | Situação |
|---|---|---|
| [1 — Refatoração](evolucao-missao-marte/fases/fase-01-refatoracao/README.md) | Separação entre regras (`model`, `service`) e console | ✅ 162 verificações |
| [2 — Gradle](evolucao-missao-marte/fases/fase-02-gradle/README.md) | Build com Gradle 8.7, Java 17 e testes JUnit | ✅ |
| [3 — LibGDX](evolucao-missao-marte/fases/fase-03-libgdx/README.md) | Versão gráfica: menu, HUD, pausa, áudio e sprites | ✅ 44 testes |
| [4 — Interface gráfica](evolucao-missao-marte/fases/fase-04-interface-grafica/README.md) | Animações, movimento visual e HUD no estilo SpaceRage | ✅ 48 testes |
| [5 — Servidor e ranking](evolucao-missao-marte/fases/fase-05-servidor-ranking/README.md) | API Spring Boot com contas, JWT, BCrypt, histórico e ranking global | ✅ 27 + 70 testes |
| [6 — Android](evolucao-missao-marte/fases/fase-06-android/README.md) | APK com toque, *swipe* e controle físico, mesmo `core` e mesmo servidor | ✅ 27 + 83 testes, testado em celular |
| [7 — Evolução livre](evolucao-missao-marte/fases/fase-07-evolucao-livre/README.md) | Servidor no Render, tempo real, modo Infinito, 9 rankings, corações, direcional no celular e pacote da apresentação | ✅ 41 + 132 testes, testado em celular |

Os planos de cada fase ficam em [`evolucao-missao-marte/docs/`](evolucao-missao-marte/docs/)
e as regras de trabalho em [`PROMPT_MESTRE.md`](evolucao-missao-marte/PROMPT_MESTRE.md).

---

## 🏗️ Arquitetura atual (Fase 7)

```
servidor/  Spring Boot 4 · JPA (PostgreSQL no Render, H2 local) · Spring Security (JWT HS256, BCrypt) · OpenAPI · Docker
cliente/
├── core/     regras do jogo + telas Scene2D (código compartilhado)
├── lwjgl3/   launcher desktop
└── android/  launcher Android
```

- O `core` mantém as regras (`model`, `service`) sem dependência de LibGDX; a
  apresentação (telas, entrada, rede) fica separada.
- **Padrões aplicados:** Strategy (`RegrasDePartida`: Clássico, Tempo real e
  Infinito), Adapter (`RegrasClassicas`), Port and Adapter (`ServidorDoJogo` / `ServidorHttp`,
  `EntradaJogo` / adaptadores de toque e controle), Composite
  (`EntradaComposta`), Decorator (`RankingSomenteConvidado`), Template Method
  (telas-base), Repository e Composition Root por plataforma.
- **Pilares de POO** desde a Missão 1: herança e polimorfismo nas entidades do
  mapa, encapsulamento das regras e pontuação, abstração da nave e da missão.

**Tecnologias:** Java 17 · Gradle · LibGDX 1.12.1 · Spring Boot 4.1 · PostgreSQL / H2 · Docker · Render ·
Android SDK 34 (minSdk 21) · JUnit 5 · MockMvc.

---

## ⚙️ Como executar a versão mais recente

> **Sem instalar nada (apresentação):** `MissaoMarte-windows.zip` traz o jogo com
> Java embutido, e o APK já aponta para o servidor na nuvem. Passo a passo em
> [`APRESENTACAO.md`](evolucao-missao-marte/fases/fase-07-evolucao-livre/APRESENTACAO.md).

Pré-requisitos: **JDK 17** e, para o Android, o **Android SDK** (Android Studio).
Dentro de `evolucao-missao-marte/fases/fase-07-evolucao-livre/cliente/`, usando o
servidor publicado (`https://missao-marte-servidor.onrender.com`):

```bash
# Jogo no desktop
./gradlew :lwjgl3:run "-Dmissaomarte.servidor=https://missao-marte-servidor.onrender.com"

# Jogo no Android (celular com depuração USB)
./gradlew :android:installDebug "-Pmissaomarte.servidor=https://missao-marte-servidor.onrender.com"
```

Para rodar o servidor localmente: `cd ../servidor && ./gradlew bootRun`, e use
`:lwjgl3:run` sem a opção. Sem servidor, o jogo funciona em **modo convidado**,
com ranking local. Detalhes no
[README da Fase 7](evolucao-missao-marte/fases/fase-07-evolucao-livre/README.md).

---

## 📁 Organização do repositório

| Pasta / arquivo | Conteúdo |
|---|---|
| [`evolucao-missao-marte/`](evolucao-missao-marte/README.md) | Fases 1 a 7 da evolução, planos, tutoriais e evidências |
| [`render.yaml`](render.yaml) | Blueprint do Render: cria o servidor e o banco PostgreSQL na nuvem |
| [`missao2/`](missao2/) | Entrega da Missão 2: versão original e versão revisada com SOLID, UML |
| [`missao1/`](missao1/) | Entrega da Missão 1: primeira versão do jogo em console |
| [`design_patterns/`](design_patterns/) | Estudo de padrões de projeto (Singleton) |
| [`work/`](work/) | Prévia e inspeção dos assets SpaceRage usados na Fase 4 |
| `ReadMe_Missão Marte.pdf` | Documento da entrega original |

As entregas `missao1/` e `missao2/` permanecem preservadas como histórico.
