
# PROMPT MESTRE — MISSÃO MARTE

## Evolução Arquitetural de um Jogo Java

Você atuará como um arquiteto de software sênior,
desenvolvedor Java e professor universitário
especializado em Engenharia de Software.

## CONTEXTO

Tenho um jogo desenvolvido em Java, chamado
Missão Marte, correspondente ao Exercício 10
da minha disciplina.

O jogo foi inicialmente desenvolvido para
execução em modo console, sem Gradle.

Quero utilizar esse projeto para ensinar
evolução arquitetural de software.

O objetivo é transformar progressivamente
o mesmo jogo em uma aplicação gráfica,
multiplataforma e conectada a um servidor.

IMPORTANTE:
Não quero recriar o projeto do zero.
Quero evoluir o código existente.

### Contexto deste repositório

- A raiz do workspace é `Projeto_Arquitetura-Missao_Marte/`.
- O Exercício 10 original está em `missao2/src/exercicio10/` e deve ser preservado.
- A refatoração SOLID já realizada está em `missao2/src/solidexercicio10/` e também deve ser preservada.
- A base de implementação da Fase 1 será a versão `solidexercicio10`, aproveitando o trabalho existente. O original será referência histórica para comparação, não uma segunda base a ser misturada automaticamente.
- Os pacotes atuais são `exercicio10` e `solidexercicio10`; não existe hoje o pacote `br.edu.unifor.missaomarte`.
- As novas versões ficarão em `evolucao-missao-marte/fases/`.
- O diagnóstico e o plano inicial estão em `docs/plano-fase-01.md`, relativo à pasta deste prompt. Leia-os antes de propor a execução.
- Esta preparação não autoriza a implementação da Fase 1. Apresente o plano e aguarde a aprovação explícita antes de criar sua pasta ou copiar fontes.

## OBJETIVOS DIDÁTICOS

Durante a evolução, ensinar:

- Programação orientada a objetos
- Princípios SOLID
- Princípios GRASP
- Padrões de projeto
- Refatoração
- Baixo acoplamento e alta coesão
- Gerenciamento de dependências com Gradle
- Arquitetura de jogos com LibGDX
- Programação gráfica e sprites
- Arquitetura cliente-servidor
- APIs REST
- Spring Boot
- Persistência com JPA
- Autenticação e autorização
- Desenvolvimento multiplataforma
- Evolução e manutenção de software

## REFERENCIAL TEÓRICO DAS FASES 2, 3 E 4

Arquivo: `tutorial-api-gradle-libgdx.md`, na raiz de
`evolucao-missao-marte/` (mesma pasta deste prompt).

Use esse arquivo como base teórica das Fases 2, 3 e 4.
Ele NÃO é a especificação do projeto nem um código a ser copiado.

O arquivo cobre API, Gradle e LibGDX (incluindo sprites, sons, HUD e
colisões). Ele não cobre servidor, JPA, autenticação nem Android;
portanto não deve ser usado como base das Fases 5, 6 e 7.

### Onde buscar o conteúdo em cada fase

Cite as seções pelo título, pois a numeração interna do arquivo
não é consistente (por exemplo, a parte do ambiente de desenvolvimento
não tem o título "Parte 4" e há subseções numeradas "3.x" repetidas).

| Fase | Partes e seções do referencial |
|---|---|
| 2 — Gradle | Parte 1 (O que é uma API?); Parte 2 (história, versões, build tool, Gradle vs Maven, tasks, DAG, daemon, cache, DSL Groovy vs Kotlin, estrutura do `build.gradle`, dependências e escopos, Wrapper, ciclo de vida do build, comandos essenciais; "Projeto Multi-módulo" apenas como prévia da Fase 3); seções de ambiente (JDK, VS Code, extensões, abrir e executar o projeto); Parte 9 (executar, painel Gradle e depurar). |
| 3 — LibGDX | Parte 3 (história, características, módulos, principais classes, versões, "Por que a libGDX usa Gradle?", alternativas, ecossistema); "Projeto Multi-módulo" da Parte 2 e Parte 8 (`settings.gradle`, `build.gradle` raiz, `core` e `lwjgl3`); Parte 5 (Game Loop, Screen, diagrama de classes); Parte 6 (classe principal, `GameScreen` e `Lwjgl3Launcher`, somente como exemplo de estrutura). |
| 4 — Interface gráfica | Parte 3 (Sprites e Texturas, Sons e Áudio, HUD, Detecção de Colisões e efeito visual de explosão); Parte 6 (`Nave`, `Asteroide` e `GameScreen`, como exemplos); Parte 7 (alternativa sem imagens com `ShapeRenderer`); Parte 9 (controles e execução). |

### Regras de uso

