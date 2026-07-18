# Cobertura de Testes — JaCoCo (Entrega 3)

**Medição atualizada (T5.10):** JUnit 5 + Testcontainers (PostGIS). **163
testes**, **0 falhas**, **0 erros**. **97 classes** analisadas.

Os números foram gerados por `mvn clean test` em 15/07/2026. O relatório HTML
fica em `target/site/jacoco/index.html` e o CSV completo desta medição está em
[`jacoco.csv`](jacoco.csv).

A estratégia priorizou o **núcleo de negócio (camada `service`)**, onde estão as regras críticas (autenticação, autorização, avaliação, roommate, moderação).

## Atualização T5.10 — suítes adicionadas

Os novos testes são unitários, com JUnit 5 e Mockito, e isolam dependências de
persistência, auditoria, e-mail, JWT e geocodificação. As integrações já
existentes continuam responsáveis por validar PostGIS e o schema real.

| Serviço | Principais cenários cobertos |
|---|---|
| `AnuncioService` | publicação, distância/fallback, autorização, status, edição, detalhe, mapa, paginação, filtros e texto |
| `ImovelService` | criação, autorização, coordenadas explícitas/geocodificadas e falha de geocodificação |
| `AuthService` | cadastro/LGPD/role, login e refresh token válido, ausente, inválido ou revogado |
| `PasswordResetService` / `EmailVerificationService` | emissão, expiração, consumo de token e auditoria |
| `DenunciaService` | validação, duplicidade e ocultação automática na 5ª denúncia |
| `NotificacaoService` | listagem, contagem, leitura individual/em lote e isolamento por usuário |

## Visão geral

| Escopo | Instruções | Branches |
|---|---|---|
| **Projeto todo** | **67.0%** | **55.2%** |
| **Camada `service` (núcleo)** | **73.5%** | **61.2%** |

## Por pacote

| Pacote `service` | Instruções | Branches |
|---|---|---|
| `service.imovel` | 78.3% | 53.0% |
| `service.usuario` | 60.0% | 59.3% |
| `service.roommate` | 91.6% | 67.4% |
| `service.auth` | 87.2% | 80.0% |
| `service.admin` | 26.0% | 37.5% |
| `service.avaliacao` | 97.7% | 93.3% |
| `service.denuncia` | 100.0% | 91.7% |
| `service.notificacao` | 100.0% | 100.0% |
| `service.geocoding` | 32.8% | 0.0% |
| `service.interacao` | 60.2% | 50.0% |
| `service.email` | 9.3% | 0.0% |
| `service.audit` | 0.0% | 0% |

## Classes de serviço mais cobertas

| Classe | Instruções |
|---|---|
| `AuthService` | 100.0% |
| `DenunciaService` | 100.0% |
| `ImovelService` | 100.0% |
| `NotificacaoService` | 100.0% |
| `AvaliacaoServiceImpl` | 95.9% |
| `PasswordResetService` | 95.7% |
| `AnuncioService` | 95.2% |
| `EmailVerificationService` | 94.3% |
| `RoommateServiceImpl` | 91.6% |
