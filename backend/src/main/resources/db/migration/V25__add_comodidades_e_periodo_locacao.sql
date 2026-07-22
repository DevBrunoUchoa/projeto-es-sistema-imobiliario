-- Mais opções de comodidade no cadastro/filtro de imóveis (mesmo padrão da V21).
ALTER TABLE properties ADD COLUMN IF NOT EXISTS seguranca_24h BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE properties ADD COLUMN IF NOT EXISTS lavanderia BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE properties ADD COLUMN IF NOT EXISTS internet_inclusa BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE properties ADD COLUMN IF NOT EXISTS mercadinho_proximo BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE properties ADD COLUMN IF NOT EXISTS gas_incluso BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE properties ADD COLUMN IF NOT EXISTS vaga_garagem BOOLEAN NOT NULL DEFAULT FALSE;

-- Período de locação do anúncio: janela de disponibilidade (data_disponivel_de
-- é obrigatória — DEFAULT CURRENT_DATE marca anúncios já existentes como
-- disponíveis a partir de hoje) e mínimo/máximo de meses aceitos pelo locador,
-- ambos opcionais (NULL = sem restrição). O aluno filtra por "quero ficar X
-- meses" e só vê anúncios cujo min/max aceitam esse X (RF de período variável,
-- não um contrato de duração fixa).
ALTER TABLE ads ADD COLUMN IF NOT EXISTS data_disponivel_de DATE NOT NULL DEFAULT CURRENT_DATE;
ALTER TABLE ads ADD COLUMN IF NOT EXISTS data_disponivel_ate DATE;
ALTER TABLE ads ADD COLUMN IF NOT EXISTS periodo_min_meses INTEGER CHECK (periodo_min_meses IS NULL OR periodo_min_meses >= 1);
ALTER TABLE ads ADD COLUMN IF NOT EXISTS periodo_max_meses INTEGER CHECK (periodo_max_meses IS NULL OR periodo_max_meses >= 1);

ALTER TABLE ads ADD CONSTRAINT chk_ads_periodo_max_maior_que_min
    CHECK (periodo_max_meses IS NULL OR periodo_min_meses IS NULL OR periodo_max_meses >= periodo_min_meses);
ALTER TABLE ads ADD CONSTRAINT chk_ads_disponivel_ate_apos_de
    CHECK (data_disponivel_ate IS NULL OR data_disponivel_ate >= data_disponivel_de);
