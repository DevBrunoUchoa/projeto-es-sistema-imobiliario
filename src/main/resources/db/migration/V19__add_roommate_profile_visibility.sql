-- ============================================================================
-- roommate_profiles.ja_possui_casa / perfil_visivel — RF-32 pede que o card
-- público no mural de roommates carregue esses dois campos, mas eles não
-- estavam no diagrama de classes original (só "ativo" existia).
--
-- Por que os dois separados de "ativo": "ativo" controla se o usuário está
-- participando do sistema de roommates como um todo (a coluna já existia);
-- "perfil_visivel" é um controle de privacidade mais fino — dá pra estar
-- "ativo" (recebendo/calculando compatibilidade) sem necessariamente aparecer
-- publicamente no mural ainda.
-- ============================================================================

ALTER TABLE roommate_profiles
    ADD COLUMN ja_possui_casa BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN perfil_visivel BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN roommate_profiles.ja_possui_casa IS 'TRUE = usuário já tem moradia e busca colega para dividir; FALSE = está procurando moradia+colega junto.';
COMMENT ON COLUMN roommate_profiles.perfil_visivel IS 'Controla se o card aparece publicamente no mural de roommates (RF-32); distinto de "ativo".';
