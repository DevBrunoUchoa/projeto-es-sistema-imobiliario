# Cobertura de Testes — JaCoCo (Entrega 3)

**Medição atualizada (T5.10 integrado ao build):** JUnit 5 + Testcontainers
(PostGIS) + Mockito. **176 testes**, **0 falhas**, **0 erros**.

Os números foram gerados por `mvn clean test jacoco:report`. As 7 suítes do
T5.10 estavam commitadas em `src/test/` na raiz do repositório — fora do
diretório que o Maven compila (`backend/src/test/`) — e por isso nunca eram
executadas; foram movidas para dentro do build e atualizadas contra a API
atual dos serviços (comodidades extras e período de locação do imóvel). O
relatório HTML fica em `target/site/jacoco/index.html` e o CSV completo desta
medição está em [`jacoco.csv`](jacoco.csv).

A estratégia priorizou o **núcleo de negócio (camada `service`)**, onde estão as regras críticas (autenticação, autorização, avaliação, roommate, moderação).

## Suítes de teste

Testes unitários (JUnit 5 + Mockito) isolam persistência, auditoria, e-mail,
JWT e geocodificação; testes de integração (Testcontainers) validam o PostGIS
e o schema real.

| Serviço | Principais cenários cobertos |
|---|---|
| `AnuncioService` | publicação, distância/fallback, autorização, status, edição, detalhe, mapa, paginação, filtros, texto e período de locação |
| `ImovelService` | criação, autorização, coordenadas explícitas/geocodificadas, falha de geocodificação e comodidades |
| `AuthService` | cadastro/LGPD/role, login e refresh token válido, ausente, inválido ou revogado |
| `PasswordResetService` / `EmailVerificationService` | emissão, expiração, consumo de token e auditoria |
| `DenunciaService` | validação, duplicidade e ocultação automática na 5ª denúncia |
| `NotificacaoService` | listagem, contagem, leitura individual/em lote e isolamento por usuário |
| `RoommateServiceImpl` | preferências, ativação pública (RF-32), compatibilidade e match |
| Integração (PostGIS) | distância à UFCG, trigger de nota média, filtros sem texto e acesso protegido |

## Visão geral

| Escopo | Instruções | Branches |
|---|---|---|
| **Projeto todo** | **64.9%** | **53.7%** |
| **Camada `service` (núcleo)** | **71.6%** | **60.3%** |

## Por pacote (`service`)

| Pacote | Instruções | Branches |
|---|---|---|
| `service.denuncia` | 100.0% | 91.7% |
| `service.notificacao` | 100.0% | 100.0% |
| `service.avaliacao` | 93.9% | 87.5% |
| `service.auth` | 86.4% | 78.6% |
| `service.roommate` | 86.2% | 68.3% |
| `service.imovel` | 79.8% | 54.7% |
| `service.usuario` | 60.4% | 61.0% |
| `service.interacao` | 50.0% | 38.9% |
| `service.geocoding` | 31.3% | 7.1% |
| `service.admin` | 22.9% | 25.0% |
| `service.email` | 13.5% | 0.0% |
| `service.audit` | 0.0% | 0.0% |

## Classes de serviço mais cobertas

| Classe | Instruções |
|---|---|
| `ImovelService` | 100.0% |
| `DenunciaService` | 100.0% |
| `NotificacaoService` | 100.0% |
| `PalavraoFilter` | 100.0% |
| `AuthService` | 98.0% |
| `PasswordResetService` | 95.0% |
| `EmailVerificationService` | 93.5% |
| `AnuncioService` | 93.3% |
| `AvaliacaoServiceImpl` | 89.5% |
| `RoommateServiceImpl` | 86.2% |

## Débito técnico reconhecido

Coberturas baixas concentram-se em adaptadores de infraestrutura e integrações
externas, fora do núcleo de regras de negócio: `service.email` (SMTP),
`service.geocoding` (Nominatim/LocationIQ), `SupabaseImageStorageService`,
`LocalDiskDocumentStorageService` e `AdminService` (relatórios). São candidatos
naturais à ampliação de cobertura em iteração futura.
