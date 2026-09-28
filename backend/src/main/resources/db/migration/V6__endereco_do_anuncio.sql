-- A localização pertence à oferta, não ao tipo específico de veículo.
CREATE TABLE catalogo.enderecos_anuncio (
    id uuid NOT NULL PRIMARY KEY,
    cep varchar(8),
    cidade varchar(120) NOT NULL,
    bairro varchar(120),
    rua varchar(200),
    numero varchar(20),
    complemento varchar(200),
    uf char(2) NOT NULL,
    lock_version bigint NOT NULL DEFAULT 0,
    CONSTRAINT ck_endereco_anuncio_cep CHECK (cep IS NULL OR cep ~ '^[0-9]{8}$'),
    CONSTRAINT ck_endereco_anuncio_uf CHECK (uf ~ '^[A-Z]{2}$')
);

-- IDs iguais preservam de forma determinística a localização resumida de anúncios legados.
INSERT INTO catalogo.enderecos_anuncio (id, cidade, uf)
SELECT id, cidade, upper(uf)
FROM catalogo.anuncios;

ALTER TABLE catalogo.anuncios ADD COLUMN endereco_id uuid;
UPDATE catalogo.anuncios SET endereco_id = id;
ALTER TABLE catalogo.anuncios ALTER COLUMN endereco_id SET NOT NULL;
ALTER TABLE catalogo.anuncios ADD CONSTRAINT uq_anuncios_endereco UNIQUE (endereco_id);
ALTER TABLE catalogo.anuncios DROP COLUMN cidade;
ALTER TABLE catalogo.anuncios DROP COLUMN uf;
