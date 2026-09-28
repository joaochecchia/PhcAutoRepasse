-- A projeção aceita atualizações fora de ordem e não impõe unicidade de negócio.
ALTER TABLE catalogo.anuncios DROP CONSTRAINT uq_anuncios_endereco;
CREATE INDEX idx_anuncios_endereco_id ON catalogo.anuncios (endereco_id);
