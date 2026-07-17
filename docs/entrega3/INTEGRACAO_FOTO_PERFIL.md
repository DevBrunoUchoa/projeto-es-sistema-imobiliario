# Integração da foto de perfil

## Como testar

1. Inicie o banco e o backend normalmente.
2. Inicie o frontend com `npm run dev`.
3. Faça login e abra **Meu perfil**.
4. Clique no botão de câmera sobre o avatar.
5. Escolha uma imagem JPEG, PNG ou WEBP de até 5 MB.

Sem Supabase configurado, o backend salva a imagem localmente em `backend/uploads/perfis/...` e a disponibiliza em `http://localhost:8080/uploads/...`.

Com Supabase configurado, o fluxo existente continua usando o bucket indicado por `SUPABASE_STORAGE_BUCKET`.

## Variáveis opcionais para desenvolvimento local

```env
APP_BACKEND_PUBLIC_URL=http://localhost:8080
LOCAL_UPLOAD_DIRECTORY=uploads
```
