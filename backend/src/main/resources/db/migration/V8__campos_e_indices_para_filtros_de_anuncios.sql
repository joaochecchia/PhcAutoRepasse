-- Campos estruturados ausentes para os filtros públicos do catálogo.
ALTER TABLE catalogo.veiculos
    ADD COLUMN condicao varchar(16),
    ADD COLUMN tipo_freio varchar(60),
    ADD CONSTRAINT ck_veiculos_condicao CHECK (condicao IS NULL OR condicao IN ('ZERO_KM', 'USADO'));

ALTER TABLE catalogo.carros
    ADD COLUMN tipo_direcao varchar(60),
    ADD COLUMN cilindrada_litros decimal(4,1),
    ADD CONSTRAINT ck_carros_cilindrada_litros CHECK (cilindrada_litros IS NULL OR cilindrada_litros > 0);

ALTER TABLE catalogo.caminhoes
    ADD COLUMN tipo_direcao varchar(60);

ALTER TABLE catalogo.caminhonetes
    ADD COLUMN tipo_direcao varchar(60),
    ADD COLUMN cilindrada_litros decimal(4,1),
    ADD COLUMN blindado boolean,
    ADD CONSTRAINT ck_caminhonetes_cilindrada_litros CHECK (cilindrada_litros IS NULL OR cilindrada_litros > 0);

-- Índices orientados aos filtros mais seletivos e às ordenações por faixa.
CREATE INDEX idx_enderecos_anuncio_uf_cidade ON catalogo.enderecos_anuncio (uf, cidade, id);
CREATE INDEX idx_veiculos_tipo_fabricante_ano ON catalogo.veiculos (tipo, fabricante, ano_modelo, id);
CREATE INDEX idx_veiculos_condicao ON catalogo.veiculos (condicao) WHERE condicao IS NOT NULL;
CREATE INDEX idx_anuncios_publicados_preco ON catalogo.anuncios (preco_centavos, veiculo_id)
    WHERE status = 'PUBLICADO';
CREATE INDEX idx_usuarios_tipo_nome ON identidade.usuarios (tipo_pessoa, nome, id);
CREATE INDEX idx_usuarios_pj_nome_fantasia ON identidade.usuarios_pj (nome_fantasia, usuario_id)
    WHERE nome_fantasia IS NOT NULL;
