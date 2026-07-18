-- ============================================================================
-- Consentimento do estudante para liberar o próprio contato ao locador (RF-28
-- / RNF-LEG-03). Por padrão o contato do estudante NÃO é exposto; ao registrar
-- interesse, ele pode optar por liberar e-mail/telefone para o locador daquele
-- anúncio poder retornar o contato.
-- ============================================================================

ALTER TABLE contacts
    ADD COLUMN contato_liberado BOOLEAN NOT NULL DEFAULT FALSE;
