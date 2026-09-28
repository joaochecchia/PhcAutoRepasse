-- Índices funcionais para os filtros textuais case-insensitive mais usados na busca pública.
CREATE INDEX idx_enderecos_anuncio_busca_localizacao
    ON catalogo.enderecos_anuncio (lower(uf), lower(cidade), id);

CREATE INDEX idx_veiculos_busca_marca_tipo_ano
    ON catalogo.veiculos (lower(fabricante), tipo, ano_modelo, id);

CREATE INDEX idx_usuarios_busca_nome_perfil
    ON identidade.usuarios (tipo_pessoa, lower(nome), id);

CREATE INDEX idx_usuarios_pj_busca_nome_fantasia
    ON identidade.usuarios_pj (lower(nome_fantasia), usuario_id)
    WHERE nome_fantasia IS NOT NULL;
