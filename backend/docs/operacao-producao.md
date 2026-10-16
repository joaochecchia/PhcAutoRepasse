# Proteção contra abuso e observabilidade

## Estado de preparação para produção

Esta é a checklist de referência para o primeiro deploy. Um item marcado como concluído significa que existe implementação no repositório; ainda assim, configurações próprias do ambiente de produção devem ser aplicadas e verificadas no deploy.

### Implementado no projeto

- [x] Imagem Docker em múltiplas etapas e execução com usuário sem privilégios de root.
- [x] PostgreSQL write, projeção read, Redis, volumes e healthchecks no Compose.
- [x] Flyway como único mecanismo de evolução do esquema e validação do Hibernate.
- [x] Senhas com BCrypt, política mínima de senha, JWT curto e refresh token opaco com rotação, revogação e detecção de reutilização.
- [x] Cookies de autenticação `HttpOnly`, `SameSite=Lax` e suporte a `Secure`.
- [x] Proteção CSRF para operações de navegador autenticadas por cookie, incluindo uploads multipart.
- [x] Autorização por proprietário e papéis para usuários e anúncios.
- [x] CORS com lista explícita de origens, sem wildcard e com credenciais controladas.
- [x] Rate limiting distribuído para login, registro, refresh, senha, buscas, fotos e contato por WhatsApp.
- [x] Limites de tamanho e quantidade para fotos de anúncios.
- [x] Actuator, health de PostgreSQL write/read e Redis e métricas Prometheus.
- [x] Métricas e alertas definidos para indisponibilidade, recursos, HTTP 5xx e atraso da projeção Spring Modulith.
- [x] Logs locais persistentes e rotação dos arquivos da aplicação e dos contêineres.
- [x] Integração opcional com Sentry, sem envio padrão de PII.
- [x] Testes automatizados para regras centrais, segurança, persistência, rollback e arquitetura; integrações reais possuem flags próprias.

### Bloqueadores do primeiro deploy público

- [ ] **Infraestrutura pública:** contratar/preparar a VPS, configurar DNS, proxy reverso e HTTPS com renovação automática do certificado.
- [ ] **Isolamento de rede:** publicar somente `80/443`; manter PostgreSQL, Redis, Actuator, Prometheus, Alertmanager e Grafana inacessíveis pela internet. Bloquear acesso direto à porta 8080 e aceitar tráfego apenas do proxy.
- [ ] **Configuração segura:** usar `PRODUCTION_MODE=true`, `AUTH_COOKIE_SECURE=true`, origem CORS definitiva e `FORWARD_HEADERS_STRATEGY=NATIVE` somente atrás do proxy confiável.
- [ ] **Segredos:** gerar `JWT_SECRET` exclusivo e senhas fortes para bancos, projeção, Redis quando externo e Grafana; armazená-los fora do Git e definir procedimento de rotação. Remover todos os defaults de desenvolvimento no ambiente de produção.
- [ ] **Privilégios do banco:** separar credencial de migration da credencial da aplicação e limitar cada usuário ao mínimo necessário. Confirmar que os repositories read continuam usando somente `SELECT`.
- [ ] **Backup externo:** automatizar backup diário do PostgreSQL write e das fotos em armazenamento fora da VPS, com criptografia, retenção definida e monitoramento de falhas. O banco read não é backup.
- [ ] **Teste de restauração:** restaurar banco e fotos em ambiente isolado, documentar o procedimento e definir RPO/RTO antes da abertura ao público.
- [ ] **Armazenamento de fotos:** montar diretório persistente fora do código/imagem, incluí-lo no backup e validar permissões do usuário não-root. Para mais de uma instância, migrar para armazenamento compartilhado/objeto antes de escalar horizontalmente.
- [ ] **Monitoramento operacional:** iniciar `compose.monitoring.yaml`, configurar senha definitiva do Grafana e conectar um receiver real do Alertmanager, como e-mail, PagerDuty ou webhook.
- [ ] **Rastreamento e disponibilidade externa:** configurar `SENTRY_DSN`, ambiente/release e uma verificação externa do domínio público; testar pelo menos um alerta sem usar dados pessoais reais.
- [ ] **Centralização de logs:** enviar os logs rotacionados para um serviço externo ou agente de coleta com retenção e controle de acesso. Logs apenas na VPS são perdidos junto com ela.
- [ ] **Swagger em produção:** definir `SWAGGER_ENABLED=false` ou protegê-lo por rede/autenticação; não deixá-lo aberto por conveniência.
- [ ] **Pipeline e staging:** criar um ambiente de homologação equivalente, executar build, testes completos, migrations e smoke tests antes do deploy. O pipeline deve impedir publicação quando algum check obrigatório falhar.
- [ ] **Teste de carga e capacidade:** medir criação/login, vitrine, filtros, lojistas, fotos e fila da projeção com volume semelhante ao esperado; ajustar pools, workers, memória e limites com base nos resultados.
- [ ] **Teste de segurança:** executar análise de dependências, SAST e DAST, revisar headers do proxy, permissões de arquivos, enumeração de contas e fluxos de upload. Corrigir achados críticos/altos antes da abertura.
- [ ] **Plano de incidente:** documentar responsáveis, contatos, rollback de versão, restauração, rotação emergencial de segredos e resposta a vazamento de dados.
- [ ] **LGPD operacional:** publicar Termos e Política reais e versionados, validar a base legal com responsável jurídico e definir retenção, exclusão/correção/exportação de dados, atendimento ao titular e resposta a incidentes. O registro técnico do aceite já existe, mas não substitui esses processos.
- [ ] **Dados de produção:** remover ou isolar contas, anúncios, fotos e credenciais fictícias. Nunca executar seeds ou testes destrutivos no banco de produção.
- [ ] **Verificação final do domínio:** testar registro, login, refresh, logout, CSRF, autorização por proprietário, anúncio, upload, busca, WhatsApp, rollback, backup e alertas usando HTTPS no endereço definitivo.

