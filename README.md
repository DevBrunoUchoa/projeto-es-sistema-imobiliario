<div align="center">

# 🏠 CampusLiving

**Plataforma web de locação de imóveis para estudantes da UFCG** (Campina Grande — PB)

[![CI](https://github.com/DevBrunoUchoa/projeto-es-sistema-imobiliario/actions/workflows/ci.yml/badge.svg)](https://github.com/DevBrunoUchoa/projeto-es-sistema-imobiliario/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16%20%2B%20PostGIS-4169E1?logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)

</div>

---

## O problema

Estudantes que buscam moradia perto do campus da UFCG hoje dependem de grupos de
WhatsApp e classificados informais — sem filtro por distância real do campus,
sem forma de avaliar locadores, e sem um jeito seguro de encontrar colega de
quarto compatível. O **CampusLiving** centraliza esse processo: busca de imóveis
com distância calculada até a UFCG, avaliações de locadores, e um sistema de
matching para roommates.

> Projeto desenvolvido para a disciplina de Engenharia de Software da UFCG
> (Equipe 04). O código e as decisões de arquitetura abaixo são o resultado
> real da implementação, não apenas um planejamento acadêmico.

## Principais funcionalidades

- **Busca georreferenciada** — filtro por distância real até a UFCG usando PostGIS, além de preço, mobília e regras de convivência.
- **Autenticação completa** — cadastro, login com JWT + refresh token, login social via Google (OAuth2), verificação de e-mail e recuperação de senha.
- **Avaliações e reputação** — nota e comentário para locadores, com resposta e nota média recalculada automaticamente por trigger no banco.
- **Match de roommates** — perfil de convivência e busca de colegas de quarto compatíveis.
- **Moderação** — denúncias com ocultação automática, painel administrativo e log de auditoria imutável.
- **Upload de imagens** — até 10 fotos por anúncio, armazenadas no Supabase Storage.

## Arquitetura

```
                        ┌───────────────────┐
                        │   Frontend (React)│
                        └─────────┬─────────┘
                                  │ HTTPS / JSON (cookies HttpOnly)
                        ┌─────────▼─────────┐
                        │  API REST (Spring)│
                        │ Controller→Service│
                        │    →Repository    │
                        └─────────┬─────────┘
                                  │ JPA
                  ┌───────────────┼──────────────────┐
         ┌────────▼────────┐            ┌────────────▼───────────┐
         │ PostgreSQL+PostGIS│           │  Supabase Storage       │
         │ (Flyway migrations)│          │  (imagens dos anúncios) │
         └────────────────────┘          └─────────────────────────┘
```

O backend segue o padrão **Controller → Service → Repository**, com JWT para
autenticação, Flyway para versionar o schema (25 migrations) e PostGIS para as
consultas geoespaciais. Detalhes completos de cada camada estão no
[README do backend](backend/README.md).

## Stack

| Camada | Tecnologias |
|---|---|
| Backend | Java 21, Spring Boot 3.4, Spring Data JPA, Spring Security (JWT + OAuth2) |
| Banco de dados | PostgreSQL 16 + PostGIS 3.4, Flyway |
| Frontend | React (Vite) |
| Infraestrutura | Docker, Docker Compose |
| Testes | JUnit 5, Spring Boot Test, Testcontainers |
| CI/CD | GitHub Actions (build + testes a cada PR) |
| Documentação | SpringDoc OpenAPI / Swagger UI |

## Como executar

Pré-requisito: Docker e Docker Compose instalados.

```bash
git clone https://github.com/DevBrunoUchoa/projeto-es-sistema-imobiliario.git
cd projeto-es-sistema-imobiliario/backend
cp .env.example .env        # ajuste os valores conforme necessário
docker compose up --build   # sobe banco + API em http://localhost:8080
```

Com a API no ar, a documentação interativa fica em `http://localhost:8080/swagger-ui`.
Para o frontend:

```bash
cd frontend
npm install
npm run dev                 # http://localhost:5173
```

Instruções completas de variáveis de ambiente, deploy e comandos Maven estão no
[README do backend](backend/README.md); notas do frontend estão em
[frontend/README.md](frontend/README.md).

## Documentação do projeto

Este repositório inclui a documentação acadêmica completa da disciplina em
[`docs/entrega3/`](docs/entrega3/), incluindo diagrama de classes, especificação
formal em Alloy, relatório de cobertura de testes (JaCoCo) e análise estática
(Checkstyle) — material que evidencia o processo de engenharia por trás do
código, não só o resultado final.

## Status

Projeto em desenvolvimento ativo. Deploy de referência: banco no Supabase,
API no Render, frontend no Vercel (ver detalhes e limitações de free tier no
[README do backend](backend/README.md#deploy-produção)).
