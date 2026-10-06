# Localização municipal

O módulo `localizacao` mantém um catálogo local de 5.571 municípios brasileiros. A migration V19 cria `localizacao.municipios`, carrega código IBGE, nome, nome normalizado, UF e coordenadas municipais, e vincula endereços de usuários e anúncios por `municipio_codigo_ibge`.

Os códigos foram validados contra a API oficial de localidades do IBGE. As coordenadas da carga são dados de sede municipal distribuídos pelo projeto aberto `municipios-brasileiros`. A fonte e a data da carga ficam registradas em cada linha.

Não há integração com Google, Nominatim ou outro geocodificador. O módulo possui core Java puro, gateway de consulta e adaptador JDBC sobre o banco read. Redis pode armazenar resultados por nome/UF ou código IBGE, mas PostgreSQL permanece como fonte de verdade.

A busca pública possui cinco modos:

- `DISPOSITIVO`: recebe `latitude` e `longitude` do navegador ou aplicativo mediante autorização. As coordenadas são usadas apenas na requisição e não são persistidas.
- `ENDERECO_CADASTRADO`: usa o município relacionado ao endereço do usuário autenticado e suas coordenadas centrais.
- `CIDADE`: resolve cidade e UF no catálogo e utiliza as coordenadas municipais.
- `UF`: filtra diretamente os anúncios daquele estado, sem raio.
- `BRASIL`: não aplica filtro geográfico.

Sem `modoLocalizacao`, o backend infere: coordenadas significam dispositivo, cidade significa cidade, apenas UF significa estado e ausência de localização significa Brasil. Dispositivo, cidade e endereço cadastrado usam 100 km, ampliando para 200 km somente quando todos os demais filtros não retornam resultados em 100 km.

Endereços continuam armazenando os dados postais necessários ao cadastro, mas não armazenam latitude e longitude. Todos os anúncios e usuários de um município compartilham a mesma referência municipal. Essa aproximação foi aceita para raios amplos de 100 e 200 km.

A migration V20 do write adiciona as chaves estrangeiras e impede novos endereços sem município, preservando registros legados eventualmente não reconhecidos durante o backfill.

A migration V21 associa regiões administrativas do Distrito Federal ao município de Brasília, mantendo o nome local no endereço para exibição.

## Endpoint temporário de teste

`GET /api/v1/localizacao/municipios/coordenadas?cidade={cidade}&uf={UF}` consulta o catálogo local e retorna código IBGE, nome canônico, UF, latitude e longitude da sede municipal. Cidade e UF são obrigatórias para eliminar ambiguidade entre municípios homônimos. O endpoint é público, aparece no Swagger com a tag `Localização (teste)` e não chama serviços externos.

Esse endpoint é diagnóstico e deve ser removido após os testes de integração do frontend. O controller fica no adaptador web de infraestrutura e reutiliza a fachada existente, sem criar um novo caso de uso para esse fluxo descartável.
