-- ============================================================================
-- ad_rules — regras de convivência de um anúncio (relação 1:1 com ads)
-- ============================================================================

CREATE TABLE ad_rules (
    -- A própria FK é a PK: garante 1:1 real com "ads" sem precisar de UNIQUE
    -- separado.
    ad_id               UUID PRIMARY KEY REFERENCES ads(id) ON DELETE CASCADE,
    aceita_fumantes     BOOLEAN NOT NULL DEFAULT FALSE,
    pet_friendly        BOOLEAN NOT NULL DEFAULT FALSE,
    restricao_genero    VARCHAR(20) NOT NULL DEFAULT 'SEM_RESTRICAO'
                        CHECK (restricao_genero IN ('SEM_RESTRICAO', 'MASCULINO', 'FEMININO')),
    nivel_barulho       VARCHAR(20)
                        CHECK (nivel_barulho IN ('SILENCIOSO', 'MODERADO', 'AGITADO')),
    alimentacao_inclusa VARCHAR(20) NOT NULL DEFAULT 'NENHUMA'
                        CHECK (alimentacao_inclusa IN ('NENHUMA', 'CAFE', 'ALMOCO', 'JANTAR', 'COMPLETA')),
    permite_visitas     BOOLEAN NOT NULL DEFAULT TRUE,
    horario_silencio    VARCHAR(50)
);
