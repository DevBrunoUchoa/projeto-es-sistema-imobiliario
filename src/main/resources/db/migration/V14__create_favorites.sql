-- ============================================================================
-- favorites — anúncios favoritados por um usuário (RF-26/RF-27)
-- ============================================================================

CREATE TABLE favorites (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    ad_id        UUID NOT NULL REFERENCES ads(id) ON DELETE CASCADE,
    data_criacao TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_favorites_user_ad UNIQUE (user_id, ad_id)
);

CREATE INDEX idx_favorites_user_id ON favorites (user_id);
