package repasse.phcauto.backend.usuarios.internal.core;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import repasse.phcauto.backend.domain.event.identidade.UsuarioAlterado;
import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;
import repasse.phcauto.backend.usuarios.request.AtualizarUsuarioRequest;
import repasse.phcauto.backend.usuarios.request.EnderecoPatchRequest;

public final class AtualizarUsuario implements AtualizarUsuarioUseCase {
    private final ConsultarUsuarioGateway consultas;
    private final AtualizarUsuarioGateway atualizacoes;
    private final HashSenhaGateway senhas;
    private final PublicarUsuarioAlteradoGateway eventos;
    private final Clock clock;

    public AtualizarUsuario(ConsultarUsuarioGateway consultas, AtualizarUsuarioGateway atualizacoes,
            HashSenhaGateway senhas, PublicarUsuarioAlteradoGateway eventos, Clock clock) {
        this.consultas = Objects.requireNonNull(consultas);
        this.atualizacoes = Objects.requireNonNull(atualizacoes);
        this.senhas = Objects.requireNonNull(senhas);
        this.eventos = Objects.requireNonNull(eventos);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override public UsuarioCompleto execute(UUID usuarioId, AtualizarUsuarioRequest request) {
        Objects.requireNonNull(usuarioId, "usuarioId");
        Objects.requireNonNull(request, "request");
        if (semAlteracoes(request)) throw new CadastroInvalidoException("Informe ao menos uma alteração");
        var atual = consultas.buscar(usuarioId);
        validarCamposDoTipo(atual.tipoPessoa(), request);

        String nome = request.nome() == null ? null : ValidacaoCadastro.texto(request.nome(), "Nome", 160);
        String email = request.email() == null ? null
                : ValidacaoCadastro.texto(request.email(), "Email", 254).toLowerCase(Locale.ROOT);
        if (email != null && !email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            throw new CadastroInvalidoException("Email inválido");
        }
        String telefone = request.telefone() == null ? null
                : ValidacaoCadastro.texto(request.telefone(), "Telefone", 32);
        if (telefone != null && !telefone.matches("\\+?[1-9][0-9]{9,14}")) {
            throw new CadastroInvalidoException("Telefone inválido");
        }
        String senhaHash = null;
        if (request.senha() != null) {
            if (request.senha().isBlank() || request.senha().length() < 8 || request.senha().length() > 128) {
                throw new CadastroInvalidoException("Senha deve ter entre 8 e 128 caracteres");
            }
            senhaHash = senhas.gerar(request.senha());
        }

        String cpf = validarCpf(request.cpf());
        LocalDate nascimento = validarNascimento(request.dataNascimento());
        String cnpj = validarCnpj(request.cnpj());
        String razaoSocial = request.razaoSocial() == null ? null
                : ValidacaoCadastro.texto(request.razaoSocial(), "Razão social", 200);
        EnderecoUsuarioDados endereco = mesclarEndereco(atual.endereco(), request.endereco());

        if (email != null && !email.equals(atual.email())
                && atualizacoes.existeEmailDeOutroUsuario(email, usuarioId)) {
            throw new CadastroDuplicadoException("Já existe cadastro com os dados informados");
        }
        String documento = atual.tipoPessoa() == TipoPessoa.PF ? cpf : cnpj;
        if (documento != null && atualizacoes.existeDocumentoDeOutroUsuario(atual, documento)) {
            throw new CadastroDuplicadoException("Já existe cadastro com os dados informados");
        }

        var alteracoes = new AlteracoesUsuario(nome, email, telefone, senhaHash, cpf, nascimento,
                cnpj, razaoSocial, endereco);
        var agora = clock.instant();
        var atualizado = atualizacoes.atualizar(usuarioId, alteracoes, agora);
        eventos.publicar(new UsuarioAlterado(UUID.randomUUID(), usuarioId, atual.tipoPessoa(),
                UsuarioAlterado.Operacao.ATUALIZADO, agora));
        return atualizado;
    }

    private boolean semAlteracoes(AtualizarUsuarioRequest request) {
        return request.nome() == null && request.email() == null && request.telefone() == null
                && request.senha() == null && request.cpf() == null && request.dataNascimento() == null
                && request.cnpj() == null && request.razaoSocial() == null && request.endereco() == null;
    }

    private void validarCamposDoTipo(TipoPessoa tipo, AtualizarUsuarioRequest request) {
        if (tipo == TipoPessoa.PF && (request.cnpj() != null || request.razaoSocial() != null)) {
            throw new CadastroInvalidoException("PF não aceita dados de PJ");
        }
        if (tipo == TipoPessoa.PJ && (request.cpf() != null || request.dataNascimento() != null)) {
            throw new CadastroInvalidoException("PJ não aceita dados de PF");
        }
    }

    private String validarCpf(String cpf) {
        if (cpf != null && !ValidacaoCadastro.documentoValido(cpf, true)) {
            throw new CadastroInvalidoException("CPF inválido");
        }
        return cpf;
    }

    private String validarCnpj(String cnpj) {
        if (cnpj != null && !ValidacaoCadastro.documentoValido(cnpj, false)) {
            throw new CadastroInvalidoException("CNPJ inválido");
        }
        return cnpj;
    }

    private LocalDate validarNascimento(LocalDate nascimento) {
        if (nascimento != null && !nascimento.isBefore(LocalDate.now(clock))) {
            throw new CadastroInvalidoException("Nascimento deve estar no passado");
        }
        return nascimento;
    }

    private EnderecoUsuarioDados mesclarEndereco(EnderecoUsuarioDados atual, EnderecoPatchRequest patch) {
        if (patch == null) return null;
        return new EnderecoUsuarioDados(
                patch.cep() == null ? atual.cep() : validarCep(patch.cep()),
                patch.cidade() == null ? atual.cidade() : ValidacaoCadastro.texto(patch.cidade(), "Cidade", 120),
                patch.bairro() == null ? atual.bairro() : ValidacaoCadastro.texto(patch.bairro(), "Bairro", 120),
                patch.rua() == null ? atual.rua() : ValidacaoCadastro.texto(patch.rua(), "Rua", 200),
                patch.numero() == null ? atual.numero() : ValidacaoCadastro.opcional(patch.numero(), "Número", 20),
                patch.complemento() == null ? atual.complemento()
                        : ValidacaoCadastro.opcional(patch.complemento(), "Complemento", 200),
                patch.uf() == null ? atual.uf() : validarUf(patch.uf()));
    }

    private String validarCep(String cep) {
        cep = ValidacaoCadastro.texto(cep, "CEP", 8);
        if (!cep.matches("[0-9]{8}")) throw new CadastroInvalidoException("CEP inválido");
        return cep;
    }

    private String validarUf(String uf) {
        return new EnderecoCadastro("01001000", "x", "x", "x", null, null, uf).uf();
    }
}
