CREATE TABLE identidade.sessoes_refresh_token (
    id UUID PRIMARY KEY,
    familia_id UUID NOT NULL,
    usuario_id UUID NOT NULL REFERENCES identidade.usuarios(id) ON DELETE CASCADE,
    token_hash CHAR(64) NOT NULL UNIQUE,
    criado_em TIMESTAMPTZ NOT NULL,
    expira_em TIMESTAMPTZ NOT NULL,
    usado_em TIMESTAMPTZ,
    revogado_em TIMESTAMPTZ
);

CREATE INDEX idx_refresh_token_familia
    ON identidade.sessoes_refresh_token (familia_id);
CREATE INDEX idx_refresh_token_usuario
    ON identidade.sessoes_refresh_token (usuario_id);
CREATE INDEX idx_refresh_token_expiracao
    ON identidade.sessoes_refresh_token (expira_em);
