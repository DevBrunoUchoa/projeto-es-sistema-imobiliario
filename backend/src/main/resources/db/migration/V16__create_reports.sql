-- ============================================================================
-- reports — denúncias de anúncios/usuários (RF-36/RF-37; fora do escopo do
-- T5, mas a tabela é criada agora para o ER ficar completo e consistente).
-- ============================================================================

CREATE TABLE reports (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    denunciante_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tipo_alvo           VARCHAR(20) NOT NULL CHECK (tipo_alvo IN ('ANUNCIO', 'USUARIO')),

    -- Referencia ads.id OU users.id dependendo de tipo_alvo — como o alvo
    -- pode ser qualquer um dos dois, não dá pra criar uma FK simples aqui.
    -- Integridade é responsabilidade da camada de serviço (T6+/admin).
    alvo_id             UUID NOT NULL,

    motivo              VARCHAR(30) NOT NULL
                        CHECK (motivo IN ('CONTEUDO_INADEQUADO', 'SPAM', 'FRAUDE', 'ASSEDIO', 'OUTROS')),
    descricao           TEXT,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDENTE'
                        CHECK (status IN ('PENDENTE', 'EM_ANALISE', 'RESOLVIDA', 'REJEITADA')),
    resolvido_por       UUID REFERENCES users(id),
    contador_denuncias  INTEGER NOT NULL DEFAULT 1,
    data_criacao        TIMESTAMPTZ NOT NULL DEFAULT now(),
    data_resolucao       TIMESTAMPTZ
);

CREATE INDEX idx_reports_alvo   ON reports (tipo_alvo, alvo_id);
CREATE INDEX idx_reports_status ON reports (status);