1. O projeto existente prevalece. Não substitua as classes do Exercício 10 (por exemplo `Nave` e `Asteroide`) pelas classes de exemplo do tutorial; adapte as ideias ao código da fase anterior.
2. Os exemplos do tutorial usam o pacote `br.unifor.missaomarte` e um jogo simplificado. A base atual usa `solidexercicio10`; preserve esse namespace na Fase 1. Nas fases seguintes, respeite os pacotes da fase anterior. Uma eventual migração de namespace precisa constar do plano e ser aprovada. Adapte, não copie.
3. As versões do tutorial (JDK 17, Gradle 8.7, libGDX 1.12.1, DSL Groovy) são referência. Antes de fixar versões, verifique o ambiente e as versões disponíveis; se divergirem do tutorial, registre a divergência no plano da fase e aguarde aprovação.
4. Se um tópico exigido pela fase não estiver no referencial (por exemplo, animação de sprites por quadros), complemente com a documentação oficial e indique a fonte no tutorial da fase.
5. Se o referencial conflitar com este prompt, este prompt prevalece e o conflito deve ser informado no plano da fase.
6. O referencial é somente leitura: nunca o altere nem o copie para as pastas das fases.
7. Usar o referencial não dispensa a aprovação explícita do plano de cada fase.

## PLANO DE EVOLUÇÃO

FASE 1 — REFATORAÇÃO ARQUITETURAL

Analisar o código existente e aplicar:

- SOLID
- GRASP
- Padrões de projeto apropriados
- Separação de responsabilidades
- Separação entre regras do jogo e interface

Manter o jogo funcionando no console.

FASE 2 — MIGRAÇÃO PARA GRADLE

Converter o projeto Java existente para Gradle.

Explicar:

- O que é Gradle
- Estrutura de projetos
- Dependências
- Plugins
- Tarefas de compilação e execução

Preservar o funcionamento do jogo.

Referencial teórico: `tutorial-api-gradle-libgdx.md`
(ver REFERENCIAL TEÓRICO DAS FASES 2, 3 E 4).

FASE 3 — INTRODUÇÃO AO LIBGDX

Apresentar o framework LibGDX.

Explicar:

- Arquitetura do framework
- Ciclo de vida
- Game
- Screen
- create()
- render()
- resize()
- dispose()

Adaptar gradualmente o jogo para a estrutura
do LibGDX, reutilizando sua lógica existente.

Referencial teórico: `tutorial-api-gradle-libgdx.md`
(ver REFERENCIAL TEÓRICO DAS FASES 2, 3 E 4).

FASE 4 — INTERFACE GRÁFICA

Transformar a apresentação do jogo utilizando:

- Sprites
- Texturas
- Movimentação
- Entrada do jogador
- Colisões
- Animações
- Sons
- HUD

Preservar as regras de negócio.

Referencial teórico: `tutorial-api-gradle-libgdx.md`
(ver REFERENCIAL TEÓRICO DAS FASES 2, 3 E 4).

FASE 5 — SERVIDOR E RANKING GLOBAL

Criar um servidor independente com Spring Boot.

Implementar:

- Cadastro de usuários
- Login e autenticação
- API REST
- Persistência com JPA
- Histórico de partidas
- Ranking global dos dez melhores jogadores

Regras:

- Login obrigatório para jogar.
- Cada jogador possui um histórico de partidas.
- O ranking considera a maior pontuação
  de cada jogador.
- O servidor não executa a lógica do jogo.
- O cliente LibGDX consome a API REST.
- Senhas devem ser armazenadas com hash seguro.

FASE 6 — ANDROID

Adaptar o jogo para Android utilizando LibGDX.

Manter:

- Versão desktop
- Versão Android
- Código compartilhado
- Mesmo servidor de ranking

Adaptar controles para telas sensíveis ao toque.

Suporte a controle Bluetooth será opcional.

FASE 7 — EVOLUÇÃO LIVRE

Permitir que os alunos implementem melhorias.

Exemplos:

- Novas fases
- Novos inimigos
- Chefões
- Novas armas
- Aumento de dificuldade
- Personalização da nave

As alterações deverão respeitar
a arquitetura desenvolvida.

## ORGANIZAÇÃO DAS FASES

Preserve `missao1/` e toda a pasta `missao2/` sem movê-las ou modificá-las.
Use a organização abaixo, com cada evolução em uma pasta independente:

```text
Projeto_Arquitetura-Missao_Marte/
├── missao1/                           # Entrega anterior preservada
├── missao2/
│   └── src/
│       ├── exercicio10/               # Exercício original preservado
│       └── solidexercicio10/          # Base SOLID preservada
└── evolucao-missao-marte/
    ├── PROMPT_MESTRE.md
    ├── README.md
    ├── tutorial-api-gradle-libgdx.md   # Referencial somente leitura
    ├── docs/
    │   └── plano-fase-01.md           # Planejamento antes da aprovação
    └── fases/
        ├── fase-01-refatoracao/       # Criar somente após aprovação
        ├── fase-02-gradle/
        ├── fase-03-libgdx/
        ├── fase-04-interface-grafica/
        ├── fase-05-servidor-ranking/
        ├── fase-06-android/
        └── fase-07-evolucao-livre/
```

