ALTER TABLE assinaturas.planos
    ADD COLUMN limite_vistorias_cautelares integer;

ALTER TABLE assinaturas.planos
    ADD CONSTRAINT ck_planos_limite_vistorias_cautelares
        CHECK (limite_vistorias_cautelares IS NULL OR limite_vistorias_cautelares >= 0);

ALTER TABLE assinaturas.assinaturas
    ADD COLUMN limite_anuncios_contratado integer,
    ADD COLUMN limite_vistorias_cautelares_contratado integer;

ALTER TABLE assinaturas.assinaturas
    ADD CONSTRAINT ck_assinaturas_limite_anuncios_contratado
        CHECK (limite_anuncios_contratado IS NULL OR limite_anuncios_contratado >= 0),
    ADD CONSTRAINT ck_assinaturas_limite_vistorias_contratado
        CHECK (limite_vistorias_cautelares_contratado IS NULL OR limite_vistorias_cautelares_contratado >= 0);
