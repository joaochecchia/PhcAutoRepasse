# Eventos para o futuro CRUD de usuário

Esta preparação reutiliza o registro JDBC persistente do Spring Modulith 2.1.1 e o executor existente. Não cria um broker RabbitMQ/Kafka nem filas Redis. A criação local foi implementada posteriormente em `usuarios`; veja [criacao-usuario.md](criacao-usuario.md). Atualização/exclusão continuam provisórias. As instruções de UsuarioAlterado abaixo documentam a preparação original; o cadastro real publica UsuarioCriado.

## Fluxo de cadastro

```text
Controller -> caso de uso -> gateways
  transação write:
    1. validar cadastro e autenticação
    2. salvar usuarios
    3. salvar usuarios_pf OU usuarios_pj, conforme TipoPessoa
    4. salvar enderecos_usuario com o mesmo usuario_id
    5. vincular identidade externa verificada, quando aplicável
    6. publicar UsuarioAlterado(CADASTRADO) pela porta do core
  commit
    -> RowChanged de cada linha -> ReadModelListener -> banco read
    -> futuros listeners de UsuarioAlterado -> efeitos posteriores
```

PF e PJ são alternativas; não criar os dois perfis. Perfil e endereço necessários ao cadastro completo pertencem à mesma transação do usuário. Não delegar essas gravações essenciais a listeners assíncronos: uma falha deve reverter todo o cadastro. A futura implementação precisa impor essa regra; os novos contratos de evento não a executam.

A atualização deve carregar os registros existentes, preservar o controle de versão e publicar ATUALIZADO ao final. EXCLUIDO representa uma exclusão efetivamente concluída; regras de exclusão, dependências e desativação ainda serão definidas no CRUD. Exclusões por cascata SQL não geram callbacks JPA dos filhos: remover entidades dependentes pelo fluxo JPA ou tratar explicitamente suas projeções. Consultas não publicam esses eventos.

## Contratos e publicação

- `domain.event.identidade.UsuarioAlterado`: record Java puro com eventId, usuarioId, tipoPessoa, operacao e ocorridoEm; sem senha, hash, tokens, CPF/CNPJ ou endereço.
- `domain.gateway.identidade.PublicarEventoUsuarioGateway`: porta interna para publicação.
- `SpringPublicarEventoUsuarioGateway`: adaptador Spring com propagação MANDATORY no writeTransactionManager; rejeita ausência de transação e transação somente leitura.

O futuro caso de uso publica depois de salvar todo o cadastro, ainda antes do commit, pela porta injetada. Um listener transacional só recebe após commit. Cada fato novo deve ter eventId próprio; reentregas preservam esse identificador.

## Consumidores futuros

Não foram criados listeners vazios, envio de email ou criação assíncrona de perfis. Ao implementar um efeito posterior real, registrar um método `@ApplicationModuleListener(id = "identificador-estavel-v1")` que receba UsuarioAlterado e trate apenas as operações relevantes.

O registro Modulith persiste uma entrega por listener transacional existente. **Sem consumidor registrado, publicar UsuarioAlterado não cria uma mensagem durável para um listener adicionado no futuro.** Por isso os consumidores devem existir antes de habilitar a publicação no CRUD real. Um listener que não trata determinada operação deve ignorá-la conscientemente; filtros e identificadores precisam permanecer compatíveis com publicações pendentes.

A entrega pode se repetir, não garante ordem global e não equivale a processamento exatamente uma vez. Os consumidores devem ser idempotentes, usar eventId para deduplicação quando necessário e não considerar ocorridoEm uma versão de registro. RowChanged continua sendo o único responsável pela projeção read, consultando o estado atual do write; o evento de negócio não duplica a gravação de projeção.

## Execução e recuperação

| Variável | Padrão | Uso |
| --- | --- | --- |
| MODULITH_EVENT_WORKERS | 2 | Número de trabalhadores do executor compartilhado |
| MODULITH_EVENT_QUEUE_CAPACITY | 1000 | Capacidade da fila em memória |
| PROJECTION_RETRY_DELAY | 30s | Intervalo compartilhado da recuperação de RowChanged e UsuarioAlterado |

As duas novas variáveis são repassadas pelo Compose. Valores de workers e capacidade devem ser positivos. Ao saturar a fila, CallerRunsPolicy aplica pressão ao produtor executando a tarefa na thread chamadora. A fila em memória não é a garantia de durabilidade: essa função pertence ao registro JDBC de entregas transacionais. O executor aguarda até 15 segundos no encerramento; pendências persistidas continuam sujeitas à recuperação.

O registro JDBC, as migrations existentes, a republicação no restart e o monitor de publicações paradas foram preservados. PublicationRecovery agora inclui UsuarioAlterado na lista de eventos recuperáveis. Não é necessária migration nova para cada classe de evento.

## Validação

`./mvnw -B -ntp -Dtest=UsuarioEventosTests,PublicationRecoveryTests,ModulithStructureTests test`

Os testes verificam a porta Spring com proxy transacional, entrega após commit, ausência de entrega no rollback, rejeição sem transação e em transação somente leitura, seleção para recuperação e arquitetura atual. O datasource nesses novos testes é simulado; eles não comprovam durabilidade, restart ou gravação PF/PJ/endereço em PostgreSQL. A integração real do futuro cadastro deverá testar esses cenários em bancos isolados.

Referência: https://docs.spring.io/spring-modulith/reference/events.html
