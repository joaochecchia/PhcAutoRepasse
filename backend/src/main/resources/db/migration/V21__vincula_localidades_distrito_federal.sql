-- O Distrito Federal possui um único município no cadastro do IBGE. Regiões administrativas
-- como Taguatinga permanecem no campo cidade para exibição, mas compartilham Brasília (5300108)
-- como referência municipal para a busca por raio.
UPDATE identidade.enderecos_usuario
SET municipio_codigo_ibge = 5300108
WHERE municipio_codigo_ibge IS NULL
  AND upper(trim(uf)) = 'DF';

UPDATE catalogo.enderecos_anuncio
SET municipio_codigo_ibge = 5300108
WHERE municipio_codigo_ibge IS NULL
  AND upper(trim(uf)) = 'DF';
