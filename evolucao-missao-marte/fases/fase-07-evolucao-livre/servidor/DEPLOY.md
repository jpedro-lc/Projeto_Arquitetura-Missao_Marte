# Publicar o servidor no Render

O repositório já traz tudo o que o Render precisa:

| Arquivo | Papel |
|---|---|
| [`render.yaml`](../../../../render.yaml) (raiz do repositório) | *Blueprint*: cria o serviço web e o banco |
| [`Dockerfile`](Dockerfile) | Compila o servidor com o wrapper do projeto e roda o JAR com JRE 17, sem privilégios |
| `UrlDoBanco` | Converte o `DATABASE_URL` do Render (`postgresql://…`) para o formato JDBC |
| `GET /api/saude` | Verificação de saúde do Render; o jogo também chama para acordar o servidor |

O que o Blueprint cria (tudo no plano **gratuito**, região **Virginia**):

- **missao-marte-servidor**: serviço web Docker, a partir da pasta
  `evolucao-missao-marte/fases/fase-07-evolucao-livre/servidor` da branch `master`.
  Refaz o deploy sozinho quando essa pasta muda.
- **missao-marte-banco**: PostgreSQL, acessível só pela rede privada do Render.
- Variáveis: `DATABASE_URL` (vem do banco) e `MISSAOMARTE_JWT_SEGREDO` (gerada
  pelo Render, 256 bits). **Nenhum segredo fica no repositório.**

## Passo a passo

1. Entre em <https://dashboard.render.com> e crie a conta (pode usar o login
   do GitHub).
2. Clique em **New → Blueprint**.
3. Conecte o GitHub e dê acesso ao repositório
   `jpedro-lc/Projeto_Arquitetura-Missao_Marte` (pode ser só este repositório).
4. Escolha o repositório, branch `master`. O Render lê o `render.yaml` e mostra
   o serviço e o banco que serão criados. Dê um nome ao Blueprint (por exemplo,
   `missao-marte`) e clique em **Deploy Blueprint**.
5. Aguarde. O banco fica pronto em 1 ou 2 minutos; o primeiro build do servidor
   leva alguns minutos (baixa o Gradle e as dependências). Acompanhe em
   **missao-marte-servidor → Logs** até aparecer `Started ServidorApplication`.
6. Copie o endereço do serviço, no topo da página dele
   (algo como `https://missao-marte-servidor.onrender.com`).
7. Confira no navegador: `https://<endereço>/api/saude` deve mostrar
   `{"status":"ok"}`. A documentação interativa fica em
   `https://<endereço>/swagger-ui/index.html`.

## APK apontando para a nuvem

Na pasta `cliente`, com o endereço do passo 6:

```powershell
.\gradlew.bat :android:installDebug "-Pmissaomarte.servidor=https://missao-marte-servidor.onrender.com"
```

O celular passa a usar o servidor da nuvem por qualquer rede (Wi-Fi ou dados
móveis), sem cabo nem `adb reverse`. Para o desktop:

```powershell
.\gradlew.bat :lwjgl3:run "-Dmissaomarte.servidor=https://missao-marte-servidor.onrender.com"
```

## Limites do plano gratuito

| Limite | Efeito no jogo |
|---|---|
| O serviço **dorme após 15 min** sem uso e leva cerca de 1 min para acordar | O jogo chama `/api/saude` ao abrir e espera até 70 s; no login aparece "Conectando... pode levar até 1 min." |
| O **PostgreSQL gratuito expira 30 dias** após a criação (mais 14 dias para virar pago, senão é apagado) | Contas e rankings somem depois disso. Basta para a apresentação; depois, apague o Blueprint ou recrie o banco |
| O disco do serviço é efêmero | Por isso o banco é o PostgreSQL, e não o H2 em arquivo |
| 750 horas gratuitas por mês | Suficiente para um serviço |

## Segurança

- Só HTTPS na nuvem (o Render cuida do certificado). O HTTP liberado no APK de
  depuração continua servindo para `localhost` e rede local.
- Não há limite de tentativas de login nem monitoramento: é um ambiente
  acadêmico. Use nomes e senhas de teste e apague os serviços quando não
  precisar mais (**Blueprint → Settings → Delete**, depois o banco).

## Rodar a mesma imagem localmente (opcional)

Precisa do Docker Desktop, que não estava instalado no ambiente desta fase:

```powershell
docker build -t missao-marte-servidor .
docker run --rm -p 8080:8080 -e MISSAOMARTE_JWT_SEGREDO="um-texto-de-teste-com-32-ou-mais-caracteres" missao-marte-servidor
```

Sem `DATABASE_URL`, o contêiner usa o H2 em arquivo dentro dele (os dados somem
ao remover o contêiner).
