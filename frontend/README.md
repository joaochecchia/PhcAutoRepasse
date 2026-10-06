# PHC Auto — frontend

Interface independente em React, TypeScript e Vite, inspirada na organização das referências fornecidas. Identidade própria em vermelho, carvão e branco, com tema escuro acessível pela interface.

## Executar

```bash
cd frontend
npm install
npm run dev
```

Abra a URL exibida pelo Vite (normalmente `http://localhost:5173`). `npm run build` verifica TypeScript e gera `dist/`. `npm run preview` serve o build.

## Escopo da prévia

- Home, marcas com logotipos, catálogo, favoritos da visita e apresentação de planos. Login, cadastro PF/PJ, dados do anunciante, formulário de anúncio e detalhes do veículo são páginas dedicadas.
- Busca com barra lateral no desktop e modal de filtros no celular. Chips removíveis e filtros no fragmento da URL permitem navegar/voltar/compartilhar a seleção.
- Catálogo inteiramente ilustrativo. Filtros não calculam resultados, proximidade, preços ou permissões. O controle “Prévia” da busca permite inspecionar carregamento, vazio e erro sem simular chamadas ao backend.
- Fotos são ilustrativas; valores, versões e dados não constituem ofertas reais.
- Nenhum login, cadastro, anúncio, pagamento, assinatura ou contato é enviado ou persistido. Não existem preços ou limites de planos inventados.
- Tema e favoritos ficam em memória. Nenhum uso de localStorage, sessionStorage ou cookies legíveis por JavaScript.

## Páginas dedicadas

Os endereços mantêm a navegação por fragmento já usada pelo projeto, com suporte a acesso direto, recarregamento, Voltar e Avançar:

| Página                                               | Endereço                     |
| ---------------------------------------------------- | ---------------------------- |
| Login                                                | `/#entrar`                   |
| Cadastro PF/PJ                                       | `/#cadastro`                 |
| Conferência dos dados do anunciante                  | `/#anunciar/dados`           |
| Veículo, localização, fotos e conferência do anúncio | `/#anunciar/veiculo`         |
| Detalhes de um veículo demonstrativo                 | `/#anuncio/1` (IDs de 1 a 8) |

Os links dos cards, cabeçalho, rodapé e chamadas da home abrem essas páginas. Somente planos e filtros móveis continuam em diálogo. Uma rota ou um anúncio desconhecido mostra uma tela de não encontrado.

O cadastro apresenta nome/nome fantasia, e-mail, telefone, CPF e nascimento para PF ou CNPJ e razão social para PJ, endereço e senha. Os documentos legais vigentes não foram fornecidos: a prévia explica o aceite pendente e não coleta nem inventa consentimento. Na integração, o request exigirá também `aceitouTermos`, `aceiteTermosEm`, `versaoTermosUso` e `versaoPoliticaPrivacidade`; o IP vem do servidor. Não há escolha de papel administrativo na interface pública.

A etapa de dados do anunciante é uma prévia do preenchimento/conferência de perfil. Não há sessão nem consulta que determine quais dados de uma conta real estão faltando. Ela conserva o rascunho apenas na memória da visita para permitir copiar o endereço ao formulário do veículo. Sair/recarregar a aplicação perde esse rascunho. CPF/CNPJ, senha e endereço não são colocados na URL. Dados complementares de compra (RG, filiação, regime tributário etc.) não fazem parte do contrato de criação de anúncio e não são exigidos aqui.

O formulário de anúncio apresenta os campos comuns e os blocos técnicos dos seis tipos de veículo, incluindo motores de barcos. Permite revisar o endereço independente do anúncio, título, descrição, preço em reais ou sob consulta, troca e intenção de rascunho/publicação. A seleção de fotos, ordem e texto alternativo funciona apenas como prévia local; URLs temporárias são revogadas ao desmontar os componentes. Nada é enviado ou salvo. O estado desse formulário é descartado ao sair da página. Apenas presença/formato HTML são conferidos; documentos, idade, preços, placa, limites e autorização continuam sendo responsabilidade do backend.

