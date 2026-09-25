# Contratos de domínio

Fonte: [modelo-inicial.dbml](modelo-inicial.dbml), fornecido pelo usuário via dbdiagram.io.

O domínio usa somente Java 17, sem Spring, JPA, Lombok, Jackson ou Bean Validation. Os modelos são classes abstratas com getters abstratos; não contêm estado, construtores de dados, persistência ou validações executáveis. As implementações concretas serão criadas posteriormente, conforme solicitado. Os contratos específicos iniciais usam interfaces com uma operação `executar`. O CRUD agora possui interfaces genéricas segregadas por operação e bases abstratas por modelo, descritas abaixo.

## Organização

- `domain/model/identidade`: usuário, perfis PF/PJ e enums.
- `domain/model/assinaturas`: plano, assinatura e status.
- `domain/model/catalogo`: veículo, detalhes dos seis tipos, motores, características, anúncios e fotos.
- `domain/model/vendas`: compra, pagamento, evento de gateway e enums.
- `domain/usecases/<area>`: contratos de consulta, gravação e listagens específicas; vendas também contém os contratos de recebimento e conclusão de eventos.

Os campos das 24 tabelas de negócio e os dez enums do DBML estão representados. As relações usam identificadores, sem referências a entidades JPA ou carregamento lazy. Perfis e detalhes de veículos preservam os identificadores compartilhados do modelo relacional; não herdam de Usuario ou Veiculo, pois representam os detalhes vinculados, não a entidade completa.

## Tipos e contratos

- UUID para identificadores UUID; long para o identificador interno de evento.
- Instant para timestamptz e LocalDate para date.
- Long/long para valores monetários em centavos e BigDecimal para decimais.
- Integer e Boolean preservam ausência em campos opcionais; zero e false não substituem ausência.
- Campos obrigatórios estão documentados, mas a classe abstrata não impede uma implementação de retornar null.
- Consultas individuais usam Optional; listagens retornam listas vazias quando não houver resultados.
- Salvar é um contrato básico de gravação, não uma aprovação automática de publicação, assinatura ou pagamento. Implementações devem validar autorização, consistência e transações antes de persistir.
- Modelos de domínio não são DTOs HTTP. Em particular, `Usuario.getSenhaHash()` não deve ser exposto em respostas públicas.

## Restrições para as futuras implementações

As seguintes restrições vêm do modelo. A migration inicial implementa FKs, unicidades e preço condicionado ao tipo; as regras de composição e os fluxos de negócio ainda precisam de casos de uso:

- Cada usuário tem exatamente um perfil PF ou PJ, coerente com seu tipo. A gravação do usuário e do perfil precisa ser coordenada em uma transação.
- Cada veículo tem detalhes correspondentes ao seu tipo. A gravação dos dados comuns e detalhes precisa ser coordenada em uma transação.
- Anúncio FIXO exige preço; SOB_CONSULTA exige preço ausente. Preservar o campo de versão para futura concorrência otimista.
- Preservar as unicidades documentadas no DBML: email, CPF, CNPJ, nome de plano, posição de motor por barco, característica por tipo/nome, vínculo veículo/característica, chave de arquivo, posição de foto por anúncio, referência de pagamento por provedor e evento externo por provedor.
- Respeitar defaults, limites de tamanho, referências e nulabilidade do DBML nas implementações e migrations.
- Registrar um evento de gateway deve ser atômico e detectar duplicidade. Registro de recebimento e processamento são etapas distintas: um evento recebido mas ainda não processado precisa permitir retomada. Marcar como processado deve ser coordenado com seus efeitos, evitando pagamentos duplicados ou eventos perdidos.

Não foram definidas regras de transição dos status, preços/limites dos planos, exigência de plano para publicar ou conclusão da compra no site. Nenhuma dessas regras foi inventada nos contratos.

## Limites desta entrega

A infraestrutura agora possui entidades JPA, repositories de leitura/escrita e a migration SQL inicial; consulte [persistence.md](persistence.md). Os casos de uso continuam abstratos, sem gateways externos, autenticação, cobrança ou controllers de negócio. Os enums expressam os valores permitidos no modelo; não constituem máquinas de estado. A implementação de regras deverá continuar independente dos frameworks, com adaptadores técnicos na infraestrutura.

## CRUD e bases abstratas

`domain/usecases/crud` contém cinco interfaces independentes:

| Interface | Operação |
| --- | --- |
| `CriarUseCase<T>` | `criar(dados)` |
| `BuscarPorIdUseCase<T, ID>` | `buscarPorId(id)` |
| `AtualizarUseCase<T, ID>` | `atualizar(id, dados)` |
| `ExcluirUseCase<ID>` | `excluirPorId(id)` |
| `ListarUseCase<T>` | `listar(offset, limite)` |

