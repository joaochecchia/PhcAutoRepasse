UPDATE catalogo.fotos AS foto
SET chave_arquivo = 'usuarios/' || anuncio.anunciante_id || '/veiculos/' || anuncio.veiculo_id || '/'
    || regexp_replace(foto.chave_arquivo, '^.*/', '')
FROM catalogo.anuncios AS anuncio
WHERE anuncio.id = foto.anuncio_id
  AND foto.chave_arquivo NOT LIKE 'usuarios/%/veiculos/%/%';

ALTER TABLE catalogo.fotos
    ADD CONSTRAINT ck_fotos_posicao_limite
    CHECK (posicao BETWEEN 0 AND 7);
