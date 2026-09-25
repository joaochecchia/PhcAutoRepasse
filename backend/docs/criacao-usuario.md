# Criação de usuário

O módulo `repasse.phcauto.backend.usuarios` implementa cadastro **local** de PF e PJ, com senha e endereço. O login e o cadastro por provedores Google/Facebook ainda exigem um fluxo autenticado próprio.

## Organização

```text
usuarios/
  package-info.java
  CriarUsuarioRequest.java
  EnderecoRequest.java
  UsuarioResponse.java
  UsuarioCriado.java
  UsuariosFacade.java
  internal/
    core/
      CriarUsuarioUseCase.java
      CriarUsuario.java
      CriarUsuarioCommand.java
      DadosNovoUsuario.java
      EnderecoCadastro.java
      UsuarioCriadoResultado.java
      UsuarioGateway.java
      HashSenhaGateway.java
      PublicarUsuarioCriadoGateway.java
      CadastroInvalidoException.java
      CadastroDuplicadoException.java
      ValidacaoCadastro.java
    infrastructure/
      controller/
        LoginController.java
        UsuariosController.java
      JpaUsuarioGateway.java
      SpringUsuarioCriadoPublisher.java
      UsuariosConfiguration.java
      UsuariosExceptionHandler.java
      entity/
      repository/read/
      repository/write/
```

A raiz expõe contratos HTTP, fachada e evento público. Os controllers são adaptadores Spring MVC internos em `internal/infrastructure/controller`. O core contém interface e implementação do caso de uso com `execute`, portas e validações Java puras. `UsuariosConfiguration` monta o caso de uso por injeção de dependências. A fachada delimita a transação write. As quatro entidades e seus repositories foram movidos, preservando nomes de tabelas, dados e versões; nenhuma migration nova foi necessária.

O caso de uso solicita a publicação por `PublicarUsuarioCriadoGateway`. Seu adaptador usa `ApplicationEventPublisher` na mesma transação. Assim o core não importa Spring. Importar diretamente esse publisher no core contrariaria sua independência de frameworks.

Os demais módulos ainda têm estrutura transitória. O sincronizador compartilhado conhece os mapeamentos JPA internos para projetar linhas, mas os outros módulos de negócio não acessam os detalhes de `usuarios`. `ApplicationModules.verify()` e um teste ArchUnit verificam os limites atuais e a independência do core.

## Endpoint

`POST /api/v1/usuarios` substitui a criação provisória e é a única rota de cadastro. O novo contrato é tipado e retorna `UsuarioResponse`, sem o envelope HashMap anterior. As outras operações provisórias não passaram a persistir dados.

Exemplo PF:

```json
{
  "tipoPessoa": "PF",
  "nome": "Cliente de exemplo",
  "email": "cliente@example.com",
  "telefone": "11999999999",
  "senha": "Senha-de-exemplo-123",
  "cpf": "52998224725",
  "dataNascimento": "1990-01-01",
  "endereco": {
    "cep": "01001000",
    "cidade": "São Paulo",
    "bairro": "Sé",
    "rua": "Praça da Sé",
    "numero": "10",
    "complemento": null,
    "uf": "SP"
  }
}
```

Para PJ, usar `tipoPessoa: "PJ"`, `nome` como nome da empresa, `cnpj` e `razaoSocial`; omitir CPF e nascimento. Exemplo:

```json
{
  "tipoPessoa": "PJ",
  "nome": "Empresa de exemplo",
  "email": "empresa@example.com",
  "telefone": "1133334444",
  "senha": "Senha-de-exemplo-123",
  "cnpj": "11222333000181",
  "razaoSocial": "Empresa de exemplo LTDA",
  "endereco": {
    "cep": "01001000",
    "cidade": "São Paulo",
    "bairro": "Sé",
    "rua": "Praça da Sé",
    "numero": "10",
    "complemento": null,
    "uf": "SP"
  }
}
```

