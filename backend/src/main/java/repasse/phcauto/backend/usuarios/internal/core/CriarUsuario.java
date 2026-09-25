package repasse.phcauto.backend.usuarios.internal.core;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;
import repasse.phcauto.backend.usuarios.UsuarioCriado;

public final class CriarUsuario implements CriarUsuarioUseCase {
    private final UsuarioGateway usuarios;
    private final HashSenhaGateway senhas;
    private final PublicarUsuarioCriadoGateway eventos;
    private final Clock clock;

    public CriarUsuario(UsuarioGateway usuarios, HashSenhaGateway senhas,
            PublicarUsuarioCriadoGateway eventos, Clock clock) {
        this.usuarios = Objects.requireNonNull(usuarios);
        this.senhas = Objects.requireNonNull(senhas);
        this.eventos = Objects.requireNonNull(eventos);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override public UsuarioCriadoResultado execute(CriarUsuarioCommand c) {
        Objects.requireNonNull(c, "command");
        if (c.tipoPessoa() == TipoPessoa.PF && !c.dataNascimento().isBefore(LocalDate.now(clock))) {
            throw new CadastroInvalidoException("Nascimento deve estar no passado");
        }
        String documento = c.tipoPessoa() == TipoPessoa.PF ? c.cpf() : c.cnpj();
        if (usuarios.existeEmail(c.email()) || usuarios.existeDocumento(c.tipoPessoa(), documento)) {
            throw new CadastroDuplicadoException("Já existe cadastro com os dados informados");
        }
        String hash = senhas.gerar(c.senha());
        var dados = new DadosNovoUsuario(c.tipoPessoa(), c.nome(), c.email(), c.telefone(), hash,
                c.cpf(), c.dataNascimento(), c.cnpj(), c.razaoSocial(), c.endereco());
        var id = UUID.randomUUID();
        var agora = clock.instant();
        usuarios.salvarCadastro(id, dados, agora);
        eventos.publicar(new UsuarioCriado(UUID.randomUUID(), id, c.tipoPessoa(), agora));
        return new UsuarioCriadoResultado(id, c.tipoPessoa(), c.nome(), c.email(), agora);
    }
}
