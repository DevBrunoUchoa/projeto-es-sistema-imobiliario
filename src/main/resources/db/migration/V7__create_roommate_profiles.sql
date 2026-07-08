-- ============================================================================
-- roommate_profiles — perfil de busca por colega de quarto (RF-10/RF-32)
-- ============================================================================

CREATE TABLE roommate_profiles (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- 1 perfil de roommate por usuário (o próprio diagrama trata como "possui",
    -- cardinalidade 0..1 do lado do usuário).
    user_id                  UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,

    descricao                TEXT,
    orcamento_max            NUMERIC(10, 2),
    data_entrada_desejada    DATE,
    periodo_min_meses        INTEGER,
    aceita_pets              BOOLEAN NOT NULL DEFAULT FALSE,
    fumante                  BOOLEAN NOT NULL DEFAULT FALSE,
    nivel_barulho_preferido  VARCHAR(20)
                             CHECK (nivel_barulho_preferido IN ('SILENCIOSO', 'MODERADO', 'AGITADO')),
    horario_acorda           TIME,
    horario_dorme            TIME,
    ativo                    BOOLEAN NOT NULL DEFAULT TRUE,
    data_atualizacao         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TRIGGER set_timestamp_roommate_profiles
BEFORE UPDATE ON roommate_profiles
FOR EACH ROW
EXECUTE FUNCTION trigger_set_timestamp();

-- Índice parcial: a listagem de "roommates compatíveis" (RF-33) só olha
-- perfis ativos, então não faz sentido indexar os inativos.
CREATE INDEX idx_roommate_profiles_ativo ON roommate_profiles (ativo) WHERE ativo = TRUE;