Os documentos acima são dados sintéticos de teste. Nesta versão CPF e CNPJ seguem o formato numérico do esquema existente, sem pontuação, e têm dígitos verificadores validados. Não se mistura PF e PJ. Email é normalizado em minúsculas; nome e campos textuais são aparados. Senha mantém exatamente o texto recebido. Nascimento deve ser anterior à data UTC atual; nenhum limite etário foi inventado.

Nome, email, telefone, senha, tipo, perfil correspondente e endereço são obrigatórios. Endereço exige CEP, cidade, bairro, rua e UF brasileira válida. Número e complemento continuam opcionais, como nos contratos de cadastro anteriores. Dados complementares de compra não são criados no cadastro.

- **201**: resposta com id, tipoPessoa, nome, email e criadoEm.
- **400**: campos inválidos, perfil incompatível ou corpo malformado.
- **409**: email/CPF/CNPJ já cadastrado, inclusive colisões concorrentes protegidas pelas unicidades PostgreSQL.

Nenhum parâmetro permite escolher papel administrativo; o cadastro cria CLIENTE ativo. A verificação de credenciais local está disponível no [fluxo de login](login.md); sessão, tokens e OAuth continuam pendentes. A senha só entra no request/comando, é codificada com PBKDF2 do Spring Security Crypto e sai do core para persistência apenas como hash. Respostas e eventos não transportam senha ou hash. A dependência crypto não instala filtros de autenticação HTTP.

## Transação e eventos

```text
HTTP -> UsuariosFacade (@Transactional write)
     -> CriarUsuario.execute
        -> validações e unicidades no write
        -> hash da senha
        -> JpaUsuarioGateway
           -> usuarios
           -> usuarios_pf OU usuarios_pj
           -> enderecos_usuario
        -> SpringUsuarioCriadoPublisher -> ApplicationEventPublisher
     -> commit
        -> RowChanged por linha -> projeção read
        -> UsuarioCriado -> listeners de negócio registrados
```

Falha no endereço, no perfil ou na publicação reverte a transação inteira. Flushes confirmam restrições antes da publicação, mas não fazem commits intermediários. A projeção mantém consistência eventual e não é usada para decidir unicidade.

`UsuarioCriado` contém eventId, usuarioId, tipoPessoa e ocorridoEm. Não cria perfil nem endereço por listeners. O registro Modulith persiste uma entrega para cada listener transacional registrado, com recuperação de falhas. Não há listener de negócio fictício em produção; email e outros efeitos serão implementados quando definidos. O teste de integração registra um consumidor real para verificar commit, falha e reentrega. `UsuarioAlterado` permanece como preparação anterior para a evolução de atualização/exclusão; a criação real publica apenas `UsuarioCriado` além dos eventos técnicos.

## Testes

Testes rápidos:

```bash
./mvnw -B -ntp -Dtest=CriarUsuarioTests,UsuariosArchitectureTests,ModulithStructureTests test
```

Integração, com **dois bancos isolados**, migrations habilitadas e permissões read preparadas:

```bash
POSTGRES_DB=phcauto_usuario_test_20260925 \
POSTGRES_PORT=15432 POSTGRES_READ_PORT=15433 REDIS_PORT=16379 \
USUARIOS_INTEGRATION_TEST=true ./mvnw -B -ntp -Dtest=UsuariosIntegrationTests test
```

A integração grava dados sintéticos; não apontar para produção. Verifica HTTP, PF/PJ, hash, ausência de credenciais na resposta, perfil exclusivo, endereço, projeção read, rollback, colisão concorrente e recuperação de listener. Os testes de persistência existentes cobrem os 24 mapeamentos e podem ser habilitados com `PERSISTENCE_INTEGRATION_TEST=true` nos mesmos bancos isolados.

Para executar o código atualizado no contêiner, reconstrua o backend com `docker compose up -d --build backend` usando as portas/variáveis do ambiente atual.