### Decisões de produto antes do lançamento

Estes itens não bloqueiam um MVP dedicado somente a cadastro, anúncios, busca e contato, desde que as funcionalidades ausentes não sejam anunciadas nem expostas na interface:

- [ ] Definir se Google/Facebook entrarão no primeiro lançamento; o login local funciona, mas OAuth continua pendente.
- [ ] Implementar verificação de e-mail e recuperação de senha por token de uso único se esses fluxos forem exigidos no lançamento.
- [ ] Definir planos, limites de anúncios, assinatura e benefícios de vistoria antes de ativar cobrança ou restrições comerciais.
- [ ] Implementar compra, pagamento, conciliação e estorno antes de permitir concluir vendas dentro da plataforma.
- [ ] Definir moderação, denúncia, bloqueio e tratamento de anúncios fraudulentos.
- [ ] Definir atendimento ao cliente, suporte ao anunciante e regras para suspensão/desativação de contas.

O sistema ainda não deve ser considerado pronto para produção enquanto houver itens abertos em **Bloqueadores do primeiro deploy público**. A conclusão deve ser baseada em evidência do ambiente implantado, principalmente restauração de backup, alertas recebidos e smoke tests pelo domínio HTTPS.

## Rate limiting

O backend aplica limites distribuídos com Redis. A contagem usa um script Lua atômico, portanto continua correta com várias instâncias da aplicação. As chaves armazenam somente SHA-256 do identificador e expiram ao fim da janela.

| Operação | Limite local padrão | Identificador |
|---|---:|---|
| Login | 10/minuto | IP |
| Registro | 5/hora | IP |
| Renovação de token | 20/minuto | IP |
| Buscas | 120/minuto | usuário autenticado ou IP |
| Fotos de anúncio | 30/hora | usuário |
| Foto de perfil | 10/hora | usuário |
| Contato por WhatsApp | 10/hora | usuário |
| Tentativas de senha | 5/15 minutos | e-mail normalizado |

Ao exceder o limite, a API retorna `429 Too Many Requests` e `Retry-After`. Todos os valores podem ser alterados pelas variáveis `RATE_LIMIT_*` declaradas no Compose. Se o Redis ficar indisponível, cada instância ativa um fallback local de janela fixa, limitado por `RATE_LIMIT_FALLBACK_MAX_KEYS` para evitar crescimento irrestrito de memória. Esse fallback mantém proteção básica, mas não soma contadores entre instâncias; a aplicação registra log e métricas críticas. O proxy de borda deve manter um segundo limite por IP para absorver ataques antes de chegarem à VPS.

Em produção, mantenha o backend acessível apenas pelo proxy reverso/Cloudflare. Configure `FORWARD_HEADERS_STRATEGY=NATIVE` somente quando o proxy for confiável e bloquear acesso direto à porta do backend; assim o IP usado no limite não pode ser forjado por cabeçalhos enviados diretamente.

## Cookies e CSRF

A autenticação por cookie usa double-submit cookie do Spring Security. O cookie `XSRF-TOKEN`, legível pelo frontend, deve ser devolvido no cabeçalho `X-XSRF-TOKEN` em `POST`, `PUT`, `PATCH` e `DELETE`. O cookie JWT `PHC_AUTH` e o refresh token continuam `HttpOnly`; o JavaScript não tem acesso a eles. O frontend Axios inicializa e envia o token CSRF automaticamente, inclusive em uploads multipart.

