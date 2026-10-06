ALTER TABLE identidade.enderecos_usuario
    ADD CONSTRAINT fk_enderecos_usuario_municipio
        FOREIGN KEY (municipio_codigo_ibge)
        REFERENCES localizacao.municipios (codigo_ibge),
    ADD CONSTRAINT ck_enderecos_usuario_municipio_informado
        CHECK (municipio_codigo_ibge IS NOT NULL) NOT VALID;

ALTER TABLE catalogo.enderecos_anuncio
    ADD CONSTRAINT fk_enderecos_anuncio_municipio
        FOREIGN KEY (municipio_codigo_ibge)
        REFERENCES localizacao.municipios (codigo_ibge),
    ADD CONSTRAINT ck_enderecos_anuncio_municipio_informado
        CHECK (municipio_codigo_ibge IS NOT NULL) NOT VALID;
