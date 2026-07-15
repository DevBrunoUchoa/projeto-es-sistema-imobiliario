# Frontend React — EstudanteLar

Migração inicial das telas de login, cadastro e perfil. O backend Java não foi alterado.

## Executar

Com o backend em `http://localhost:8080`:

```bash
cd frontend
npm install
npm run dev
```

Abra `http://localhost:5173`.

## Rotas

- `/login`
- `/cadastro`
- `/perfil`

## Integração

O Vite encaminha `/auth`, `/usuarios`, `/oauth2` e `/login` para o Spring Boot. As requisições usam `credentials: 'include'`, pois a autenticação está em cookies HttpOnly.

## Limitação conhecida

O backend não possui endpoint de logout. A ação de sair remove apenas o estado local do React; o cookie continua válido até expirar.
