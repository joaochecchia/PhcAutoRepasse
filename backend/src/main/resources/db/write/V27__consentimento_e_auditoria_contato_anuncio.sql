ALTER TABLE catalogo.anuncios
    ADD COLUMN contato_whatsapp_autorizado BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN contato_ligacao_autorizado BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN contato_consentido_em TIMESTAMPTZ,
    ADD COLUMN contato_texto_versao VARCHAR(32),
    ADD CONSTRAINT ck_anuncio_contato_consentimento CHECK (
        (NOT contato_whatsapp_autorizado AND NOT contato_ligacao_autorizado)
        OR (contato_consentido_em IS NOT NULL AND contato_texto_versao IS NOT NULL)
    );

CREATE TABLE catalogo.acessos_contato_anuncio (
    id UUID PRIMARY KEY,
    anuncio_id UUID NOT NULL REFERENCES catalogo.anuncios(id) ON DELETE CASCADE,
    interessado_id UUID NOT NULL REFERENCES identidade.usuarios(id) ON DELETE CASCADE,
    autorizado BOOLEAN NOT NULL,
    motivo VARCHAR(64) NOT NULL,
    endereco_rede VARCHAR(64),
    user_agent VARCHAR(512),
    ocorrido_em TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_acesso_contato_limite
    ON catalogo.acessos_contato_anuncio (interessado_id, anuncio_id, ocorrido_em DESC)
    WHERE autorizado = TRUE;
