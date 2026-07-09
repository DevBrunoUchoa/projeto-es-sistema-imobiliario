-- ============================================================================
-- RF-14 (T5.5.4): status possíveis de um anúncio.
-- ============================================================================
-- V9 criou ads.status com CHECK ('ATIVO', 'INATIVO', 'ALUGADO') — pensado na
-- época só pra estrutura de dados (T5.2, entidade "stub"). A tarefa T5.5.4
-- pede explicitamente um terceiro estado de inativação lógica, SUSPENSO,
-- distinto de INATIVO (decisão do próprio locador) — pense em suspensão
-- administrativa/temporária (ex.: denúncia em análise, T5.10). ALUGADO
-- permanece no conjunto: é um estado de ciclo de vida diferente (unidade
-- efetivamente alugada), fora do escopo do PATCH de status do T5.5.4, mas
-- ainda uma transição válida da tabela.
--
-- Não sabemos o nome exato que o Postgres deu à constraint (gerado
-- automaticamente a partir de "CHECK (status IN (...))" dentro do CREATE
-- TABLE), mas o padrão de nomenclatura do Postgres pra isso é
-- "<tabela>_<coluna>_check" — daí o DROP CONSTRAINT IF EXISTS abaixo com
-- esse nome specific, seguido de uma nova constraint com o mesmo nome e o
-- conjunto de valores atualizado.
ALTER TABLE ads DROP CONSTRAINT IF EXISTS ads_status_check;
ALTER TABLE ads ADD CONSTRAINT ads_status_check
    CHECK (status IN ('ATIVO', 'INATIVO', 'ALUGADO', 'SUSPENSO'));
