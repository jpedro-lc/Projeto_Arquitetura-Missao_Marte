# Resumo da Fase 7: o que há de novo no Missão Marte

A Fase 7 (evolução livre) levou o jogo para a nuvem e mudou o jeito de jogar.
O jogo deixou de ser por turnos e passou a ter três modos, 9 rankings, vidas em
corações, controles na tela do celular e uma versão que roda em qualquer Windows
sem instalar nada.

**Baixar e jogar:** [Release v7.0-apresentacao](https://github.com/jpedro-lc/Projeto_Arquitetura-Missao_Marte/releases/tag/v7.0-apresentacao).

---

## 1. Servidor na nuvem

| | Antes (Fase 6) | Agora (Fase 7) |
|---|---|---|
| Onde roda | No PC do aluno (`localhost:8080`) | No **Render**: `https://missao-marte-servidor.onrender.com` |
| Banco | H2 num arquivo do PC | **PostgreSQL** no Render |
| Celular online | Só com cabo USB e `adb reverse` | De qualquer lugar, no Wi-Fi ou nos dados móveis |

**Como funciona:**
- O servidor roda num contêiner Docker. O `render.yaml`, na raiz do repositório,
  cria o serviço e o banco.
- A porta, o endereço do banco e o segredo dos tokens vêm de variáveis de
  ambiente. Nada disso fica no código.
- O plano gratuito "dorme" após 15 min sem uso. Ao abrir, o jogo chama
  `/api/saude` para acordar o servidor e espera até 70 s pela resposta. No login
  aparece "Conectando... pode levar até 1 min.".

## 2. Três modos de jogo

O modo é escolhido na tela **Nova partida**, junto com a dificuldade.

### Tempo real (novo padrão)
- **Largada:** contagem **3, 2, 1, VAI!**.
- **Nave:** anda uma casa a cada 0,15 s **enquanto a direção estiver
  pressionada**. Um toque rápido anda uma casa.
- **Inimigos:** andam sozinhos, um passo a cada 0,9 s (Fácil), 0,7 s (Médio) ou
  0,5 s (Difícil).
- **Pontos:** começam em 30, 20 ou 15 e caem **1 por segundo**.
- **Embarque:** automático, ao passar pelo passageiro.
- **Colisão:** tira 1 vida e deixa a nave protegida por 1,5 s.
- **Pausa:** congela tudo, inclusive quando o celular vai para segundo plano.

### Infinito (novo)
- Escolhe-se a dificuldade e joga-se **setor após setor** (todos em tempo real).
- Para concluir um setor, é preciso embarcar todos os passageiros e voltar à
  base. Aí começa o próximo, com nova contagem e "SETOR n" na tela.
- **Pontos e vidas passam adiante.** Cada setor dá uma recarga de 30/20/15
  pontos, e nos setores 6, 11, 16… ganha-se 1 vida, até 5.
- **A cada setor fica mais difícil:**
  - +1 inimigo a cada 2 setores;
  - +1 asteroide a cada 3 setores;
  - inimigos 8% mais rápidos;
  - o mapa cresce a cada 4 setores.
- A corrida termina quando acabam as vidas ou os pontos. Ficam registrados os
  **pontos** e o **setor alcançado**.

### Clássico
É o jogo por turnos das fases anteriores, sem mudanças: cada tecla é uma jogada,
e os inimigos andam junto com ela.

## 3. Rankings: 9 tabelas

Há uma tabela para cada **modo** (Tempo real, Infinito, Clássico) e
**dificuldade** (Fácil, Médio, Difícil), todas ordenadas por **pontos**.

- No Tempo real e no Clássico contam as vitórias.
- No Infinito conta toda corrida terminada, e a tabela mostra o setor.
- A tela de ranking tem **abas** de modo e de dificuldade. Ela funciona com o
  ranking global (jogador com conta) e com o local (convidado).
- O **histórico** mostra o modo de cada partida, por exemplo "INFINITO S4".

## 4. Vidas com corações

O HUD trocou as mini-naves por **corações**: cheios para as vidas que restam e
vazios para as perdidas, até 5 (o máximo do Infinito). Os corações são
desenhados por código, sem imagem de terceiros. A troca também corrigiu um erro:
o HUD antigo só tinha espaço para 3 vidas.

## 5. Controles na tela do celular

- **Direcional em cruz** no canto esquerdo. Segurar a seta mantém a nave andando.
- Botão **EMBARCAR** grande no canto direito.
- Os dois ficam **por cima do mapa e translúcidos**: o mapa continua do tamanho
  da tela e dá para ver o jogo através deles.
- No **PC** nada mudou: teclado (WASD, setas, C, P, Q) e os botões de texto no
  rodapé.

## 6. Versão para apresentação

- **Windows (`MissaoMarte-windows.zip`):** traz o jogo e um Java 17 embutido.
  Basta extrair e dar dois cliques em **`Jogar Missao Marte.bat`**, sem instalar
  nada. Ele usa um `.bat` porque o `.exe` gerado pelo `jpackage` era bloqueado
  pelo Smart App Control do Windows 11.
- **Android (`MissaoMarte-android.apk`):** já aponta para o servidor na nuvem.
- O passo a passo está em [`APRESENTACAO.md`](APRESENTACAO.md).

## 7. Como foi feito (arquitetura)

- **Strategy:** a tela fala com um contrato, `RegrasDePartida`, que tem três
  implementações: Clássico, Tempo real e Infinito.
- **Adapter:** `RegrasClassicas` reaproveita o `JogoService` das fases
  anteriores sem mudar nenhuma regra dele.
- **O tempo entra por parâmetro nas regras**, que nunca leem o relógio do
  sistema. Por isso a partida em tempo real é testada com tempo simulado, sem
  janela.
- **A API continua compatível com os clientes antigos:**
  - os campos novos (`modo`, `setor`) são opcionais;
  - a rota antiga do ranking responde igual;
  - as partidas antigas foram marcadas como Clássico.
- As regras (`model` e `service`) continuam sem LibGDX.

## 8. Números

| Item | Resultado |
|---|---|
| Testes do servidor | **41** (27 da Fase 6 + 14 novos) |
| Testes do cliente | **132** (83 da Fase 6 + 49 novos) |
| Verificações com janela real | 4: clássico, online (83 passos), tempo real e Infinito |
| Celular | Testado no Galaxy S20 FE, contra o servidor na nuvem |
| Entregas | 6 pull requests (#18 a #23) e uma Release |

## 9. Limites conhecidos

- O banco gratuito do Render expira por volta de **27/12/2026**.
- O servidor dorme após 15 min: abra o jogo uns 2 minutos antes de apresentar.
- A pontuação é informada pelo próprio jogo, e o servidor não a confere.
- Não foram testados: dois dedos ao mesmo tempo no celular (seta + Embarcar) e
  o pacote num outro computador.

Detalhes técnicos: [tutorial da Fase 7](docs/fase-07-tutorial.md) ·
[README da Fase 7](README.md) · [plano aprovado](../../docs/plano-fase-07.md).
