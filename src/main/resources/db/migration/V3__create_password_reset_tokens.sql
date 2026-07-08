-- ============================================================================
-- password_reset_tokens — tokens de uso único para recuperação de senha (RF-04)
-- ============================================================================

CREATE TABLE password_reset_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(255) NOT NULL,
    expira_em   TIMESTAMPTZ NOT NULL,
    usado       BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens (user_id);

-- Nunca guardamos o token em texto puro — só o hash (mesma lógica de senha).
COMMENT ON COLUMN password_reset_tokens.token_hash IS 'Hash do token enviado por e-mail; o valor em texto puro nunca é persistido.';
