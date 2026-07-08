-- ============================================================================
-- SEED DE DESENVOLVIMENTO — só é aplicada no profile "dev".
--
-- Fica numa location Flyway separada de db/migration (veja
-- spring.flyway.locations em application-dev.yml) para nunca rodar sem querer
-- em test/prod. Numerada V900 de propósito, bem acima das migrations reais,
-- pra nunca colidir com uma Vn nova adicionada em db/migration.
--
-- Cria 3 locadores fictícios + 20 imóveis/anúncios em bairros reais de
-- Campina Grande-PB, cobrindo os 4 tipos de imóvel e as duas modalidades de
-- oferta (imóvel completo / vaga compartilhada).
--
-- Senha de todos os usuários de seed: "Senha123!"
-- (hash bcrypt custo 12, gerado com Python `bcrypt` — o prefixo $2b$ é
-- compatível com o BCryptPasswordEncoder do Spring Security, que aceita
-- $2a$/$2b$/$2y$ na verificação).
-- ============================================================================

INSERT INTO users (id, nome_completo, email, senha_hash, tipo_conta, telefone, email_verificado, ativo, aceite_lgpd, aceite_lgpd_data)
VALUES
    ('a0000000-0000-0000-0000-000000000001', 'Maria Locadora Silva',  'maria.locadora@example.com', '$2b$12$3TWxwMzUTLJjAMH1L0zJguTugSO0S3uNQakLVvdtHvqzZV3Kyj08q', 'LOCADOR', '83999990001', TRUE, TRUE, TRUE, now()),
    ('a0000000-0000-0000-0000-000000000002', 'Joao Locador Souza',    'joao.locador@example.com',  '$2b$12$3TWxwMzUTLJjAMH1L0zJguTugSO0S3uNQakLVvdtHvqzZV3Kyj08q', 'LOCADOR', '83999990002', TRUE, TRUE, TRUE, now()),
    ('a0000000-0000-0000-0000-000000000003', 'Ana Conta Mista Costa', 'ana.mista@example.com',     '$2b$12$3TWxwMzUTLJjAMH1L0zJguTugSO0S3uNQakLVvdtHvqzZV3Kyj08q', 'MISTO',   '83999990003', TRUE, TRUE, TRUE, now()),
    ('a0000000-0000-0000-0000-000000000004', 'Estudante Exemplo UFCG','estudante@example.com',     '$2b$12$3TWxwMzUTLJjAMH1L0zJguTugSO0S3uNQakLVvdtHvqzZV3Kyj08q', 'ESTUDANTE', '83999990004', TRUE, TRUE, TRUE, now());

-- Gera 20 imóveis + anúncios em loop, variando bairro, tipo e locador.
-- Poderia ser 20 INSERTs explícitos, mas o loop deixa fácil ajustar a
-- quantidade e garante que cada imóvel tenha coordenadas plausíveis dentro
-- da área urbana de Campina Grande (aprox. lat -7.19 a -7.24, lon -35.85 a
-- -35.92).
DO $$
DECLARE
    bairros TEXT[] := ARRAY[
        'José Pinheiro', 'Bodocongó', 'Mirante', 'Catolé', 'Prata', 'Malvinas',
        'Liberdade', 'Centro', 'Cruzeiro', 'Alto Branco', 'Universitário',
        'Palmeira', 'Tambor', 'Jeremias', 'Monte Santo', 'Dinamérica',
        'Santa Rosa', 'Ligeiro', 'Itararé', 'São José'
    ];
    tipos    TEXT[] := ARRAY['APARTAMENTO', 'QUARTO', 'FLAT', 'PENSIONATO'];
    ofertas  TEXT[] := ARRAY['IMOVEL_COMPLETO', 'VAGA_COMPARTILHADA'];
    locadores UUID[] := ARRAY[
        'a0000000-0000-0000-0000-000000000001'::uuid,
        'a0000000-0000-0000-0000-000000000002'::uuid,
        'a0000000-0000-0000-0000-000000000003'::uuid
    ];
    v_property_id UUID;
    v_ad_id       UUID;
    v_locador     UUID;
    v_tipo        TEXT;
    v_lat         DOUBLE PRECISION;
    v_lon         DOUBLE PRECISION;
    i             INTEGER;
BEGIN
    FOR i IN 1..20 LOOP
        v_locador := locadores[1 + ((i - 1) % array_length(locadores, 1))];
        v_tipo    := tipos[1 + ((i - 1) % array_length(tipos, 1))];
        v_lat     := -7.2400 + (random() * 0.05);   -- dentro da área urbana de CG
        v_lon     := -35.9200 + (random() * 0.06);

        INSERT INTO properties (id, proprietario_id, tipo, cep, rua, numero, bairro, cidade, estado, latitude, longitude, ativo)
        VALUES (
            gen_random_uuid(), v_locador, v_tipo,
            '58400-000', 'Rua ' || bairros[i], (100 + i)::text,
            bairros[i], 'Campina Grande', 'PB', v_lat, v_lon, TRUE
        )
        RETURNING id INTO v_property_id;

        INSERT INTO ads (
            id, imovel_id, locador_id, titulo, tipo_oferta, preco_aluguel, preco_condominio,
            status, descricao, vagas_total, vagas_disponiveis, data_publicacao
        )
        VALUES (
            gen_random_uuid(), v_property_id, v_locador,
            v_tipo || ' em ' || bairros[i],
            ofertas[1 + ((i - 1) % array_length(ofertas, 1))],
            600 + (i * 35),
            CASE WHEN i % 3 = 0 THEN 120 ELSE 0 END,
            'ATIVO',
            'Imóvel próximo à UFCG, bairro ' || bairros[i] || '. Dados fictícios de seed (#' || i || ').',
            CASE WHEN i % 4 = 0 THEN 3 ELSE 1 END,
            CASE WHEN i % 4 = 0 THEN 2 ELSE 1 END,
            now()
        )
        RETURNING id INTO v_ad_id;

        INSERT INTO ad_rules (ad_id, aceita_fumantes, pet_friendly, restricao_genero, nivel_barulho, alimentacao_inclusa, permite_visitas)
        VALUES (v_ad_id, (i % 5 = 0), (i % 2 = 0), 'SEM_RESTRICAO', 'MODERADO', 'NENHUMA', TRUE);
    END LOOP;
END $$;

-- distancia_ufcg_metros fica NULL de propósito: em produção quem preenche é o
-- job assíncrono do RF-16/T5.5, não o seed. Se quiser testar a busca com
-- distância já calculada, rode o job manualmente após subir a aplicação.