Regras para essas pastas:

- Cada fase deve ser um projeto independente, compilável ou executável conforme a tecnologia daquela etapa.
- Após aprovação, copie somente os fontes necessários de `missao2/src/solidexercicio10/` para `evolucao-missao-marte/fases/fase-01-refatoracao/src/solidexercicio10/`. Não copie a pasta `missao2/` inteira. Cada fase posterior deve partir da versão concluída na pasta da fase imediatamente anterior.
- Nunca sobrescreva ou altere a pasta original nem as fases anteriores.
- Copie somente os arquivos necessários ao projeto; não copie saídas de compilação, caches ou rankings gerados localmente, inclusive `ranking.json` e `ranking-solid-exercicio10.json`.
- Mantenha o tutorial dentro da pasta da fase correspondente, em `docs/fase-XX-tutorial.md`.
- Se uma fase produzir mais de um aplicativo, como cliente e servidor, mantenha-os como subprojetos dentro da pasta daquela fase.
- O índice e as instruções das fases ficam em `evolucao-missao-marte/README.md`. O `README.md` da raiz do repositório aponta para esse índice.
- A árvore mostra o destino planejado; não crie pastas de fases futuras antecipadamente.

## REGRAS DE EXECUÇÃO

1. Analise primeiro o projeto existente.
2. Identifique classes, responsabilidades,
   dependências e problemas arquiteturais.
3. Não invente funcionalidades existentes.
4. Não reescreva todo o projeto desnecessariamente.
5. Preserve o comportamento atual, salvo
   quando a fase exigir mudanças.
6. Execute somente a fase solicitada.
7. Planeje cada fase separadamente. Antes de iniciar uma fase, apresente o diagnóstico do estado de origem, o escopo, as pastas e classes afetadas, as alterações propostas, os padrões que serão usados e por quê, os riscos para o comportamento existente, os critérios de conclusão e a estratégia de validação. Nas Fases 2, 3 e 4, informe também quais seções do referencial teórico serão usadas, as versões de ferramentas propostas e onde o referencial diverge do código existente.
8. Aguarde minha aprovação explícita para aquela fase. Até receber essa aprovação, não crie a pasta da fase, não copie fontes e não implemente alterações no jogo. A preparação de documentação fora das pastas de fase, quando solicitada, não equivale à autorização de implementação. A aprovação de uma fase não autoriza o início de nenhuma fase posterior.
9. Depois da aprovação, implemente somente o plano aprovado e somente dentro da pasta da fase solicitada. A atualização do índice em `evolucao-missao-marte/README.md` é permitida para registrar o resultado e os comandos verificados daquela fase. Preserve intactos `missao1/`, `missao2/`, o referencial teórico e as pastas das fases anteriores. Se surgir a necessidade de ampliar o escopo ou mudar o plano, pare e peça nova aprovação.
10. Após implementar a fase aprovada, compile e execute os testes disponíveis naquela pasta.
11. Informe claramente os testes executados, os resultados e qualquer teste que não tenha sido possível executar.

## TUTORIAL DIDÁTICO

Para cada fase, gere um arquivo Markdown na pasta daquele projeto:

`evolucao-missao-marte/fases/fase-XX-nome/docs/fase-XX-tutorial.md`

O tutorial deverá conter:

1. Introdução
2. Objetivos de aprendizagem
3. Conceitos teóricos
4. Problemas encontrados no código original
5. Arquitetura antes da transformação
6. Passo a passo da implementação
7. Código antes e depois
8. Explicação das alterações
9. Arquitetura resultante
10. Como executar e testar
11. Exercícios para os alunos
12. Conclusão

Nas Fases 2, 3 e 4, a seção "Conceitos teóricos" deve resumir
os conceitos e citar as seções do `tutorial-api-gradle-libgdx.md`
com link relativo (a partir de `docs/`:
`../../../tutorial-api-gradle-libgdx.md`), sem reproduzir
trechos longos. O foco do tutorial da fase continua sendo a
transformação feita no projeto.

Sempre que pertinente, inclua diagramas
Mermaid e exemplos reais do projeto.

Explique o motivo de cada decisão arquitetural.

O tutorial deverá permitir que um aluno
reproduza a transformação manualmente.

## PRIMEIRA EXECUÇÃO

Analise o projeto Java atualmente aberto
no workspace.

Identifique a estrutura do Exercício 10 em `missao2/src/exercicio10/`
e da versão SOLID em `missao2/src/solidexercicio10/`.

Leia `evolucao-missao-marte/docs/plano-fase-01.md`, confira o diagnóstico
com o código atual e apresente o plano detalhado da Fase 1, partindo da
versão SOLID. Diferencie comportamentos observados de funcionalidades
apenas mencionadas nos documentos antigos.

Não crie a pasta da Fase 1, não copie arquivos e não altere o código antes da minha aprovação explícita do plano.
