---
name: phc-frontend
description: Convenções do frontend do PHC Auto (React 19 + Vite + TypeScript, axios via backend, CSS com tokens, marketplace de veículos em pt-BR). Use ao criar ou alterar telas, componentes, formulários ou chamadas de API em frontend/.
---

# Frontend — PHC Auto

Diretrizes de produto e visual completas: `frontend/AGENTS.md` (leia antes de mudanças de UI). Para direção estética, a skill `frontend-design` complementa esta.

## Stack real
React 19, TypeScript ~5.7, Vite 6, axios, lucide-react. **Sem** Tailwind, roteador, TanStack Query ou biblioteca de formulários. Não adicione dependências sem o usuário pedir.

## Mapa do código
- `src/lib/api.ts`: instância axios (`baseURL /api/v1`, cookie HttpOnly, CSRF `X-XSRF-TOKEN`). O frontend **nunca** lê nem guarda token.
- `src/lib/backend.ts`: funções de chamada ao backend. Toda nova chamada entra aqui, tipada.
- `src/lib/contracts.ts`: tipos dos contratos HTTP.
- `src/lib/routes.ts`: navegação por hash (`#/anunciar/veiculo`). Para tela nova, adicione em `Page` e `paths`.
- `src/pages/*Page.tsx` + `pages.css`; `src/components/` (ex.: `FormFields`, `Modal`, `Filters`, `VehicleCard`); `src/data/` (catálogos estáticos).
- `src/styles.css`: tokens em `:root` (`--bg`, `--surface`, `--text`, `--muted`, `--border`, `--red`…) e tema escuro em `:root[data-theme="dark"]`. Use os tokens e não fixe cores.

## Regras
- **Regra de negócio só no backend.** O frontend exibe, coleta e valida apenas formato/UX. Cálculos, elegibilidade, preços e FIPE vêm da API.
- Nunca chamar serviços externos com segredo (ex.: Parallelum) direto do navegador; tudo passa pelo backend. APIs públicas sem token já usadas (ViaCEP) podem continuar.
- Formulários controlados. Selects dependentes limpam os campos seguintes quando um campo anterior muda.
- Todo fluxo assíncrono tem estados de loading, vazio, erro com retry, desabilitado e sucesso. Não engolir erro no console.
- Texto curto em pt-BR, específico do domínio de veículos, sem frases genéricas.
- Mobile-first, sem scroll horizontal nem overflow; ícones do lucide-react; HTML semântico com labels.
- Reutilize os componentes existentes antes de criar novos e não crie abstração sem duplicação real.

## Validação
```bash
cd frontend
npm run typecheck
npm run build
npm run dev   # conferir a tela em desktop e mobile quando houver mudança visual
```
Diga o que foi verificado visualmente e o que não foi. Remova imports não usados e logs de debug.
