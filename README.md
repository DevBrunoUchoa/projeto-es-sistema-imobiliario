# Campus Living — Sistema Imobiliário

Projeto com:

- Backend: Java 21, Spring Boot, Maven e PostgreSQL
- Frontend: React + Vite
- Autenticação: e-mail/senha, JWT em cookies HttpOnly, refresh token e Google OAuth2
- Banco local: PostgreSQL via Docker Compose

## 1. Pré-requisitos

Instale antes de iniciar:

- Java JDK 21
- Maven 3.9 ou superior
- Node.js 20 ou superior
- npm
- Docker Desktop

Confirme no terminal:

```bash
java -version
mvn -version
node -v
npm -v
docker --version
```

O Java exibido por `mvn -version` também deve ser o Java 21.

No macOS, caso o Maven use outra versão:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

## 2. Descompactar e acessar o projeto

```bash
unzip projeto-es-sistema-imobiliario-completo.zip
cd projeto-es-sistema-imobiliario
```

## 3. Criar as credenciais do Google

Esta etapa é necessária apenas para testar o botão **Continuar com Google**.

No Google Cloud Console:

1. Crie ou selecione um projeto.
2. Configure a tela de consentimento OAuth.
3. Crie uma credencial OAuth 2.0 do tipo **Aplicativo da Web**.
4. Em **Origens JavaScript autorizadas**, adicione:

```text
http://localhost:5173
```

5. Em **URIs de redirecionamento autorizados**, adicione:

```text
http://localhost:8080/login/oauth2/code/google
```

6. Copie o Client ID e o Client Secret.

## 4. Configurar o backend

Entre na pasta:

```bash
cd backend
```

Crie o arquivo local de ambiente:

```bash
cp .env.example .env
```

No arquivo `.env`, preencha pelo menos:

```env
GOOGLE_CLIENT_ID=seu-client-id
GOOGLE_CLIENT_SECRET=seu-client-secret
APP_FRONTEND_URL=http://localhost:5173
APP_COOKIE_SECURE=false
```

Não envie o `.env` real para o Git.

### Importante ao executar com Maven

O Maven não lê automaticamente o arquivo `.env`. Antes de iniciar, exporte as variáveis no terminal:

```bash
export GOOGLE_CLIENT_ID="seu-client-id"
export GOOGLE_CLIENT_SECRET="seu-client-secret"
export APP_FRONTEND_URL="http://localhost:5173"
export APP_COOKIE_SECURE="false"
```

Sem credenciais válidas do Google, use valores vazios apenas caso o perfil local permita. Para testar o login Google, as credenciais são obrigatórias.

## 5. Subir o PostgreSQL

Ainda dentro de `backend`:

```bash
docker compose up -d db
```

Confira se o banco está ativo:

```bash
docker compose ps
```

Para ver logs do banco:

```bash
docker compose logs -f db
```

## 6. Iniciar o backend

Na pasta `backend`:

```bash
mvn clean spring-boot:run
```

Quando estiver funcionando, estarão disponíveis:

- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui/index.html
- Health: http://localhost:8080/actuator/health

Caso apareçam erros antigos de Lombok, confirme novamente que `mvn -version` aponta para Java 21 e execute:

```bash
mvn clean
mvn spring-boot:run
```

## 7. Iniciar o frontend

Abra outro terminal e, a partir da raiz do projeto:

```bash
cd frontend
npm install
npm run dev
```

Acesse:

```text
http://localhost:5173
```

Não use `node server.js` para o frontend React. Esse comando pertence ao frontend legado.

## 8. Testar os fluxos

### Cadastro e login comum

1. Abra http://localhost:5173.
2. Crie uma conta.
3. Faça login com e-mail e senha.
4. Atualize a página protegida para confirmar que a sessão é restaurada.
5. Clique em sair para confirmar que os cookies são removidos.

### Login com Google

1. Mantenha backend e frontend ligados.
2. Na tela de login, clique em **Continuar com Google**.
3. Escolha a conta Google.
4. O Google retornará ao backend.
5. O backend criará os cookies JWT e refresh token.
6. O navegador será redirecionado para:

```text
http://localhost:5173/auth/google/success
```

7. O frontend consultará `GET /auth/me` e restaurará o usuário autenticado.

### Verificação de e-mail

Quando o SMTP estiver desativado, o backend escreve no terminal um link acompanhado de:

```text
[e-mail desabilitado]
```

Copie o link e abra no navegador. Ele deve levar a:

```text
http://localhost:5173/verificar-email?token=...
```

### Refresh token

Quando uma chamada protegida retornar `401`, o frontend tenta automaticamente:

```text
POST /auth/refresh
```

Se o refresh token estiver válido, a chamada original é repetida.

## 9. Comandos úteis

Parar o banco:

```bash
cd backend
docker compose down
```

Parar e apagar os dados locais do banco:

```bash
docker compose down -v
```

Reinstalar dependências do frontend:

```bash
cd frontend
rm -rf node_modules
npm install
```

Gerar build de produção do frontend:

```bash
npm run build
```

## 10. Problemas comuns

### Porta 8080 ocupada

No macOS/Linux:

```bash
lsof -i :8080
```

Encerre o processo correspondente ou altere a porta do backend.

### Porta 5173 ocupada

```bash
lsof -i :5173
```

### PostgreSQL não inicia

```bash
cd backend
docker compose logs db
```

Confirme que o Docker Desktop está aberto.

### `redirect_uri_mismatch` no Google

A URI cadastrada no Google deve ser exatamente:

```text
http://localhost:8080/login/oauth2/code/google
```

Não substitua por uma rota do frontend.

### Login funciona, mas a sessão desaparece

Confirme:

- frontend em `http://localhost:5173`;
- backend em `http://localhost:8080`;
- `APP_COOKIE_SECURE=false` no ambiente local;
- chamadas frontend usando `credentials: include`;
- navegador aceitando cookies locais.

## Estrutura principal

```text
projeto-es-sistema-imobiliario/
├── backend/
├── frontend/
├── frontend-legado/
├── docs/
├── INTEGRACAO_GOOGLE_EMAIL.md
└── README.md
```
