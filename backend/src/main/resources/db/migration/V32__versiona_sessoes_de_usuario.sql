ALTER TABLE identidade.usuarios
    ADD COLUMN sessao_versao bigint NOT NULL DEFAULT 0;
