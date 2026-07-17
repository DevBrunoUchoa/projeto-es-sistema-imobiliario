# Integração Google, verificação de e-mail e sessão

## 1. Credenciais do Google

No Google Cloud Console, crie credenciais OAuth 2.0 do tipo **Aplicativo da Web**.

Cadastre:

- Origem JavaScript autorizada: `http://localhost:5173`
- URI de redirecionamento autorizada: `http://localhost:8080/login/oauth2/code/google`

No ambiente do backend, defina:

```env
GOOGLE_CLIENT_ID=seu-client-id
GOOGLE_CLIENT_SECRET=seu-client-secret
APP_FRONTEND_URL=http://localhost:5173
APP_COOKIE_SECURE=false
```

Ao executar o backend diretamente com Maven, exporte as variáveis antes:

```bash
export GOOGLE_CLIENT_ID="seu-client-id"
export GOOGLE_CLIENT_SECRET="seu-client-secret"
export APP_FRONTEND_URL="http://localhost:5173"
mvn spring-boot:run
```

Ao usar `docker compose up --build`, as variáveis são lidas do arquivo `backend/.env` e encaminhadas ao container.

## 2. Fluxo implementado

1. O botão Google abre `/oauth2/authorization/google`.
2. O Google retorna para `/login/oauth2/code/google` no backend.
3. O backend cria ou recupera o usuário, gera JWT e refresh token e grava os dois em cookies HttpOnly.
4. O backend redireciona para `/auth/google/success` no frontend.
5. O frontend consulta `/auth/me`, restaura o usuário e abre o perfil.
6. Requisições que retornarem 401 tentam `/auth/refresh` uma vez e repetem a requisição original.
7. Ao recarregar a página, o frontend consulta `/auth/me` e restaura a sessão pelos cookies.
8. O logout chama `/auth/logout` e apaga os cookies.

## 3. Verificação de e-mail

O link enviado passa a apontar para:

```text
http://localhost:5173/verificar-email?token=...
```

A página do frontend chama o backend e apresenta sucesso ou erro ao usuário.

Sem SMTP ativo, o link aparece no log do backend. Para teste local:

1. Cadastre um usuário.
2. Procure no terminal do backend a mensagem `[e-mail desabilitado]`.
3. Copie o link de confirmação e abra no navegador.

## 4. Testes sugeridos

- Entrar com e-mail e senha, recarregar `/perfil` e verificar se a sessão permanece.
- Esperar o JWT expirar ou remover apenas o cookie `jwt`; a próxima chamada deve usar o refresh token.
- Entrar com Google usando uma conta nova e conferir o usuário no banco.
- Entrar novamente com a mesma conta Google e conferir que não há usuário duplicado.
- Sair e confirmar que `/perfil` volta a exigir autenticação.
- Abrir um link de verificação válido, repetido e expirado.
