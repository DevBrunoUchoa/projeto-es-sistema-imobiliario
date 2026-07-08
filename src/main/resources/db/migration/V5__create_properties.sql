-- ============================================================================
-- properties — imóveis cadastrados pelos proprietários (RF-11)
-- ============================================================================

CREATE TABLE properties (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    proprietario_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tipo            VARCHAR(20) NOT NULL
                    CHECK (tipo IN ('APARTAMENTO', 'QUARTO', 'FLAT', 'PENSIONATO')),
    cep             VARCHAR(9)  NOT NULL,
    rua             VARCHAR(200) NOT NULL,
    numero          VARCHAR(20)  NOT NULL,
    complemento     VARCHAR(100),
    bairro          VARCHAR(100) NOT NULL,
    cidade          VARCHAR(100) NOT NULL DEFAULT 'Campina Grande',
    estado          CHAR(2)      NOT NULL DEFAULT 'PB',
    latitude        DOUBLE PRECISION NOT NULL,
    longitude       DOUBLE PRECISION NOT NULL,

    -- Coluna geoespacial (PostGIS) usada para os cálculos de distância até a
    -- UFCG e para buscas por raio. Não está desenhada no diagrama de classes
    -- (lá aparecem só latitude/longitude), mas RNF/ESC-02 pede explicitamente
    -- um índice espacial (GiST), que só é possível sobre um tipo `geometry`.
    geom            geometry(Point, 4326),

    ativo           BOOLEAN NOT NULL DEFAULT TRUE,
    data_criacao    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Mantém "geom" sempre sincronizado com latitude/longitude automaticamente,
-- tanto em INSERT quanto em UPDATE — a aplicação só precisa gravar lat/lon.
CREATE OR REPLACE FUNCTION properties_sync_geom()
RETURNS TRIGGER AS $$
BEGIN
    NEW.geom = ST_SetSRID(ST_MakePoint(NEW.longitude, NEW.latitude), 4326);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_properties_sync_geom
BEFORE INSERT OR UPDATE OF latitude, longitude ON properties
FOR EACH ROW
EXECUTE FUNCTION properties_sync_geom();

-- GiST: índice espacial usado por buscas geoespaciais (ST_DWithin, ST_Distance...).
CREATE INDEX idx_properties_geom            ON properties USING GIST (geom);
CREATE INDEX idx_properties_proprietario_id ON properties (proprietario_id);
CREATE INDEX idx_properties_bairro          ON properties (bairro);

COMMENT ON COLUMN properties.geom IS 'Ponto geográfico (SRID 4326) derivado automaticamente de latitude/longitude via trigger.';
