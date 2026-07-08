-- ============================================================================
-- Habilita as extensões do PostgreSQL usadas pelo projeto.
--
-- Roda como parte do Flyway (não só no docker/initdb) porque em produção
-- (ex.: Supabase) não controlamos o processo de inicialização do container do
-- banco — só temos acesso via conexão SQL normal. Isso torna a extensão parte
-- do histórico de schema versionado, e não uma dependência "escondida" do
-- ambiente Docker local.
-- ============================================================================

-- PostGIS: tipos e funções geoespaciais (RNF/ESC-02, cálculo de distância até a UFCG).
CREATE EXTENSION IF NOT EXISTS postgis;

-- Observação: gen_random_uuid() já é nativo do PostgreSQL a partir da versão 13
-- (movido do módulo pgcrypto para o core), então não é preciso habilitar pgcrypto
-- só para gerar UUID como chave primária.
