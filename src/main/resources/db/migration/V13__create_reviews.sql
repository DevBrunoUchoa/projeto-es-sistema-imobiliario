-- ============================================================================
-- reviews — avaliações de anúncios/locadores (RF-29 a RF-31)
-- ============================================================================

CREATE TABLE reviews (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- ON DELETE SET NULL (não CASCADE): quando um usuário pede exclusão
    -- definitiva dos dados (RNF/LEG-02), a conta é removida mas a AVALIAÇÃO
    -- em si deve ser anonimizada e preservada (histórico do locador/anúncio
    -- não pode simplesmente desaparecer). Por isso as FKs de pessoa aqui são
    -- opcionais: se o usuário some, a referência vira NULL em vez de apagar
    -- a linha inteira.
    avaliador_id      UUID REFERENCES users(id) ON DELETE SET NULL,
    avaliado_id       UUID REFERENCES users(id) ON DELETE SET NULL,

    ad_id             UUID NOT NULL REFERENCES ads(id) ON DELETE CASCADE,
    nota              SMALLINT NOT NULL CHECK (nota BETWEEN 1 AND 5),
    comentario        TEXT,
    resposta_locador  TEXT,
    contato_previo    BOOLEAN NOT NULL DEFAULT FALSE,
    data_criacao      TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT chk_reviews_nao_auto_avaliacao CHECK (avaliador_id IS DISTINCT FROM avaliado_id)
);

-- RF-29: "bloquear mais de uma avaliação por par (usuario, anuncio)".
-- Índice parcial porque avaliador_id pode virar NULL depois de uma exclusão
-- LGPD, e nesse caso a unicidade deixa de fazer sentido.
CREATE UNIQUE INDEX uq_reviews_avaliador_ad ON reviews (avaliador_id, ad_id) WHERE avaliador_id IS NOT NULL;

CREATE INDEX idx_reviews_ad_id      ON reviews (ad_id);
CREATE INDEX idx_reviews_avaliado_id ON reviews (avaliado_id);
