ALTER TABLE catalogo.enderecos_anuncio
    ADD CONSTRAINT ck_enderecos_anuncio_novo_com_coordenadas
        CHECK (latitude IS NOT NULL AND longitude IS NOT NULL) NOT VALID;
