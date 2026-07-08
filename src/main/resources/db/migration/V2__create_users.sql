-- ============================================================================
-- users — usuários da plataforma (estudantes, locadores, contas mistas, admin)
-- ============================================================================

CREATE TABLE users (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome_completo      VARCHAR(150)  NOT NULL,
    email              VARCHAR(180)  NOT NULL,
    senha_hash         VARCHAR(255)  NOT NULL,
    tipo_conta         VARCHAR(20)   NOT NULL
                       CHECK (tipo_conta IN ('ESTUDANTE', 'LOCADOR', 'MISTO', 'ADMIN')),
    telefone           VARCHAR(20),
    foto_url           VARCHAR(500),
    bio                TEXT,
    email_verificado   BOOLEAN       NOT NULL DEFAULT FALSE,
    ativo              BOOLEAN       NOT NULL DEFAULT TRUE,

    -- RNF/LEG-01: LGPD exige consentimento explícito registrado no cadastro.
    -- Não estava no diagrama de classes original; acrescentado aqui porque é
    -- fundamentalmente uma decisão de schema (não dá pra "adicionar depois"
    -- sem migração, e falta dela bloqueia RNF/LEG-01 desde o primeiro cadastro).
    aceite_lgpd        BOOLEAN       NOT NULL DEFAULT FALSE,
    aceite_lgpd_data   TIMESTAMPTZ,

    data_criacao       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    data_atualizacao   TIMESTAMPTZ   NOT NULL DEFAULT now(),
    ultimo_login       TIMESTAMPTZ
);

-- E-mail é o identificador de login: precisa ser único.
CREATE UNIQUE INDEX uq_users_email ON users (LOWER(email));

COMMENT ON TABLE users IS 'Usuários da plataforma: estudantes, locadores, contas mistas e administradores.';
COMMENT ON COLUMN users.tipo_conta IS 'ESTUDANTE | LOCADOR | MISTO | ADMIN';

-- ----------------------------------------------------------------------------
-- Função e gatilho genéricos para manter "data_atualizacao" sempre correta.
-- Reutilizados por qualquer tabela que tenha essa coluna (ver migrations
-- seguintes) — evita depender de código Java para não esquecer de atualizar
-- o campo em algum fluxo.
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION trigger_set_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.data_atualizacao = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_timestamp_users
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION trigger_set_timestamp();
