ALTER TABLE identidade.usuarios ADD COLUMN foto_perfil_chave varchar(255);
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX idx_pj_nome_fantasia_trgm ON identidade.usuarios_pj
    USING gin (lower(nome_fantasia) gin_trgm_ops);
CREATE INDEX idx_enderecos_usuario_localidade ON identidade.enderecos_usuario
    (lower(uf), localizacao.normalizar_nome(cidade), usuario_id);
CREATE INDEX idx_anuncios_publicados_anunciante ON catalogo.anuncios
    (anunciante_id, criado_em DESC, id DESC) WHERE status = 'PUBLICADO';
