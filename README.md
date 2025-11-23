# Lyra - Sistema de Apoio ao Bem-Estar Emocional

## 👤 Integrantes

| Turma | RM       | Nome Completo         |
|:-----:|:--------:|:---------------------:|
| 2TDSB | RM555679 | Lavinia Soo Hyun Park |
| 2TDSB | RM559123 | Caroline de Oliveira  |
| 2TDSB | RM554473 | Giulia Correa Camillo |

## Índice
1. [Descrição do Projeto](#descrição-do-projeto)
2. [Diagrama da Arquitetura](#diagrama-da-arquitetura)
3. [Como Executar Localmente](#como-executar-localmente)

## Descrição do Projeto
O **Lyra** é uma aplicação voltada para apoiar o bem-estar emocional de trabalhadores que enfrentam estresse e sobrecarga na rotina profissional.

Através de relatos enviados pelo usuário, o sistema identifica o nível de risco emocional utilizando **Inteligência Artificial (Google Gemini)** e gera recomendações personalizadas que podem ajudar no momento. Cada interação é registrada, permitindo acompanhar como o estado emocional evolui ao longo do tempo através de check-ins diários.

O objetivo do Lyra é oferecer um apoio **rápido, acessível e acolhedor** para quem precisa de suporte emocional, funcionando como uma primeira linha de cuidado e direcionamento para recursos profissionais quando necessário.

### 💻 Tecnologias Utilizadas

| Categoria              | Tecnologia        |
|:----------------------:|:-----------------:|
| Linguagem              | Java 17           |
| Framework Web          | Spring Boot       |
| Segurança              | Spring Security + JWT   |
| Persistência ORM       | Spring Data JPA   |
| Versionamento de Banco | Flyway            |
| Banco de Dados         | SQL Server (JDBC) |
| Gerenciador de Build   | Maven             |
| Redução de boilerplate | Lombok            |

### ⚙️ Funcionalidades

#### 🔐 Autenticação e Autorização
- **Cadastro de usuários** (`POST /api/auth/signup`)
- **Login com JWT** (`POST /api/auth/signin`)
- **Controle de acesso baseado em roles** (USER, ADMIN)
- **Tokens JWT com expiração configurável**

#### 😊 Análise de Humor com IA
- **Análise de humor utilizando Google Gemini AI** (`POST /api/humor/analisar`)
- **Classificação automática de risco emocional** (0 = Leve, 1 = Moderado, 2 = Grave, 3 = Gravíssimo)
- **Geração de resumo empático do relato**
- **Recomendações personalizadas baseadas no nível de risco**
- **Sistema de fallback** para casos de falha na IA

#### 📊 Check-in Diário
- **Registro de check-in diário** (`POST /api/checkin`)
    - Humor do dia
    - Descrição detalhada
    - Horas de sono
    - Hidratação (copos de água)
- **Consulta do último check-in** (`GET /api/checkin`)
- **Histórico de evolução emocional**

#### 👤 Gerenciamento de Usuários
- **Perfil do usuário autenticado** (`GET /api/users/me`)
- **Atualização de dados pessoais**
- **Gerenciamento de roles e permissões**

#### 🔗 Integrações Externas
- **Integração com sistema .NET** para processamento adicional
- **Envio de notificações via RabbitMQ**
- **Comunicação assíncrona entre microserviços**

## Diagrama da Arquitetura

**Abaixo é o fluxo de Java quando há uma modificação no repositório**

![Arquitetura do Projeto](/docs/images/diagrama-devops-gs.png)

### 🧱 Detalhamento dos Componentes
|   Nome do Componente    |                  Tipo                  |                                               Descriçao Funcional                                                |             Tecnologia / Ferramenta             |
|:-----------------------:|:--------------------------------------:|:----------------------------------------------------------------------------------------------------------------:|:-----------------------------------------------:|
|        Developer        |                Persona                 |                      Responsável por escrever, versionar e atualizar o código da aplicaçao                       |                        -                        |
|      Usuário Final      |                Persona                 |                                   Utiliza a aplicação através da interface web                                   |                        -                        |
|         GitHub          |         Repositório de Código          |                                  Armazena e versiona o código-fonte do projeto                                   |                     GitHub                      |
|  Azure DevOps Pipeline  | CI/CD (Orquestrador de Build e Deploy) |                  Automatiza o processo de build, geração da imagem Docker e deploy da aplicação                  |          Azure DevOps (Pipeline YAML)           |
| Spring Boot Application |           Aplicação Back-End           | Responsável pela estrutura e funcionamento da aplicação, com conexão ao Banco de Dados e Thymeleaf para o visual | Spring Boot + Spring Security + JPA + Thymeleaf |
|         Docker          |            Containerização             |                                        Empacota a aplicação em uma imagem                                        |                     Docker                      |
|           ACR           |          Registro de Imagens           |                           Armazena e versiona as imagens Docker geradas pela pipeline                            |            Azure Container Registry             |
|      Azure Web App      |     Ambiente Principal de Execução     |                 Hospeda e executa a aplicação continuamente, servindo o acesso ao usuário final                  |                  Azure Web App                  |
|           ACI           |          Ambiente Secundário           |               Permite rodar rapidamente a aplicaçao em container isolado para testes ou validações               |            Azure Container Instance             |
|   Azure SQL Database    |             Banco de Dados             |                                 Armazena informações persistidas pela aplicação                                  |                Azure SQL Server                 |
|      Front Mobile       |       Aplicação Mobile / Cliente       |      Envia dados como humor, relato, hidratação e sono para os backends (.NET / Java), utilizando API REST       |     Expo + React Native + Firebase AI Logic     |

### 🔄 Explicação do Fluxo

1. O desenvolvedor realiza um commit e envia as alterações para o repositório no GitHub.
2. Esse commit dispara automaticamente a pipeline de CI/CD no Azure DevOps.
3. A pipeline executa o build da aplicação Spring Boot, validando o código e dependências.
4. Após o build, é gerada a imagem Docker da aplicação.
5. A imagem é então enviada (push) para o Azure Container Registry (ACR).
6. A partir da imagem armazenada no ACR, é realizado o deploy principal no Azure Web App, onde a aplicação é executada.
7. Como opção de execução alternativa, a mesma imagem pode ser implantada no Azure Container Instances (ACI) para testes isolados.
8. A aplicação em execução (no Web App ou ACI) se conecta ao Azure SQL Database para armazenamento e consulta dos dados.

### Fluxo da Integração

1. Front-end envia dados como requisiçao POST para o webapp de Java
2. Java recebe, passa pela IA e decide se irá enviar ou nao pro Dotnet de acordo com a classificaçao de risco
3. Webapp de .NET recebe a requisiçao de Java, e retorna os dados (recomendaçao da IA)
4. Java recebe os dados de .NET e retorna como resposta para o Front

## Como Executar Localmente

A aplicação Java não será detalhada neste README, pois este documento é focado na parte DevOps.

As instruções completas de execução e estrutura do backend Java estão disponíveis no link abaixo:

https://github.com/ucarols/JavaLyra.git