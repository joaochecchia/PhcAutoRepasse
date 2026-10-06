ALTER TABLE catalogo.enderecos_anuncio
    ADD COLUMN latitude double precision,
    ADD COLUMN longitude double precision;

ALTER TABLE catalogo.enderecos_anuncio
    ADD CONSTRAINT ck_enderecos_anuncio_latitude
        CHECK (latitude IS NULL OR latitude BETWEEN -90 AND 90),
    ADD CONSTRAINT ck_enderecos_anuncio_longitude
        CHECK (longitude IS NULL OR longitude BETWEEN -180 AND 180),
    ADD CONSTRAINT ck_enderecos_anuncio_coordenadas_completas
        CHECK ((latitude IS NULL) = (longitude IS NULL));

CREATE INDEX idx_enderecos_anuncio_coordenadas
    ON catalogo.enderecos_anuncio (latitude, longitude, id)
    WHERE latitude IS NOT NULL AND longitude IS NOT NULL;