Requisições feitas exclusivamente com `Authorization: Bearer` não exigem CSRF, pois o navegador não anexa esse segredo automaticamente. Requisições de navegador sem o token correto recebem HTTP 403. Swagger UI está configurado para transportar o padrão `XSRF-TOKEN`/`X-XSRF-TOKEN`.

No deploy, configure obrigatoriamente:

```dotenv
PRODUCTION_MODE=true
AUTH_COOKIE_SECURE=true
```

Com `PRODUCTION_MODE=true`, a aplicação recusa a inicialização se os cookies seguros estiverem desativados. O domínio público deve usar HTTPS e frontend e backend devem permanecer no mesmo site para o `SameSite=Lax` funcionar como planejado.

## Saúde, métricas e erros

O Actuator utiliza a porta interna `9091`:

- `/actuator/health/liveness`: processo ativo;
- `/actuator/health/readiness`: disponibilidade das dependências;
- `/actuator/health`: PostgreSQL write, PostgreSQL read e Redis;
- `/actuator/prometheus`: métricas para o Prometheus.

A porta de gestão é apenas `expose` no Compose e não é publicada no host. Não a exponha diretamente na internet. O healthcheck do contêiner usa a sonda de liveness. A indisponibilidade das dependências aparece na saúde agregada, sem expor credenciais ou detalhes ao cliente.

As métricas `phcauto_projection_pending` e `phcauto_projection_oldest_seconds` medem a fila persistente do Spring Modulith. O alerta dispara quando o evento mais antigo passa de dois minutos. Falhas de leitura da fila e falhas do Redis usado no rate limiting possuem alertas próprios.

Os logs da aplicação são gravados em `logs/backend/backend.log`, com arquivos de até 20 MB, retenção de 14 arquivos e limite total de 1 GB. O driver Docker também rotaciona stdout/stderr em três arquivos de 10 MB. Faça coleta externa desses arquivos antes de produção se precisar de retenção centralizada, por exemplo com Loki, CloudWatch ou o agente da hospedagem.

O Sentry é opcional. Configure `SENTRY_DSN`, `SENTRY_ENVIRONMENT=production` e `SENTRY_RELEASE` para enviar exceções não tratadas. O SDK está configurado para não enviar PII por padrão.

## Subir monitoramento

Defina uma senha forte no `.env`:

```dotenv
GRAFANA_ADMIN_PASSWORD=troque-por-uma-senha-forte
```

Suba a aplicação e o monitoramento:

```bash
docker compose -f compose.yaml -f compose.monitoring.yaml up -d --build
```

Prometheus, Alertmanager e Grafana ficam vinculados ao localhost da VPS nas portas 9090, 9093 e 3000. Acesse-os por túnel SSH ou publique o Grafana por proxy com autenticação. O Prometheus já carrega alertas de indisponibilidade, atraso da projeção, CPU, heap, disco, respostas 5xx e falha do rate limiter. Antes da produção, configure um receiver real em `docker/monitoring/alertmanager.yml` para enviar as notificações por e-mail, PagerDuty, Opsgenie ou webhook; sem esse destino, os alertas ficam visíveis somente no Alertmanager/Grafana.

## Operação FIPE

Implementado: catálogo histórico, importação incremental administrativa, consulta complementar com cache/quota Redis e snapshot no veículo, sem chamadas externas na vitrine. Consulte [FIPE](fipe.md).

Antes de ativar: definir fornecedor autorizado para o arquivo mensal, validar cobertura/qualidade da base e configurar token Parallelum e orçamento compatível com a conta. Isso permanece pendente, assim como teste de carga com o catálogo real.

No deploy: persistir e dar permissão ao diretório `data/fipe`, compartilhá-lo entre instâncias quando aplicável, incluir arquivos pendentes no backup, definir retenção e monitorar uso de disco, erros de importação e consumo externo. Configurar alertas efetivos para as métricas FIPE; a presença das métricas não significa que alertas externos já estejam ativos.


### FIPE local (15/10/2026)

Implementados catálogo persistente, paginação, cache com fallback, fila de sincronização controlada e formulário dependente. Sincronização externa **desativada por decisão do usuário**, até autorização de armazenamento comercial. Antes de ativar: contrato/permissão, retenção e carga licenciada; depois validar carga HTTP sustentada e múltiplas instâncias. `scripts/load/fipe.js` contém cenário reproduzível; latências de testes locais não são garantia de produção. Consulte `fipe-catalogo-local.md`.
