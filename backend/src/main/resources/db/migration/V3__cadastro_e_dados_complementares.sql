-- Evolução aditiva: cadastros existentes não recebem dados pessoais fictícios.
ALTER TABLE identidade.usuarios ADD COLUMN telefone varchar(32);
ALTER TABLE identidade.usuarios ALTER COLUMN senha_hash DROP NOT NULL;

CREATE TABLE identidade.enderecos_usuario (
    usuario_id uuid NOT NULL PRIMARY KEY,
    cep varchar(8),
    cidade varchar(120),
    bairro varchar(120),
    rua varchar(200),
    numero varchar(20),
    complemento varchar(200),
    uf varchar(2),
    lock_version bigint NOT NULL DEFAULT 0
);

CREATE TABLE identidade.dados_compra_pf (
    usuario_id uuid NOT NULL PRIMARY KEY,
    rg varchar(30),
    nome_pai varchar(160),
    nome_mae varchar(160),
    naturalidade varchar(160),
    genero varchar(60),
    lock_version bigint NOT NULL DEFAULT 0
);

CREATE TABLE identidade.dados_compra_pj (
    usuario_id uuid NOT NULL PRIMARY KEY,
    inscricao_estadual varchar(30),
    regime_tributario varchar(80),
    lock_version bigint NOT NULL DEFAULT 0
);

CREATE TABLE identidade.identidades_externas (
    id uuid NOT NULL PRIMARY KEY,
    usuario_id uuid NOT NULL,
    provedor varchar(32) NOT NULL,
    identificador_externo varchar(255) NOT NULL,
    lock_version bigint NOT NULL DEFAULT 0,
    CHECK (provedor IN ('GOOGLE', 'FACEBOOK'))
);

CREATE INDEX idx_identidades_externas_usuario ON identidade.identidades_externas(usuario_id);