Fontes consultadas: `domain/model/identidade`, `domain/model/catalogo`, `LoginController`, `UsuariosController`, `AnunciosController`, `FotosAnuncioController`, seus requests/responses atuais e a documentação de criação de usuário/anúncio. Alguns comentários antigos de domínio descrevem campos como opcionais, enquanto o request atual exige seu preenchimento; a interface segue o contrato HTTP atual.

**Detalhes por ID:** `AnunciosController` ainda não expõe um `GET /api/v1/anuncios/{id}` público. `AnuncioResponse` descreve detalhes técnicos, mas é resposta de mutações. O futuro carregamento da página de anúncio precisará de um contrato público próprio. A prévia usa os fixtures existentes, informa dados ausentes e não expõe documentos ou endereço privado. A listagem pública de fotos já existe em `GET /api/v1/anuncios/{id}/fotos`, mas também não é chamada nesta entrega.

## Contrato e futura integração

Os campos de `src/data/filters.ts` foram conferidos com `BuscarAnunciosRequest.java` e `backend/docs/busca-anuncios.md`: tipo, marca, localização (Brasil, cidade/UF, estado, dispositivo, endereço cadastrado), PF/PJ, perfil, preço, ano, câmbio, combustível, motorização, condição, direção, tração, IPVA, blindagem, portas, cilindrada, freios e carroceria.

Modelo, quilometragem e ordenação não são filtros do contrato atual. Não há seletor de raio: a decisão de ampliar a proximidade é do backend. Coordenadas do dispositivo e autorização de geolocalização só serão coletadas na integração. Paginação seguirá `pagina`, `tamanho` e `temProximaPagina`; a prévia exibe apenas oito fixtures, sem inventar páginas.

Os inputs de preço apresentam reais; ao conectar, o adaptador HTTP precisará serializar em centavos. Valores de campos livres/categóricos são exemplos de apresentação, não listas de regras aceitas pelo domínio. Não enviar o estado bruto do formulário como request.

`src/lib/api.ts` exporta uma instância Axios com `baseURL: '/api/v1'`, timeout e `withCredentials: true`, sem consumidores ou requisições. Preferir mesma origem por proxy na futura implantação.

**HttpOnly precisa ser implementado no servidor.** O login atual do backend retorna JWT no JSON; ele ainda não emite a sessão HttpOnly pedida. Na integração, adaptar o servidor (ou um BFF) para emitir `Set-Cookie` com `HttpOnly`, `Secure` em HTTPS e `SameSite` apropriado, além de proteção CSRF, logout e expiração. Não ler, fabricar ou persistir tokens no cliente. Essa etapa não foi implementada nem exige alteração do backend nesta entrega.

Validação de negócio, elegibilidade, publicação, preços, planos, autenticação, autorização e busca permanecem no servidor. Formulários desta prévia apenas demonstram navegação e validação básica de formato do navegador.

## Arquivos

- `src/App.tsx`: composição, navegação e estado de interface.
- `src/components/`: cards, filtros e diálogos nativos com foco e Escape.
- `src/pages/`: telas dedicadas de conta, anunciante, anúncio e detalhe; CSS isolado preserva o layout da home.
- `src/components/FormFields.tsx`: campos de apresentação reutilizados no cadastro e na conferência de perfil.
- `src/data/announcementFields.ts`: descritores dos campos técnicos dos requests, sem implementar validações de negócio.
- `src/lib/routes.ts`: endereços das páginas e títulos.
- `src/data/`: fixtures editoriais e descritores dos filtros.
- `src/styles.css`: tokens, temas e responsividade.
- `public/images/` e `public/brands/`: imagens locais. Fontes DM Sans e Manrope carregadas pelo Google Fonts, com fallback sans-serif.

## Origem dos recursos visuais

Fotografias: Unsplash, IDs `photo-1503376780353-7e6692767b70`, `photo-1555215695-3004980ad54e`, `photo-1618843479313-40f8afb4b4d8`, `photo-1606152421802-db97b9c7a11b`, `photo-1471479917193-f00955256257`, `photo-1621007947382-bb3c3994e3fb`, `photo-1533473359331-0135ef1b58bf`, `photo-1533106418989-88406c7cc8ca`.

Logotipos: `filippofilip95/car-logos-dataset` (GitHub, `logos/optimized`). Marcas pertencem aos respectivos titulares; uso como identificação de filtros, sem afirmar parceria comercial.
