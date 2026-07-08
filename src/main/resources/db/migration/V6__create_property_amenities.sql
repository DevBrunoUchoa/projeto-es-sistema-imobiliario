-- ============================================================================
-- property_amenities — comodidades de um imóvel (mobiliado, wifi, etc.)
-- ============================================================================

CREATE TABLE property_amenities (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    property_id UUID NOT NULL REFERENCES properties(id) ON DELETE CASCADE,
    nome        VARCHAR(30) NOT NULL
                CHECK (nome IN ('MOBILIADO', 'WIFI', 'AR_CONDICIONADO', 'GARAGEM',
                                 'ACADEMIA', 'LAVANDERIA', 'INTERNET_INCLUSA')),
    valor       BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT uq_property_amenities UNIQUE (property_id, nome)
);

CREATE INDEX idx_property_amenities_property_id ON property_amenities (property_id);
