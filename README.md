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

O backend segue **Clean Architecture** (RNF/PROC-03), com a regra de dependência
sempre apontando para o centro — as camadas internas não conhecem as externas:

```
interfaces ──> application ──> domain <── infrastructure
                                  ▲
                              (núcleo)
```

| Camada           | Responsabilidade                                                        | Pode depender de            |
|------------------|------------------------------------------------------------------------|-----------------------------|
| `domain`         | Regras de negócio, entidades, ports (interfaces de repositório)        | (nada)                      |
| `application`    | Casos de uso; orquestra o domínio                                       | `domain`                    |
| `infrastructure` | Implementações técnicas: JPA, integrações externas                     | `domain`, `application`     |
| `interfaces`     | Adaptadores de entrada: controladores REST, DTOs, tratamento de erros  | `application`               |
| `config`         | Composição da aplicação: beans, OpenAPI, segurança                     | (todas, como composition root) |

Cada camada possui um `package-info.java` documentando seu papel e suas regras de
dependência.

## Estrutura de pastas

```
projeto-es-sistema-imobiliario/
├── .github/workflows/ci.yml         # Pipeline de CI (build + testes)
├── docker/initdb/                   # Scripts de inicialização do banco (PostGIS)
├── src/
│   ├── main/
│   │   ├── java/com/campusliving/
│   │   │   ├── CampusLivingApplication.java
│   │   │   ├── domain/              # Núcleo: regras de negócio
│   │   │   ├── application/         # Casos de uso
│   │   │   ├── infrastructure/      # Detalhes técnicos (JPA, integrações)
│   │   │   ├── interfaces/          # Adaptadores REST (controllers, DTOs)
│   │   │   └── config/              # Configurações (OpenAPI, etc.)
│   │   └── resources/
│   │       ├── application.yml      # Config comum (sem segredos)
│   │       ├── application-dev.yml
│   │       ├── application-test.yml
│   │       └── application-prod.yml
│   └── test/java/com/campusliving/  # Testes (Testcontainers + PostGIS)
├── .env.example                     # Modelo de variáveis de ambiente
├── docker-compose.yml               # Banco + aplicação
├── Dockerfile                       # Build multi-stage da aplicação
└── pom.xml
```

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
arquivo. Operações de escrita exigem o `X-User-Id` do locador dono do anúncio,
seguindo o mecanismo provisório usado nos demais módulos do T5.

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
