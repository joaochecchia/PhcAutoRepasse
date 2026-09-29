CREATE SCHEMA IF NOT EXISTS compliance;

CREATE TABLE compliance.aceites_termos (
    id UUID PRIMARY KEY,
    usuario_id UUID NOT NULL,
    aceite_em TIMESTAMPTZ NOT NULL,
    versao_termos_uso VARCHAR(64) NOT NULL,
    versao_politica_privacidade VARCHAR(64) NOT NULL,
    endereco_rede VARCHAR(255) NOT NULL,
    registrado_em TIMESTAMPTZ NOT NULL,
    lock_version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_aceites_termos_usuario_versoes
        UNIQUE (usuario_id, versao_termos_uso, versao_politica_privacidade)
);

CREATE INDEX idx_aceites_termos_usuario
    ON compliance.aceites_termos (usuario_id);

CREATE INDEX idx_aceites_termos_registrado_em
    ON compliance.aceites_termos (registrado_em DESC);
