-- ============================================================================
-- audit_logs.user_id é FK ON DELETE SET NULL (RNF/LEG-02: excluir conta deve
-- anonimizar o autor no log, sem apagar o log em si). Mas o trigger de V17
-- (RNF/SEG-06) bloqueia QUALQUER UPDATE na tabela, inclusive esse UPDATE
-- automático disparado pela própria FK — hoje excluir uma conta que já tem
-- audit_logs (ou seja, qualquer conta que já fez login/cadastro) sempre falha
-- com "audit_logs é append-only". As duas regras nunca foram testadas juntas.
--
-- Ajuste: a função de bloqueio de UPDATE passa a permitir só o caso exato de
-- anonimização (user_id vira NULL, todo o resto da linha continua igual).
-- Qualquer outra tentativa de alterar uma linha continua sendo rejeitada. O
-- trigger de DELETE (linha nunca pode ser apagada) não muda.
-- ============================================================================

CREATE OR REPLACE FUNCTION audit_logs_block_update()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.user_id IS NULL
       AND OLD.user_id IS NOT NULL
       AND NEW.id               IS NOT DISTINCT FROM OLD.id
       AND NEW.acao              IS NOT DISTINCT FROM OLD.acao
       AND NEW.entidade          IS NOT DISTINCT FROM OLD.entidade
       AND NEW.entidade_id       IS NOT DISTINCT FROM OLD.entidade_id
       AND NEW.dados_anteriores  IS NOT DISTINCT FROM OLD.dados_anteriores
       AND NEW.dados_novos       IS NOT DISTINCT FROM OLD.dados_novos
       AND NEW.ip_address        IS NOT DISTINCT FROM OLD.ip_address
       AND NEW.data_criacao      IS NOT DISTINCT FROM OLD.data_criacao
    THEN
        RETURN NEW;
    END IF;

    RAISE EXCEPTION 'audit_logs é append-only: UPDATE e DELETE não são permitidos (RNF/SEG-06)';
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER trg_audit_logs_no_update ON audit_logs;

CREATE TRIGGER trg_audit_logs_no_update
BEFORE UPDATE ON audit_logs
FOR EACH ROW EXECUTE FUNCTION audit_logs_block_update();
