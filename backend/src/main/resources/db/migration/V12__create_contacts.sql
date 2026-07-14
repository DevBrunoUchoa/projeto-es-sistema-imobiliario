-- ============================================================================
-- contacts — registro de interesse / mensagem de um estudante para um anúncio
-- (RF-28 "Registro de Interesse e Envio de Mensagem"; o endpoint de produto é
-- POST /interesses, mas a tabela chama "contacts" no ER — é a mesma coisa).
-- É essa tabela que libera o contato direto do locador (RNF/LEG-03) e que dá
-- permissão pra avaliar o anúncio depois (RF-29 exige "contato prévio").
-- ============================================================================

CREATE TABLE contacts (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    estudante_id  UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    ad_id         UUID NOT NULL REFERENCES ads(id) ON DELETE CASCADE,
    mensagem      TEXT NOT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'ENVIADO'
                  CHECK (status IN ('ENVIADO', 'LIDO', 'RESPONDIDO')),
    data_criacao  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_contacts_ad_id        ON contacts (ad_id);
CREATE INDEX idx_contacts_estudante_id ON contacts (estudante_id);

-- Usado por reviews.avaliador_id/ad_id para checar "contato_previo" (RF-29)
-- sem precisar de subquery cara: um índice composto cobre a checagem direto.
CREATE INDEX idx_contacts_estudante_ad ON contacts (estudante_id, ad_id);
