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
- [Como executar localmente](#como-executar-localmente)
- [Docker](#docker)
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

> No profile `dev`, `SPRING_DATASOURCE_URL` é opcional (assume
> `localhost:5432`) e o usuário/senha vêm de `POSTGRES_USER`/`POSTGRES_PASSWORD`.
> No profile `prod` não há valores padrão: a aplicação falha rápido se faltar algo.

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
  privilégios.
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

## Documentação da API (Swagger)

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
