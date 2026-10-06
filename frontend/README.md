# PHC Auto — frontend

Frontend React, TypeScript e Vite integrado ao backend PHC Auto. A interface usa identidade própria em vermelho, carvão e branco, possui temas claro/escuro e mantém regras de negócio no servidor.

## Executar

Com o backend disponível em `http://127.0.0.1:8080`:

```bash
cd frontend
npm install
npm run dev
```

Abra `http://localhost:5173`. No desenvolvimento, o Vite encaminha `/api` ao backend. `npm run build` verifica TypeScript e gera `dist/`.

## Integração

- O Axios usa `baseURL: /api/v1` e `withCredentials: true`.
- Autenticação usa o cookie `PHC_AUTH` emitido pelo servidor com `HttpOnly`; nenhum token é guardado em `localStorage`, `sessionStorage` ou cookie acessível por JavaScript.
- Cadastro consulta as versões legais vigentes, registra PF/PJ e inicia a sessão. Login, consulta de sessão e logout usam endpoints reais.
- Quem seleciona **Anunciar** sem sessão é levado ao cadastro. Depois de autenticado, confere/completa o perfil e só então acessa o formulário de anúncio.
- O endereço do cadastro pode ser preenchido pelo ViaCEP; o número permanece manual porque não faz parte da resposta de CEP.
- Criação de anúncio e upload de até oito fotos usam os contratos reais do backend.
- A home solicita 20 anúncios. A página de busca solicita 52 por página, envia filtros ao servidor — incluindo marca, modelo e localização — e usa a paginação informada pela API.
- Valores digitados em reais são convertidos para centavos apenas no adaptador HTTP. Validação, autorização, elegibilidade e publicação continuam no backend.

## Páginas

| Página | Endereço |
| --- | --- |
| Login | `/#entrar` |
| Cadastro PF/PJ | `/#cadastro` |
| Conferência do anunciante | `/#anunciar/dados` |
| Criação do anúncio | `/#anunciar/veiculo` |
| Busca | `/#buscar` |
| Detalhes | `/#anuncio/{id}` |

Os filtros correspondem a `BuscarAnunciosRequest`. Coordenadas só são solicitadas quando o usuário escolhe localização pelo dispositivo. Tema, favoritos e rascunhos transitórios permanecem apenas na memória da página.

As imagens locais do catálogo são usadas como ilustração quando a resposta resumida da busca não contém foto de capa. A página de detalhes usa os dados públicos já recebidos pela busca; o backend ainda não possui um endpoint público de detalhes por ID.

## Estrutura principal

- `src/App.tsx`: rotas, sessão e composição das páginas.
- `src/lib/backend.ts`: adaptação dos contratos HTTP.
- `src/lib/api.ts`: cliente Axios.
- `src/components/Filters.tsx`: filtros da busca.
- `src/pages/`: login, cadastro, perfil, anúncio e detalhes.
