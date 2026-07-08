-- ============================================================================
-- users.curso / users.instituicao — RF-06 pede que o próprio usuário consiga
-- atualizar "nome, bio, curso, instituição", mas essas duas colunas não
-- estavam no diagrama ER original (V2__create_users.sql). Sem elas o PUT
-- /usuarios/:id do T5.4 não teria onde gravar esses dados.
-- ============================================================================

ALTER TABLE users
    ADD COLUMN curso       VARCHAR(150),
    ADD COLUMN instituicao VARCHAR(150);

COMMENT ON COLUMN users.curso IS 'Curso do estudante na instituição (opcional; nao se aplica a LOCADOR).';
COMMENT ON COLUMN users.instituicao IS 'Instituição de ensino do estudante (ex.: UFCG).';
