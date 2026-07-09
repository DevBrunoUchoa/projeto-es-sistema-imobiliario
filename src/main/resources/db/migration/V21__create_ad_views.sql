-- ============================================================================
-- ad_views — visualizações de um anúncio agrupadas por dia (RF-17)
-- ============================================================================
-- Em vez de logar 1 linha por visualização individual (a tabela cresceria
-- sem limite e a consulta "visualizações por dia" viraria um GROUP BY caro
-- sobre potencialmente milhões de linhas), mantemos no máximo 1 linha por
-- par (anúncio, dia) e incrementamos um contador via upsert
-- (INSERT ... ON CONFLICT ... DO UPDATE). Suficiente pro que a RF-17 pede
-- ("contador de visualizações com agrupamento por dia") e ordens de
-- grandeza mais barato de consultar/manter.
--
-- ads.visualizacoes (V9) continua existindo e sendo incrementada em paralelo
-- — é o total agregado exibido em GET /anuncios/:id; esta tabela aqui é só
-- a granularidade diária usada pelas estatísticas (GET /anuncios/:id/estatisticas).
CREATE TABLE ad_views (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ad_id              UUID NOT NULL REFERENCES ads(id) ON DELETE CASCADE,
    data_visualizacao  DATE NOT NULL DEFAULT CURRENT_DATE,
    quantidade         INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT uq_ad_views_ad_dia UNIQUE (ad_id, data_visualizacao)
);

CREATE INDEX idx_ad_views_ad_id ON ad_views (ad_id);
