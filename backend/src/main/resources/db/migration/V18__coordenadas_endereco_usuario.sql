ALTER TABLE identidade.enderecos_usuario
    ADD COLUMN latitude double precision,
    ADD COLUMN longitude double precision;

ALTER TABLE identidade.enderecos_usuario
    ADD CONSTRAINT ck_enderecos_usuario_latitude
        CHECK (latitude IS NULL OR latitude BETWEEN -90 AND 90),
    ADD CONSTRAINT ck_enderecos_usuario_longitude
        CHECK (longitude IS NULL OR longitude BETWEEN -180 AND 180),
    ADD CONSTRAINT ck_enderecos_usuario_par_coordenadas
        CHECK ((latitude IS NULL) = (longitude IS NULL));

CREATE INDEX idx_enderecos_usuario_coordenadas
    ON identidade.enderecos_usuario (latitude, longitude, usuario_id)
    WHERE latitude IS NOT NULL AND longitude IS NOT NULL;
