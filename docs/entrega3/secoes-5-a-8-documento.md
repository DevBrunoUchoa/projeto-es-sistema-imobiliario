# Entrega 3 — Seções 5 a 8 (rascunho para o documento)

> Texto pronto para colar no documento oficial. Baseado no código real do
> backend (branch `main`). Substitua os `<LINKS>` pelos endereços finais.

---

## 5. Projeto Arquitetural

Como referência para o desenvolvimento, adotou-se um diagrama de classes (UML)
que descreve as entidades do domínio e suas relações
(ver [`modelagem-classes.png`](modelagem-classes.png)), revisado continuamente ao
longo da implementação.

### 5.1. Estrutura de Serviços

O backend adota o padrão **Controller–Service–Repository**, separando
responsabilidades em camadas bem definidas:

- **Serviço de Autenticação** (`AuthService`, `JwtService`, `OAuth2Service`): login,
  emissão/validação de JWT, *refresh token*, verificação de e-mail, recuperação de
  senha e login social (Google/OAuth2).
- **Serviço de Usuário** (`UserService`): ciclo de vida do usuário — cadastro,
  perfil, perfil público (com máscara de contato, RNF/LEG-03), verificação de
  identidade de locador, promoção a conta mista, favoritos e exclusão (LGPD).
- **Serviço de Imóveis** (`ImovelService`): CRUD de imóveis, com geocodificação
  automática do endereço (Nominatim) quando não há coordenadas.
- **Serviço de Anúncios** (`AnuncioService`): publicação, edição, inativação
  lógica (*soft delete*), busca textual (GIN), filtros avançados, ordenação,
  paginação e cálculo pré-computado da distância até a UFCG (PostGIS).
- **Serviço de Avaliações** (`AvaliacaoService`): avaliação (1–5) e resposta do
  locador; nota média recalculada por *trigger* no banco.
- **Serviço de Roommates** (`RoommateService`): perfil de convivência, listagem de
  compatíveis e solicitação/resposta de *match*.
- **Serviço de Interação** (`ContatoService`): registro de interesse/mensagem ao
  locador.
- **Serviço de Notificações** (`NotificacaoService`): notificações in-app
  (consulta e marcação de leitura — modelo de *polling*).
- **Serviço de Denúncias/Moderação** (`DenunciaService`, `AdminService`): denúncia,
  ocultação automática a partir de 5 denúncias, painel e relatórios administrativos.
- **Serviço de Imagens/Armazenamento** (`ImageStorageService`,
  `SupabaseImageStorageService`, `DocumentStorageService`): upload e persistência de
  imagens e documentos (Supabase Storage).
- **Serviço de E-mail** (`EmailService`): e-mails transacionais (verificação, reset,
  interesse), assíncronos.
- **Serviço de Auditoria** (`AuditLogService`): registro imutável de ações críticas
  (RNF/SEG-06).

### 5.2. Comunicação entre Serviços

Desenvolvido em **Java 21 + Spring Boot 3.4**. Os *Controllers* expõem uma API
REST, recebem e validam as requisições (Bean Validation) e delegam à camada de
*Service*, que concentra as regras de negócio e as permissões de acesso. A
segurança usa **JWT** (cookie `HttpOnly` + *fallback* `Bearer`); um filtro valida
o token antes de endpoints protegidos. Há **tratamento centralizado de exceções**.
O acesso a dados usa **Spring Data JPA** sobre **PostgreSQL**, e o schema é
versionado com **Flyway** (migrations `V1..V22`). Consultas geoespaciais usam a
extensão **PostGIS**.

### 5.3. Frontend e Interação com o Usuário

O frontend consome a API REST via HTTP/JSON, autenticando por token. *(Detalhar
tecnologia/telas do nosso front — a preencher pela equipe de frontend.)*

### 5.4. Infraestrutura e Implantação

Ambiente padronizado com **Docker/Docker Compose** (PostgreSQL+PostGIS). Implantação
prevista em **free tiers**: banco no **Supabase** (Postgres+PostGIS, `sslmode=require`),
API no **Render** (deploy do `Dockerfile`, HTTPS automático — o backend reconhece o
proxy TLS via `forward-headers-strategy`), armazenamento de imagens no **Supabase
Storage**. CI no **GitHub Actions** (build + testes a cada PR).

---

## 6. Especificação Formal

As regras de negócio foram transpostas para a linguagem **Alloy** (ver
[`campusliving.als`](campusliving.als)), permitindo verificar matematicamente as
invariantes com o *Alloy Analyzer*: unicidade de e-mail, integridade das relações
(anúncio pertence ao dono do imóvel; único anúncio ativo por imóvel), e proteções
de autorização (não avaliar a si mesmo, não conectar consigo mesmo, avaliador com
vínculo prévio). O mapeamento completo `fact → RF` está em
[`especificacao-formal.md`](especificacao-formal.md).

---

## 7. Implementação

Link para os repositórios:

- Backend: `<LINK_REPO_BACKEND>`
- Frontend: `<LINK_REPO_FRONT>`
- Demonstração (vídeos): `<LINK_DEMO>`

Foram implementados os requisitos funcionais **essenciais** e **importantes**, além
de vários **desejáveis**, com ênfase em segurança (RNF/SEG): bcrypt custo 12, JWT
24h + refresh 7d, RBAC (ESTUDANTE/LOCADOR/MISTO/ADMIN), máscara de contato (LGPD) e
logs de auditoria. O schema é gerenciado por Flyway e as buscas geoespaciais por
PostGIS.

---

## 8. Testes

### 8.1. Plano de Testes (TestLink)

O plano de testes foi organizado no **TestLink** (executado localmente via Docker),
com casos de teste derivados dos requisitos, cobrindo os pilares: **Autenticação e
Segurança**, **Gestão de Usuários/Perfil**, **Imóveis e Anúncios (busca/filtros/
distância)**, **Avaliações**, **Roommates** e **Moderação/Admin**. Os casos estão
em [`plano-testes-testlink.xml`](plano-testes-testlink.xml) (importável no TestLink).

### 8.2. Análise Estática

- **Checkstyle** (ruleset Sun): **2.235** ocorrências em **153** arquivos,
  majoritariamente Javadoc/`final`/formatação (não afetam a lógica) —
  ver [`analise-estatica-checkstyle.md`](analise-estatica-checkstyle.md).
- **SonarQube/SonarCloud**: *(a executar pela equipe — requer conta/token).*

### 8.3. Testes Automatizados e Cobertura

Após a T5.10, a suíte com **JUnit 5** e **Testcontainers** (PostGIS real)
possui **163 testes** automatizados, executados com **0 falhas** e **0 erros**.
A cobertura foi medida com **JaCoCo**, priorizando o núcleo de negócio (camada
`service`):

- Projeto: **67,0%** de instruções / **55,2%** de *branches*.
- Camada `service`: **73,5%** de instruções / **61,2%** de *branches*.
- Destaques: `AuthService`, `DenunciaService`, `ImovelService` e
  `NotificacaoService` com **100%** de instruções; `AnuncioService` com
  **95,2%** e `PasswordResetService` com **95,7%**.

Na T5.10 foram adicionadas suítes unitárias para `AnuncioService`,
`ImovelService`, `AuthService`, `PasswordResetService`,
`EmailVerificationService`, `DenunciaService` e `NotificacaoService`, cobrindo
os fluxos críticos de publicação, autenticação, recuperação de senha,
moderação e notificações. Os valores acima foram gerados por `mvn clean test`;
detalhes estão em [`cobertura-jacoco.md`](cobertura-jacoco.md).
