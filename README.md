# Campus Living — Backend

Plataforma web de locação de imóveis para estudantes da UFCG (Campina Grande - PB).
Este repositório contém o **backend** (API REST) do sistema.

> Projeto acadêmico da disciplina de Engenharia de Software — Equipe 04.

---

## Sumário

- [Descrição](#descrição)
- [Tecnologias](#tecnologias)
- [Arquitetura](#arquitetura)
- [Estrutura de pastas](#estrutura-de-pastas)
- [Autenticação e autorização](#autenticação-e-autorização)
- [Principais funcionalidades](#principais-funcionalidades)
- [Rotas principais](#rotas-principais)
- [Pré-requisitos](#pré-requisitos)
- [Variáveis de ambiente](#variáveis-de-ambiente)
- [Banco de dados](#banco-de-dados)
- [Como executar localmente](#como-executar-localmente)
- [Docker](#docker)
- [Deploy (produção)](#deploy-produção)
- [Documentação da API (Swagger)](#documentação-da-api-swagger)
- [Comandos Maven](#comandos-maven)
- [Integração contínua (CI)](#integração-contínua-ci)

---

## Descrição

O Campus Living centraliza o ecossistema de moradia universitária, permitindo que
estudantes encontrem imóveis e vagas próximos ao campus, com filtros por preço,
distância, mobília e regras de convivência, além de funcionalidades de avaliação e
busca por *roommates*. O backend expõe uma API REST que será consumida pelo frontend
web.

Buscas geográficas (ex.: distância do imóvel até a UFCG) são suportadas pela extensão
**PostGIS** do PostgreSQL.

## Tecnologias

| Categoria        | Tecnologia                                  |
|------------------|---------------------------------------------|
| Linguagem        | Java 21                                      |
| Framework        | Spring Boot 3.4                               |
| Build            | Maven                                         |
| Banco de dados   | PostgreSQL 16 + PostGIS 3.4                   |
| Persistência     | Spring Data JPA                              |
| Validação        | Spring Validation (Bean Validation)         |
| Documentação API | SpringDoc OpenAPI (Swagger UI)              |
| Observabilidade  | Spring Boot Actuator                         |
| Testes           | JUnit 5, Spring Boot Test, Testcontainers   |
| Container        | Docker e Docker Compose                      |
| CI/CD            | GitHub Actions                               |

> Conforme **RNF/PROC-02**, são utilizadas exclusivamente tecnologias gratuitas e
> open-source.

## Arquitetura

O backend é organizado em **camadas** (RNF/PROC-03 permite Clean Architecture
*ou* camadas), com responsabilidade única por pacote e o fluxo de dependência
indo sempre de fora para dentro (`controller → service → repository → banco`):

| Camada        | Pacote                 | Responsabilidade                                              |
|---------------|------------------------|--------------------------------------------------------------|
| Apresentação  | `controller`           | Endpoints REST; recebem/validam requisições e delegam        |
| Aplicação     | `service`              | Regras de negócio e orquestração dos casos de uso            |
| Persistência  | `repository`           | Spring Data JPA (acesso ao PostgreSQL)                       |
| Domínio       | `model`                | Entidades `@Entity` mapeadas às tabelas                      |
| Transporte    | `dto`                  | Objetos de requisição/resposta (não expõem entidades)       |
| Segurança     | `config.security` / `utilities.security` | Filtro JWT, `SecurityConfig`, `UserDetailsService` |
| Configuração  | `config`               | OpenAPI, ModelMapper e demais beans                          |
| Transversais  | `exception`            | Exceções de negócio e tratamento global de erros            |

> Os pacotes `domain/`, `application/`, `infrastructure/` e `interfaces/` foram
> criados no T5.1 pensando em Clean Architecture, mas a implementação seguiu o
> modelo em camadas acima; eles permanecem como placeholders.

## Estrutura de pastas

```
projeto-es-sistema-imobiliario/
├── .github/workflows/              # Pipeline de CI (build + testes)
├── docker/initdb/                  # Inicialização do banco (PostGIS)
├── src/
│   ├── main/
│   │   ├── java/com/campusliving/
│   │   │   ├── CampusLivingApplication.java
│   │   │   ├── controller/         # Endpoints REST (auth, imovel, avaliacao, ...)
│   │   │   ├── service/            # Regras de negócio por domínio
│   │   │   ├── repository/         # Spring Data JPA
│   │   │   ├── model/              # Entidades JPA
│   │   │   ├── dto/                # Requests/responses
│   │   │   ├── config/            # Segurança (JWT/OAuth2), OpenAPI, beans
│   │   │   ├── exception/          # Exceções e handlers
│   │   │   └── utilities/security/ # SecurityConfig
│   │   └── resources/
│   │       ├── application.yml      # Config comum (sem segredos)
│   │       ├── application-{dev,test,prod}.yml
│   │       ├── db/migration/        # Migrations Flyway (V1..V22)
│   │       └── db/seed/             # Seed de dados (somente dev)
│   └── test/java/com/campusliving/  # Testes (Testcontainers + PostGIS)
├── .env.example                     # Modelo de variáveis de ambiente
├── docker-compose.yml               # Banco + aplicação
├── Dockerfile                       # Build multi-stage da aplicação
└── pom.xml
```

## Autenticação e autorização

- **Cadastro:** `POST /auth/cadastro` (público) — cria usuário com `tipo_conta`
  `ESTUDANTE` ou `LOCADOR`; senha com hash **bcrypt (custo 12)** (RNF/SEG-01).
- **Login:** `POST /auth/login` — valida credenciais e emite **JWT** (validade
  24h) + **refresh token** (7 dias), RNF/SEG-02. Os tokens são devolvidos em
  cookies `HttpOnly` (`jwt` e `refresh_token`); o header
  `Authorization: Bearer <token>` também é aceito como alternativa. A flag
  `Secure` dos cookies é controlada por `APP_COOKIE_SECURE` (true em produção).
- **Renovação de token:** `POST /auth/refresh` troca o `refresh_token` (cookie)
  por um novo par de tokens, sem exigir novo login (RNF/SEG-02).
- **Login social:** Google via OAuth2 em `/oauth2/authorization/google`.
- **Verificação de e-mail / recuperação de senha:**
  `GET /auth/verificar-email/{token}`, `POST /auth/forgot-password`,
  `POST /auth/reset-password`. Os links vão por **e-mail** (SMTP configurável
  via `SPRING_MAIL_*` + `MAIL_ENABLED`); sem SMTP, o link é apenas logado. O
  token **nunca** é devolvido no corpo da resposta.
- **Autorização (RBAC, RNF/SEG-04):** papéis `ESTUDANTE`, `LOCADOR`, `MISTO`,
  `ADMIN`. Rotas `/admin/**` exigem `ADMIN`; criação/edição de imóveis e
  anúncios exige `LOCADOR` ou `ADMIN`.
- **Postura padrão:** todas as rotas exigem autenticação, exceto o allowlist
  público: `/auth/**`, `/oauth2/**`, `/login/**`, `/swagger-ui/**`,
  `/api-docs/**`, `/actuator/health` e as **leituras de catálogo** para o
  Visitante (`GET /anuncios`, `GET /anuncios/{id}`, `GET /anuncios/mapa`,
  `GET /anuncios/{id}/imagens`, `GET /usuarios/{id}/publico`,
  `GET /avaliacoes/**`). O contato do locador continua **mascarado** para não
  autenticados no perfil público (RNF/LEG-03); as escritas seguem protegidas.

## Principais funcionalidades

- **Usuários & perfis:** cadastro, edição de perfil, perfil público, verificação
  de identidade de locador (upload de documento) e selo de verificado, promoção
  para conta mista (RF-01, RF-06 a RF-09, RF-18).
- **Imóveis & anúncios:** cadastro de imóvel (CEP de Campina Grande), publicação,
  edição, inativação lógica (soft delete), upload de até 10 imagens no Supabase
  Storage (RF-11 a RF-15, RF-19).
- **Busca:** busca textual (GIN `tsvector`), filtros avançados (preço, distância,
  mobília, pets, fumantes, alimentação), ordenação, paginação e dados para mapa
  (RF-21 a RF-25).
- **Interação:** registro de interesse/mensagem ao locador, favoritos (RF-26 a
  RF-28).
- **Avaliações:** nota (1–5) e comentário, resposta do locador, cálculo de nota
  média (RF-29 a RF-31).
- **Roommates:** perfil de convivência, listagem de compatíveis, solicitação e
  resposta de match (RF-32 a RF-35).
- **Moderação/Admin:** denúncia com ocultação automática a partir de 5 denúncias,
  painel administrativo, moderação de verificações, logs de auditoria (RF-36,
  RF-37, RF-40, RF-41).

## Rotas principais

Prefixo base: `http://localhost:8080`. Documentação interativa em `/swagger-ui`.

| Método | Rota | Acesso | Descrição |
|--------|------|--------|-----------|
| POST | `/auth/cadastro` | Público | Cadastro de usuário |
| POST | `/auth/login` | Público | Login (retorna JWT em cookie) |
| POST | `/auth/refresh` | Público (cookie) | Renova o par de tokens via `refresh_token` |
| POST | `/auth/forgot-password` · `/auth/reset-password` | Público | Recuperação de senha |
| GET | `/auth/verificar-email/{token}` | Público | Verificação de e-mail |
| GET | `/anuncios` | Público | Busca/filtragem/paginação de anúncios |
| GET | `/anuncios/{id}` · `/anuncios/mapa` | Público | Detalhe / dados de mapa |
| POST · PUT | `/anuncios` · `/anuncios/{id}` | `LOCADOR`/`ADMIN` | Publicar / editar anúncio |
| PATCH | `/anuncios/{id}/status` | `LOCADOR`/`ADMIN` | Inativação lógica (soft delete) |
| POST·GET·DELETE | `/anuncios/{adId}/imagens` | Autenticado (dono) | Upload/listagem/remoção de fotos |
| POST | `/imoveis` | `LOCADOR`/`ADMIN` | Cadastro de imóvel |
| GET·PUT·DELETE | `/usuarios/{id}` | Autenticado (dono/ADMIN) | Perfil, edição, exclusão (LGPD) |
| GET | `/usuarios/{id}/publico` | Público (contato mascarado) | Perfil público |
| POST | `/usuarios/{id}/foto` | Autenticado (dono/ADMIN) | Upload da foto de perfil (RF-20) |
| POST·GET·DELETE | `/usuarios/{id}/favoritos` | Autenticado (dono) | Favoritos |
| POST | `/interesses` | Autenticado | Registrar interesse/mensagem |
| POST·GET | `/avaliacoes` · `/avaliacoes/anuncio/{id}` | Autenticado | Avaliar / listar avaliações |
| POST·GET·PATCH | `/roommates/perfil` · `/roommates/compativeis` · `/roommates/match` | Autenticado | Perfil, compatíveis e matches |
| GET·PATCH | `/notificacoes` · `/notificacoes/nao-lidas` · `/notificacoes/{id}/lida` | Autenticado | Notificações in-app (polling, RF-39) |
| POST·GET | `/denuncias` | Autenticado | Denunciar / contar denúncias |
| GET·PATCH | `/admin/**` | `ADMIN` | Painel, moderação, relatórios, auditoria |
| PATCH | `/admin/denuncias/{id}/moderar` | `ADMIN` | Banir anúncio / arquivar denúncia (RF-42) |
| GET | `/admin/relatorios` · `/admin/relatorios/csv?dias=` | `ADMIN` | Métricas por período + exportação CSV (RF-43) |


## Pré-requisitos

Escolha **uma** das opções:

- **Via Docker (recomendado):** Docker + Docker Compose. Não exige Java/Maven local.
- **Via Maven local:** JDK 21 e Maven 3.9+. Para os testes, é necessário um Docker em
  execução (usado pelo Testcontainers).

## Variáveis de ambiente

As credenciais são carregadas **exclusivamente** por variáveis de ambiente, via
arquivo `.env` (não versionado). Nenhuma credencial fica fixa no código.

```bash
cp .env.example .env   # depois edite os valores
```

| Variável                     | Obrigatória            | Descrição                                              |
|------------------------------|------------------------|--------------------------------------------------------|
| `POSTGRES_DB`                | sim                    | Nome do banco                                           |
| `POSTGRES_USER`              | sim                    | Usuário do banco                                        |
| `POSTGRES_PASSWORD`          | sim                    | Senha do banco                                          |
| `POSTGRES_PORT`              | não (padrão `5432`)    | Porta exposta do PostgreSQL                             |
| `SPRING_PROFILES_ACTIVE`     | não (padrão `dev`)     | Profile ativo: `dev`, `test` ou `prod`                 |
| `APP_PORT`                   | não (padrão `8080`)    | Porta HTTP da aplicação                                |
| `SPRING_DATASOURCE_URL`      | sim em `prod`          | URL JDBC do banco                                      |
| `SPRING_DATASOURCE_USERNAME` | sim em `prod`          | Usuário do banco (profile `prod`)                      |
| `SPRING_DATASOURCE_PASSWORD` | sim em `prod`          | Senha do banco (profile `prod`)                        |
| `JWT_SECRET`                 | sim em `prod`          | Chave de assinatura JWT (≥ 32 bytes). Sem padrão em `prod` |
| `APP_COOKIE_SECURE`          | não (padrão `false`/`true` em prod) | Envia cookies de sessão só por HTTPS      |
| `MAIL_ENABLED`               | não (padrão `false`)   | Liga o envio real de e-mail (senão, loga o link)        |
| `MAIL_FROM`                  | não                    | Remetente dos e-mails                                   |
| `APP_FRONTEND_URL`           | não (padrão localhost) | URL base dos links nos e-mails (verificação/reset)      |
| `SPRING_MAIL_HOST` / `_PORT` / `_USERNAME` / `_PASSWORD` | sim se `MAIL_ENABLED=true` | Credenciais SMTP (ex.: Brevo) |
| `GEOCODING_ENABLED` / `NOMINATIM_URL` / `UFCG_LAT` / `UFCG_LON` | não | Geocodificação (Nominatim) e coordenadas da UFCG (RF-16) |
| `DB_POOL_MAX_SIZE`           | não (padrão `10`)      | Tamanho máximo do pool HikariCP (só `prod`)             |
| `DB_POOL_MIN_IDLE`           | não (padrão `2`)       | Conexões ociosas mínimas do pool (só `prod`)            |
| `JAVA_OPTS`                  | não                    | Flags extras de JVM ao rodar via Docker (ver Dockerfile)|

> No profile `dev`, `SPRING_DATASOURCE_URL` é opcional (assume
> `localhost:5432`) e o usuário/senha vêm de `POSTGRES_USER`/`POSTGRES_PASSWORD`.
> No profile `prod` não há valores padrão: a aplicação falha rápido se faltar algo.

## Banco de dados

PostgreSQL 16 + PostGIS 3.4. O schema é gerenciado por **migrations Flyway**
(`src/main/resources/db/migration/`) — o Hibernate nunca cria/altera tabelas
sozinho (`ddl-auto: validate` em todos os profiles). Detalhes de convenções e
o que cada migration faz estão em [`src/main/resources/db/README.md`](src/main/resources/db/README.md).

Pontos-chave:

- **PostGIS** é habilitado via migration (`V1__enable_extensions.sql`), não só
  pelo script de init do Docker — assim funciona igual em qualquer provedor
  gerenciado (Supabase, RDS, etc.), não só localmente.
- Distância até a UFCG usa a coluna geoespacial `properties.geom`, mantida
  automaticamente em sincronia com latitude/longitude por um trigger.
- Busca textual de anúncios usa uma coluna `tsvector` gerada pelo próprio
  Postgres (`ads.search_vector`), indexada com GIN.
- Regras de negócio que o banco consegue garantir sozinho — nada de anúncio
  duplicado ativo por imóvel, nada de avaliação duplicada, `audit_logs`
  append-only — viram `CONSTRAINT`/índice único/trigger, não só validação em
  Java.
- Em **dev**, um seed (`db/seed/V900__seed_dev.sql`) popula ~20 imóveis
  fictícios em bairros reais de Campina Grande-PB. Nunca roda em `test`/`prod`.
- Em **prod**, a conexão usa HikariCP com `sslmode=require` obrigatório
  (RNF/SEG-03) e um pool dimensionado para caber no free tier do provedor
  (`DB_POOL_MAX_SIZE`/`DB_POOL_MIN_IDLE`, ver `.env.example`).

## Como executar localmente

### Opção A — Banco no Docker + aplicação via Maven (fluxo de desenvolvimento)

```bash
cp .env.example .env
docker compose up -d db          # sobe apenas o PostgreSQL/PostGIS
mvn spring-boot:run              # sobe a aplicação no profile dev
```

A API ficará em `http://localhost:8080`.

### Opção B — Tudo no Docker

```bash
cp .env.example .env
docker compose up --build        # banco + aplicação
```

## Docker

- **`Dockerfile`** — build *multi-stage*: o primeiro estágio compila com Maven/JDK 21
  e o segundo gera uma imagem enxuta apenas com a JRE 21, executando como usuário sem
  privilégios. Inclui `HEALTHCHECK` (consulta `/actuator/health`) e flags de
  JVM adequadas para container (`JAVA_OPTS`, sobrescrevível em runtime).
- **`docker-compose.yml`** — orquestra:
  - `db`: PostgreSQL 16 + PostGIS 3.4, com *healthcheck* e volume persistente. A
    extensão PostGIS é habilitada automaticamente na inicialização via
    `docker/initdb/01-enable-postgis.sql`.
  - `app`: a aplicação, que só inicia após o banco estar saudável
    (`depends_on: condition: service_healthy`).

Comandos úteis:

```bash
docker compose up -d db          # somente o banco
docker compose up --build        # banco + aplicação
docker compose logs -f app       # acompanhar logs da aplicação
docker compose down              # parar (mantém os dados)
docker compose down -v           # parar e apagar o volume do banco
```

## Deploy (produção)

Arquitetura pensada para caber inteiramente em **free tiers** (RNF/ECO-01),
com um caminho claro de upgrade se o tráfego real justificar:

| Componente | Provedor sugerido | Por quê |
|---|---|---|
| Banco de dados | **Supabase** (Postgres + PostGIS) | Já é a peça central da modelagem geoespacial; PostGIS é habilitado com 1 clique; tier grátis não expira (diferente do Postgres free do Render, que expira em 30 dias). |
| API (este repositório) | **Render** (Web Service, plano free) | Deploy direto do `Dockerfile`, HTTPS automático, sem cartão de crédito. |
| Frontend | **Vercel** | Fora do escopo deste repositório. |

### Conectando o Render ao Supabase

No painel do Supabase: **Project Settings → Database → Connection string →
Session pooler**. Copie host, porta (`5432`) e usuário (formato
`postgres.<project-ref>`) para as variáveis de ambiente do serviço no Render
(`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`,
`SPRING_DATASOURCE_PASSWORD`, `SPRING_PROFILES_ACTIVE=prod`) — nunca num
arquivo commitado. O porquê de usar especificamente o *session pooler* (e não
a conexão direta ou o *transaction pooler*) está detalhado em
[`.env.example`](.env.example).

### O que o free tier realmente aguenta (seja honesto com a banca sobre isso)

"Gratuito" e "suporta grande demanda" puxam em direções opostas — vale deixar
isso claro no relatório da entrega, não só no código:

- **Supabase free**: banco compartilhado com 500 MB de RAM e 500 MB de
  armazenamento, até 60 conexões diretas ou 200 conexões via pooler
  (compartilhadas por todo o projeto, não só por esta API). Para uma turma
  testando o sistema isso sobra; para tráfego público real, não.
- **Render free**: a instância "dorme" depois de 15 minutos sem requisições,
  e o primeiro acesso seguinte leva de 30 a 90 segundos pra responder (*cold
  start*). Um serviço externo de ping (ex.: UptimeRobot, a cada ~10 min) evita
  o sono, mas é um contorno, não uma solução real de disponibilidade.
- **Consequência prática**: o índice GIN de busca textual, o GiST de
  distância geoespacial e o pool HikariCP dimensionado (T5.2) são o que
  garante que, dentro desses limites, a aplicação use os recursos disponíveis
  da forma mais eficiente possível — não existe configuração de banco que
  faça um plano de 500 MB "aguentar" carga de produção real. Se/quando isso
  for necessário, o upgrade natural é Supabase Pro (US$ 25/mês) + Render
  Starter (US$ 7/mês, sem cold start), sem precisar trocar nenhuma tecnologia.

## Documentação da API (Swagger)

### Upload de imagens (T5.9)

As imagens dos anúncios ficam no Supabase Storage; o PostgreSQL guarda os
metadados, a URL pública e o caminho interno necessário para exclusão. Crie um
bucket público chamado `anuncios` e configure `SUPABASE_URL`,
`SUPABASE_SERVICE_ROLE_KEY` e `SUPABASE_STORAGE_BUCKET`.

- `POST /anuncios/{adId}/imagens`: multipart no campo `imagens`.
- `GET /anuncios/{adId}/imagens`: lista as fotos na ordem de exibição.
- `PATCH /anuncios/{adId}/imagens/{imageId}/principal`: define a capa.
- `DELETE /anuncios/{adId}/imagens/{imageId}`: remove a foto.

Cada anúncio aceita até 10 imagens JPEG, PNG ou WEBP, com no máximo 5 MB por
arquivo. A listagem é pública; operações de escrita usam o usuário autenticado
pelo JWT e exigem que ele seja o locador dono do anúncio.

Com a aplicação em execução:

- **Swagger UI:** http://localhost:8080/swagger-ui
- **Documento OpenAPI (JSON):** http://localhost:8080/api-docs

## Comandos Maven

```bash
mvn clean                # limpa artefatos de build
mvn compile              # compila o código
mvn test                 # executa os testes (requer Docker para o Testcontainers)
mvn verify               # compila, empacota e testa (usado na CI)
mvn package              # gera o JAR em target/
mvn spring-boot:run      # executa a aplicação localmente
```

## Integração contínua (CI)

O workflow [`.github/workflows/ci.yml`](.github/workflows/ci.yml) executa
automaticamente **build e testes** a cada *Pull Request* para a branch `main`,
utilizando **Java 21** (RNF/PROC-05).
