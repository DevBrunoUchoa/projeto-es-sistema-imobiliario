-- ============================================================================
-- ad_images — fotos de um anúncio (RF-19, até 10 por anúncio)
-- ============================================================================

CREATE TABLE ad_images (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ad_id        UUID NOT NULL REFERENCES ads(id) ON DELETE CASCADE,
    url          VARCHAR(500) NOT NULL,
    ordem        INTEGER NOT NULL DEFAULT 0,
    principal    BOOLEAN NOT NULL DEFAULT FALSE,
    data_criacao TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_ad_images_ad_id ON ad_images (ad_id);

-- No máximo 1 imagem marcada como "principal" por anúncio.
CREATE UNIQUE INDEX uq_ad_images_uma_principal ON ad_images (ad_id) WHERE principal = TRUE;

-- O limite de "até 10 fotos por anúncio" (RF-19) é uma regra de fluxo de
-- upload, não de integridade referencial — fica melhor validada na camada de
-- serviço (T5.9) do que travada aqui via trigger.
