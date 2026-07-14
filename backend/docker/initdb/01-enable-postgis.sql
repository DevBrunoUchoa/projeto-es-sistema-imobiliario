-- Habilita a extensão PostGIS automaticamente na inicialização do banco.
-- Executado apenas na primeira criação do volume de dados
-- (scripts em /docker-entrypoint-initdb.d só rodam com o data dir vazio).
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS postgis_topology;
