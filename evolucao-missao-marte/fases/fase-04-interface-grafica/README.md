# Missão Marte — Fase 4 (Interface gráfica)

Apresentação gráfica completa sobre as mesmas regras por turnos: sprites e
painéis SpaceRage, movimento visual entre células, animações por quadros,
feedback de colisão, HUD redesenhado, sons e fundo em tela cheia. Mantém os
módulos **core e lwjgl3**, Java 17, Gradle Wrapper 8.7 e LibGDX 1.12.1.

## Executar

Na pasta desta fase, com JDK 17:

```bash
export JAVA_HOME=~/.jdks/openjdk-17.0.2   # ajuste ao caminho do seu JDK 17
./gradlew build
./gradlew :lwjgl3:run
```

| Controle | Comportamento |
|---|---|
| WASD | Um movimento por pressão; a nave desliza até a nova célula |
| C | Embarcar o passageiro na posição da nave |
| Q | Abortar imediatamente |
| P / ESC | Pausar e abrir as opções |
| R | Reiniciar uma partida encerrada |

## O que mudou em relação à Fase 3

| Tema | Fase 4 |
|---|---|
| Movimentação | `AnimadorPosicoes` interpola nave e inimigos em 0,15 s (só visual) |
| Animações | Astronauta parado (4 quadros), resgate (4), explosão (11), propulsor (5); base com estados ociosa/ativa/pouso/concluída (`EstadoVisualBase`) |
| Colisões | Regra continua por célula no `JogoService`; a nave pisca e a área treme por 0,6 s |
| Sprites/texturas | Asteroides em 3 tamanhos, astronautas, base e painéis SpaceRage; regiões criadas uma vez; filtro `Nearest` |
| HUD | Painel superior contínuo + inferior com ícones de vidas e passageiros |
| Telas | Menu e pausa com `menu_panel`/`pause_panel` e botões normal/hover/pressed |
| Layout | `ExtendViewport`: fundo cobre a janela inteira, sem faixas pretas |
| Rodapé | Somente controles existentes (SPACE/tiro removido) |
| Sons | Inalterados: música, impactos, resgate, vitória, derrota, menu, erro |

Nenhum arquivo de `model`, `service` ou `repository` foi alterado.

## Validar e distribuir

```bash
./gradlew clean build
./gradlew :lwjgl3:smokeDesktop
./gradlew :lwjgl3:installDist
./gradlew :lwjgl3:runConsole --console=plain
```

Resultado em 17/11/2026 (Windows 11, OpenJDK 17.0.2):

- **48 casos JUnit** aprovados: os 44 da Fase 3 (incluindo as **162 verificações
  herdadas**) 3 novos em `AnimacaoVisualTest` (interpolação, corte de animação,
  estados da base sem alterar a vitória) e 1 em `PartidaRendererTest` (passageiro em coordenadas negativas).
- **Smoke gráfico** aprovado com janela real: menu, instruções, HUD, pausa,
  áudio, colisão, vitória, derrota, reinício, redimensionamento e descarte;
  12 capturas em `lwjgl3/build/reports/smoke/`, incluindo 1366×768, 1600×900 e
  1920×1080.
- **Distribuição** (`installDist`) copiada para fora do projeto e aprovada no mesmo smoke.
- **Console** abriu e encerrou pela opção 4.

## Documentação

- [Tutorial da Fase 4](docs/fase-04-tutorial.md)
- [Plano aprovado](../../docs/plano-fase-04.md) e [referencial](../../tutorial-api-gradle-libgdx.md)

Tiro, escudo, ranking global e Android permanecem fora desta entrega.
