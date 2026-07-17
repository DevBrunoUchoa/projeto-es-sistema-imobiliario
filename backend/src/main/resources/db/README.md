# Banco de dados

Schema versionado com **Flyway**. Migrations em `db/migration/`, seed de
desenvolvimento em `db/seed/` (só carregado no profile `dev` — veja
`spring.flyway.locations` em `application-dev.yml`).

## Convenções usadas nas migrations

- Uma tabela por arquivo, numeradas na ordem de dependência das FKs
  (`V1__enable_extensions.sql`, `V2__create_users.sql`, ...).
- Nunca edite uma migration já commitada/aplicada em algum ambiente. Depois
  que uma `Vn` foi aplicada em qualquer banco compartilhado (dev remoto, CI,
  produção), qualquer ajuste vira uma **nova** migration `Vn+1`.
- Enums de domínio (`tipo_conta`, `status_anuncio`, etc.) são `VARCHAR` +
  `CHECK constraint`, não o tipo nativo `ENUM` do Postgres — alterar a lista de
  valores permitidos é só uma migration de `ALTER TABLE ... DROP/ADD
  CONSTRAINT`, sem o processo mais burocrático de `ALTER TYPE`.
- `id` é sempre `UUID DEFAULT gen_random_uuid()`. UUID evita vazar contagem de
  registros pela URL (`/anuncios/1`, `/anuncios/2`...) e permite gerar o ID no
  cliente antes de persistir, se algum dia for útil.
- Toda tabela com `data_atualizacao` usa o trigger `trigger_set_timestamp()`
  (definido em `V2__create_users.sql`) — a aplicação nunca precisa lembrar de
  setar esse campo manualmente.
- Regras de negócio que o banco consegue garantir sozinho (não duplicar
  anúncio ativo por imóvel, não avaliar duas vezes o mesmo anúncio, não haver
  match consigo mesmo, `audit_logs` ser append-only) viram `CONSTRAINT`,
  índice único parcial ou trigger — não ficam só na camada de serviço Java,
  porque a camada de serviço não protege contra corrida entre requisições
  concorrentes nem contra alguém rodando SQL manual.

## Rodando localmente

```bash
docker compose up -d db     # sobe só o Postgres/PostGIS
mvn spring-boot:run          # o Flyway aplica as migrations automaticamente no boot
```

Para inspecionar o schema aplicado:

```bash
docker exec -it campus-living-db psql -U $POSTGRES_USER -d $POSTGRES_DB -c "\dt"
```

## Produção

Em produção o Flyway roda exatamente as mesmas migrations — não existe
"schema de produção" diferente do de desenvolvimento. O que muda é: (1) o
seed de `db/seed/` nunca é carregado (só existe no location do profile
`dev`); (2) a conexão exige SSL (ver `application-prod.yml`).
