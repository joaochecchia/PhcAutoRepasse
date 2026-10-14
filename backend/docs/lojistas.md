# Lojistas e fotos de perfil

O cadastro PJ já contém o nome da loja (`nome` / `nome_fantasia`). Não há cadastro duplicado de loja. O diretório lista apenas pessoas jurídicas ativas. A foto é opcional para PF e PJ.

| Método | Endpoint | Finalidade |
| --- | --- | --- |
| GET | `/api/v1/lojistas?cidade=Brasília&uf=DF&pagina=0&tamanho=12` | Busca lojas em 100 km e amplia para 200 km se não houver resultado |
| GET | `/api/v1/lojistas?latitude=-15.79&longitude=-47.88&pagina=0&tamanho=12` | Busca por proximidade da localização autorizada pelo navegador |
| GET | `/api/v1/perfis/{id}` | Nome público, tipo de pessoa, cidade/UF e URL da foto |
| GET | `/api/v1/anuncios?anuncianteId={id}&pagina=0&tamanho=52` | Anúncios publicados somente desse anunciante |
| PUT | `/api/v1/usuarios/{id}/foto` | Envia multipart `arquivo`; requer o próprio usuário ou papel DONO |
| GET | `/api/v1/perfis/{id}/foto` | Foto pública; 404 quando ausente |

A localização da busca usa a cidade/UF do endereço cadastrado do visitante autenticado, resolvida pelo catálogo de municípios, ou coordenadas fornecidas pelo navegador mediante permissão. A API procura primeiro em 100 km e amplia para 200 km somente quando não encontra nenhuma loja no raio inicial; `raioKmAplicado` informa a faixa usada. Para visitantes deslogados sem localização, a consulta retorna lojas PJ ativas de todo o Brasil, priorizando quantidade de anúncios publicados, presença de foto e nome. Nesse modo `raioKmAplicado` é nulo. Todas as modalidades limitam cada página a 12 resultados. O frontend carrega 12 lojas inicialmente e, ao clicar em **Carregar mais lojas**, consulta até duas páginas seguintes de 12, totalizando até 24 por clique. O total exibido representa as lojas da consulta, e não apenas as já carregadas. O estoque exibe anúncios independentemente da cidade do carro, que pode ser diferente da loja. Resultados e perfis usam a base de leitura e têm consistência eventual. O arquivo da foto consulta sua referência no write para evitar acesso à imagem anterior após substituição.

V31 adiciona `identidade.usuarios.foto_perfil_chave`, índice trigram para nome fantasia, índice de localização e índice de anúncios publicados por anunciante. Apenas o caminho relativo é salvo, seguindo `usuarios/{id}/perfil/{uuid}.jpg` dentro da mesma raiz de uploads dos veículos. A projeção da coluna ocorre por callbacks JPA/Modulith.

A infraestrutura valida JPEG/PNG, tamanho máximo de 5 MB e resolução até 16 megapixels antes da decodificação. Reencoda em JPEG de até 800 px, descartando metadados. O core contém contratos e casos de uso puros. O armazenamento acontece antes da transação curta da referência; falhas de banco removem o arquivo recém-criado, e o arquivo anterior só é removido após commit bem-sucedido.

No frontend, `#lojistas` mantém visíveis os filtros padrão de nome, cidade e estado. Quando há sessão, cidade e estado são preenchidos com o endereço cadastrado e a busca regional inicia automaticamente. O visitante pode escolher outra cidade/UF; o nome restringe os resultados dentro da região. Também há opção de localização atual do dispositivo mediante permissão. Sem sessão, o visitante pode informar cidade e UF ou usar o dispositivo; a API não aceita busca sem região. `#loja/{id}` mostra os anúncios da PJ. O cadastro envia a foto após registro e autenticação. Caso o upload falhe, não se repete o registro: é possível tentar a foto novamente ou continuar sem ela. A página de perfil permite trocar a foto posteriormente. Todos os anúncios mostram foto ou avatar genérico, e os perfis PJ possuem link para o estoque da loja.
