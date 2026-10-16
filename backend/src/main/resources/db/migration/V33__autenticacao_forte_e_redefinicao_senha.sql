CREATE TABLE identidade.dispositivos_confiaveis (
    id uuid PRIMARY KEY,
    usuario_id uuid NOT NULL REFERENCES identidade.usuarios(id) ON DELETE CASCADE,
    token_hash varchar(64) NOT NULL UNIQUE,
    user_agent_hash varchar(64),
    nome_dispositivo varchar(120),
    versao_seguranca bigint NOT NULL,
    criado_em timestamptz NOT NULL,
    ultimo_uso_em timestamptz NOT NULL,
    expira_em timestamptz NOT NULL,
    revogado_em timestamptz,
    lock_version bigint NOT NULL DEFAULT 0
);

CREATE INDEX idx_dispositivos_confiaveis_usuario
    ON identidade.dispositivos_confiaveis(usuario_id);
CREATE INDEX idx_dispositivos_confiaveis_expiracao
    ON identidade.dispositivos_confiaveis(expira_em);

CREATE TABLE identidade.tokens_redefinicao_senha (
    id uuid PRIMARY KEY,
    usuario_id uuid NOT NULL REFERENCES identidade.usuarios(id) ON DELETE CASCADE,
    token_hash varchar(64) NOT NULL UNIQUE,
    criado_em timestamptz NOT NULL,
    expira_em timestamptz NOT NULL,
    usado_em timestamptz,
    revogado_em timestamptz,
    solicitado_ip_hash varchar(64),
    lock_version bigint NOT NULL DEFAULT 0
);

CREATE INDEX idx_tokens_redefinicao_usuario
    ON identidade.tokens_redefinicao_senha(usuario_id);
CREATE INDEX idx_tokens_redefinicao_expiracao
    ON identidade.tokens_redefinicao_senha(expira_em);
