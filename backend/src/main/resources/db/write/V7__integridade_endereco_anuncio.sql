-- A projeção read não recebe FK para aceitar eventos fora de ordem.
ALTER TABLE catalogo.anuncios ADD CONSTRAINT fk_anuncios_endereco_id
    FOREIGN KEY (endereco_id) REFERENCES catalogo.enderecos_anuncio(id);
