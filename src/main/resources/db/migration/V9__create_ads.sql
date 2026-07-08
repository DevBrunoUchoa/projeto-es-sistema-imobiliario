-- ============================================================================
-- ads — anúncios de locação vinculados a um imóvel (RF-12 a RF-17, RF-21 a RF-25)
-- Esta é a tabela mais "pesada" do sistema: é nela que a busca (T5.6) roda.
-- ============================================================================

CREATE TABLE ads (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    imovel_id              UUID NOT NULL REFERENCES properties(id) ON DELETE CASCADE,
    locador_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    -- Não estava no diagrama de classes, mas RF-21 ("busca textual") e o
    -- índice GIN pedido pelo RNF/ESC-02 exigem um título curto e pesquisável
    -- (a descrição sozinha é longa demais pra ser o "match forte" da busca).
    titulo                 VARCHAR(150) NOT NULL,

    tipo_oferta            VARCHAR(30) NOT NULL
                           CHECK (tipo_oferta IN ('IMOVEL_COMPLETO', 'VAGA_COMPARTILHADA')),
    preco_aluguel          NUMERIC(10, 2) NOT NULL CHECK (preco_aluguel >= 0),
    preco_condominio       NUMERIC(10, 2) NOT NULL DEFAULT 0,
    preco_iptu             NUMERIC(10, 2) NOT NULL DEFAULT 0,
    status                 VARCHAR(20) NOT NULL DEFAULT 'ATIVO'
                           CHECK (status IN ('ATIVO', 'INATIVO', 'ALUGADO')),

    -- Pré-computados por um job assíncrono (T5.5) — nunca calculados em tempo
    -- real na busca (RNF/PER-04). Ficam NULL até o job rodar pela 1ª vez.
    distancia_ufcg_metros  INTEGER,
    tempo_pe_min           INTEGER,
    tempo_onibus_min       INTEGER,
    geo_fallback           BOOLEAN NOT NULL DEFAULT FALSE,

    descricao              TEXT,
    vagas_total            INTEGER NOT NULL DEFAULT 1 CHECK (vagas_total >= 1),
    vagas_disponiveis      INTEGER NOT NULL DEFAULT 1 CHECK (vagas_disponiveis >= 0),
    destaque               BOOLEAN NOT NULL DEFAULT FALSE,
    visualizacoes          INTEGER NOT NULL DEFAULT 0,

    -- Coluna gerada automaticamente pelo Postgres a partir de título+descrição.
    -- "STORED" = calculada uma vez na escrita e guardada fisicamente (mais
    -- rápida de consultar do que recalcular em cada busca); título pesa mais
    -- que descrição (setweight 'A' > 'B') no ranking de relevância (RF-21).
    search_vector          TSVECTOR GENERATED ALWAYS AS (
                                setweight(to_tsvector('portuguese', coalesce(titulo, '')), 'A') ||
                                setweight(to_tsvector('portuguese', coalesce(descricao, '')), 'B')
                            ) STORED,

    data_publicacao        TIMESTAMPTZ,
    data_expiracao         TIMESTAMPTZ,
    data_criacao            TIMESTAMPTZ NOT NULL DEFAULT now(),
    data_atualizacao        TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT chk_ads_vagas_disponiveis CHECK (vagas_disponiveis <= vagas_total)
);

CREATE TRIGGER set_timestamp_ads
BEFORE UPDATE ON ads
FOR EACH ROW
EXECUTE FUNCTION trigger_set_timestamp();

-- RF-12: "bloquear duplicidade de anúncio ativo por imóvel" — em vez de deixar
-- essa regra só na camada de serviço (onde uma corrida entre duas requisições
-- simultâneas poderia burlar a checagem), um índice único PARCIAL garante isso
-- no próprio banco: só pode existir 1 linha com status='ATIVO' por imovel_id.
CREATE UNIQUE INDEX uq_ads_um_ativo_por_imovel ON ads (imovel_id) WHERE status = 'ATIVO';

-- GIN sobre o tsvector: usado pela busca textual (RF-21, RNF/ESC-02).
CREATE INDEX idx_ads_search_vector ON ads USING GIN (search_vector);

-- B-Tree: usados por filtros/ordenação simples (RF-22/RF-23, meta de
-- p95 < 2s do RNF/PER-01).
CREATE INDEX idx_ads_preco_aluguel ON ads (preco_aluguel);
CREATE INDEX idx_ads_data_criacao  ON ads (data_criacao DESC);
CREATE INDEX idx_ads_status        ON ads (status);
CREATE INDEX idx_ads_locador_id    ON ads (locador_id);

COMMENT ON COLUMN ads.search_vector IS 'Gerada automaticamente pelo Postgres (título peso A, descrição peso B) — não é preenchida pela aplicação.';
