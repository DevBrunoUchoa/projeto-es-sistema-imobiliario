# Cobertura de Testes — JaCoCo (Entrega 3)

**Suíte:** JUnit 5 + Testcontainers (PostGIS). **86 testes**, 0 falhas. **97 classes** analisadas.

A estratégia priorizou o **núcleo de negócio (camada `service`)**, onde estão as regras críticas (autenticação, autorização, avaliação, roommate, moderação).

## Visão geral

| Escopo | Instruções | Branches |
|---|---|---|
| **Projeto todo** | **41.2%** | **36.7%** |
| **Camada `service` (núcleo)** | **37.0%** | **39.9%** |

## Por pacote

| Pacote | Instruções | Branches |
|---|---|---|
| `service.imovel` | 20.8% | 20.8% |
| `service.usuario` | 60.0% | 59.3% |
| `service.roommate` | 91.6% | 67.4% |
| `service.auth` | 0.0% | 0.0% |
| `service.admin` | 26.0% | 37.5% |
| `service.avaliacao` | 97.7% | 93.3% |
| `config.security` | 15.3% | 22.2% |
| `service.denuncia` | 0.0% | 0.0% |
| `model.imovel` | 100.0% | 0% |
| `controller.imovel` | 18.0% | 0.0% |
| `utilities.security` | 100.0% | 0% |
| `controller.usuario` | 6.1% | 0.0% |
| `service.geocoding` | 26.3% | 0.0% |
| `service.interacao` | 60.2% | 50.0% |
| `model.usuario` | 51.7% | 0.0% |
| `controller.auth` | 0.0% | 0.0% |
| `exception` | 9.0% | 0.0% |
| `dto.usuario` | 100.0% | 100.0% |
| `service.email` | 9.3% | 0.0% |
| `dto.roommate` | 99.0% | 50.0% |
| `model.denuncia` | 100.0% | 0% |
| `service.notificacao` | 8.7% | 0.0% |
| `controller.admin` | 0.0% | 0.0% |
| `controller.roommate` | 9.8% | 0.0% |
| `controller.avaliacao` | 11.8% | 0.0% |
| `controller.notificacao` | 12.2% | 0.0% |
| `model.roommate` | 100.0% | 0% |
| `config` | 100.0% | 0% |
| `dto.notificacao` | 0.0% | 0.0% |
| `dto.interacao` | 100.0% | 0% |
| `dto.avaliacao` | 100.0% | 0% |
| `service.audit` | 0.0% | 0% |
| `model.notificacao` | 100.0% | 0% |
| `exception.usuario` | 64.5% | 0% |
| `controller.denuncia` | 0.0% | 0% |
| `dto.imovel` | 100.0% | 0% |
| `exception.roommate` | 100.0% | 0% |
| `exception.avaliacao` | 100.0% | 0% |
| `controller.interacao` | 27.3% | 0.0% |
| `model.interacao` | 100.0% | 0% |
| `exception.imovel` | 50.0% | 0% |
| `dto.admin` | 100.0% | 0% |
| `exception.interacao` | 100.0% | 0% |
| `com.campusliving` | 37.5% | 0% |
| `controller.audit` | 0.0% | 0% |

## Classes de serviço mais cobertas

| Classe | Instruções |
|---|---|
| `ImageStorageService.StoredImage` | 100.0% |
| `PalavraoFilter` | 100.0% |
| `AvaliacaoServiceImpl` | 95.9% |
| `RoommateServiceImpl` | 91.6% |
| `UserServiceImpl` | 69.1% |
| `ContatoServiceImpl` | 60.2% |
| `ImagemAnuncioService` | 58.6% |
| `NominatimGeocodingService` | 30.3% |
