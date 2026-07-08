-- ============================================================================
-- audit_logs — trilha de auditoria de ações críticas (RNF/SEG-06)
-- Precisa ser append-only de verdade. Um simples "não crie endpoint de
-- UPDATE/DELETE" não é suficiente: qualquer bug ou query manual ainda
-- conseguiria alterar/apagar linhas. Por isso a imutabilidade é garantida no
-- próprio banco, via trigger, independente de qual usuário/role da aplicação
-- estiver conectado.
-- ============================================================================

CREATE TABLE audit_logs (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id            UUID REFERENCES users(id) ON DELETE SET NULL,
    acao               VARCHAR(100) NOT NULL,
    entidade           VARCHAR(100) NOT NULL,
    entidade_id        UUID,
    dados_anteriores   JSONB,
    dados_novos        JSONB,
    ip_address         VARCHAR(45),
    data_criacao       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_logs_entidade ON audit_logs (entidade, entidade_id);
CREATE INDEX idx_audit_logs_user_id  ON audit_logs (user_id);

-- Bloqueia UPDATE/DELETE na tabela inteira, para qualquer role de conexão.
-- Um simples "REVOKE UPDATE, DELETE ... FROM PUBLIC" NÃO seria suficiente
-- aqui, porque o dono da tabela (owner) sempre ignora GRANT/REVOKE — só um
-- trigger que recusa a operação é garantia de verdade.
CREATE OR REPLACE FUNCTION audit_logs_block_mutation()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'audit_logs é append-only: UPDATE e DELETE não são permitidos (RNF/SEG-06)';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_audit_logs_no_update
BEFORE UPDATE ON audit_logs
FOR EACH ROW EXECUTE FUNCTION audit_logs_block_mutation();

CREATE TRIGGER trg_audit_logs_no_delete
BEFORE DELETE ON audit_logs
FOR EACH ROW EXECUTE FUNCTION audit_logs_block_mutation();