Cada um dos 20 modelos iniciais tem uma base `Abstract<Model>CrudUseCase` no pacote de sua área. Essas classes implementam as cinco interfaces e deixam os métodos abstratos, sem retornos fictícios ou exceções de operação não implementada. A infraestrutura poderá fornecer subclasses concretas. Se uma implementação precisar de apenas uma operação, pode implementar diretamente sua interface, sem estender a base completa.

Exemplo de dependência de leitura: `BuscarPorIdUseCase<Anuncio, UUID>`. O consumidor não precisa depender das operações de escrita. Não há uma interface monolítica obrigando todos os consumidores a conhecer o CRUD completo.

Os modelos continuam responsáveis pelos dados do domínio; não implementam CRUD. As bases de casos de uso são classes separadas. Os métodos têm nomes distintos para permitir que uma classe implemente leitura e exclusão com o mesmo tipo de identificador sem conflito de assinatura.

IDs: UUID para entidades comuns, UUID do usuário para PF/PJ, UUID do veículo para os detalhes, Long para eventos e `VeiculoCaracteristicaId` para o vínculo de chave composta. Essa chave é um record imutável de Java puro com igualdade por valor e rejeita componentes nulos.

Criação e atualização são distintas: atualizar não faz upsert. Os contratos documentam ausência, paginação e preservação da identidade; a execução dessas regras permanece responsabilidade das futuras implementações. Retenção de histórico e permissões de exclusão ainda precisam ser definidas antes de expor exclusão de compras, pagamentos ou eventos.

Os contratos `Salvar*UseCase` e consultas específicas anteriores foram preservados. Não confundir `salvar` com uma definição de upsert; novos fluxos CRUD devem preferir `criar` ou `atualizar` explicitamente. Consultas com filtros, listagens por associação e processamento idempotente de eventos continuam nos contratos específicos.

## Cadastro PF/PJ e complementos da compra

- PF: nome e email em `usuarios`; CPF e nascimento em `usuarios_pf`.
- PJ: nome da empresa em `usuarios_pj.nome_fantasia`, razão social e CNPJ no mesmo perfil; email de contato em `usuarios.email`.
- `usuarios.telefone` representa celular da PF ou número de contato da PJ. Senhas locais são hashes em `senha_hash`, nunca texto puro.
- `enderecos_usuario` armazena um endereço por usuário (PF ou PJ): CEP, cidade, bairro, rua, número, complemento e UF.
- `dados_compra_pf` contém RG, nome do pai, nome da mãe, naturalidade e gênero. `dados_compra_pj` contém inscrição estadual e regime tributário. São complementos opcionais do perfil, preenchidos na etapa de compra; não são snapshots de transações.
- Campos adicionados aceitam ausência para preservar usuários existentes e permitir preenchimento gradual. Isso não define todos como opcionais no formulário: a validação de cadastro completo e da etapa de compra deverá ficar nos futuros casos de uso. Complemento, filiação desconhecida e inscrição isenta precisam de tratamento adequado, sem dados fictícios.
- `identidades_externas` permite Google e Facebook no mesmo usuário, com unicidade de `(provedor, identificador_externo)` no write. A senha local pode ser nula. Nenhum token OAuth é persistido. O identificador deve vir de uma resposta autenticada do provedor; não vincular contas automaticamente pela coincidência de email.
- Esta entrega prepara a persistência, mas não implementa login OAuth, callbacks, credenciais dos provedores, endpoints ou casos de uso de cadastro/compra. O futuro fluxo deve exigir senha local válida ou identidade externa verificada, coletar email se o provedor não o fornecer e completar CPF/CNPJ/endereço antes da etapa que os exige. A criação dos vínculos deve ocorrer na mesma transação do usuário.
- V3 comum adiciona os campos e as quatro tabelas, preservando V1/V2. V4 exclusiva do write aplica FKs e unicidade; o read mantém a projeção assíncrona sem essas restrições. Próximas migrations comuns devem usar V5 ou superior.
- Os quatro novos modelos têm contratos de domínio, entidades JPA, repositories read/write e sincronização Modulith. Os casos de uso desses modelos são interfaces específicas em `domain/usecases/identidade`: salvar e consultar endereço/complementos por usuário, vincular identidade externa verificada e consultar por provedor/identificador. Não há implementações concretas nem CRUD completo para esses modelos. `domain` corresponde ao core da arquitetura, sem frameworks.
