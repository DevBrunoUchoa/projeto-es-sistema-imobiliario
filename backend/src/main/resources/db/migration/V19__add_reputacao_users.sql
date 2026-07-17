-- ============================================================================
-- users.nota_media / users.total_avaliacoes — reputação do locador (RF-30)
-- ============================================================================
-- RF-30 pede que a média/contagem sejam calculadas por um "Trigger ou Job" e
-- fiquem armazenadas como "cache" do perfil (RF-07 e RF-15 leem esses dois
-- campos como leitura simples, junto de nome/bio/foto_url). Trigger foi
-- escolhido em vez de código Java porque, como o db/README.md já registra
-- como convenção do projeto: regras que o banco garante sozinho (aqui,
-- consistência sob concorrência entre INSERTs simultâneos em reviews) viram
-- CONSTRAINT/índice/trigger, não ficam só na camada de serviço.

ALTER TABLE users
    ADD COLUMN nota_media       NUMERIC(3, 2) NOT NULL DEFAULT 0,
    ADD COLUMN total_avaliacoes INTEGER       NOT NULL DEFAULT 0;

COMMENT ON COLUMN users.nota_media IS 'Cache de AVG(reviews.nota) recalculado por trigger (RF-30). NUMERIC(3,2): nota vai de 1 a 5, então 5.00 é o máximo representável.';
COMMENT ON COLUMN users.total_avaliacoes IS 'Cache de COUNT(reviews.*) recalculado por trigger (RF-30).';

-- ----------------------------------------------------------------------------
-- Função auxiliar: recalcula a reputação de UM locador específico.
-- Separada da função de trigger para poder ser reutilizada (ex.: um script
-- de manutenção que precise "reprocessar" a reputação de todo mundo).
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION recalcular_reputacao_locador(p_avaliado_id UUID)
RETURNS void AS $$
BEGIN
    -- avaliado_id pode ser NULL (RNF/LEG-02: usuário excluído/anonimizado) —
    -- nesse caso não há mais um locador para atualizar, então não faz nada.
    IF p_avaliado_id IS NULL THEN
        RETURN;
    END IF;

    UPDATE users
    SET nota_media       = COALESCE(
                                (SELECT ROUND(AVG(nota), 2) FROM reviews WHERE avaliado_id = p_avaliado_id),
                                0
                            ),
        total_avaliacoes = (SELECT COUNT(*) FROM reviews WHERE avaliado_id = p_avaliado_id)
    WHERE id = p_avaliado_id;
END;
$$ LANGUAGE plpgsql;

-- ----------------------------------------------------------------------------
-- Trigger: dispara em qualquer mudança em reviews que possa afetar a
-- reputação de um locador — não só o INSERT "feliz" descrito no fluxo
-- principal do RF-30, mas também os casos de borda que o schema já previa:
--   • DELETE em reviews — acontece via CASCADE quando um anúncio é apagado
--     (ads.id ON DELETE CASCADE em V13__create_reviews.sql), removendo
--     avaliações de um locador sem passar por nenhum código Java.
--   • UPDATE de avaliado_id — acontece via ON DELETE SET NULL quando o
--     PRÓPRIO locador avaliado exclui a conta (RNF/LEG-02); nesse caso o
--     usuário está sumindo mesmo, mas a reputação é recalculada de qualquer
--     forma para manter users consistente com reviews em todo estado do banco.
--   • UPDATE de nota — não há RF que permita editar a nota após criada, mas
--     o trigger cobre esse caso também por segurança/robustez, a um custo
--     desprezível.
-- Responder uma avaliação (RF-31, coluna resposta_locador) NÃO deveria
-- disparar recálculo — por isso o UPDATE é filtrado por OF (avaliado_id, nota),
-- e não "UPDATE ON reviews" genérico.
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION trigger_recalcular_reputacao_locador()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'DELETE' THEN
        PERFORM recalcular_reputacao_locador(OLD.avaliado_id);
        RETURN OLD;
    ELSIF TG_OP = 'UPDATE' THEN
        PERFORM recalcular_reputacao_locador(NEW.avaliado_id);
        IF OLD.avaliado_id IS DISTINCT FROM NEW.avaliado_id THEN
            PERFORM recalcular_reputacao_locador(OLD.avaliado_id);
        END IF;
        RETURN NEW;
    ELSE
        PERFORM recalcular_reputacao_locador(NEW.avaliado_id);
        RETURN NEW;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_reviews_recalcular_reputacao
AFTER INSERT OR DELETE OR UPDATE OF avaliado_id, nota ON reviews
FOR EACH ROW
EXECUTE FUNCTION trigger_recalcular_reputacao_locador();