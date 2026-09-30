package repasse.phcauto.backend.usuarios.internal.infrastructure.service;

import java.util.UUID;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.request.CriarUsuarioRequest;
import repasse.phcauto.backend.compliance.ComplianceFacade;
import repasse.phcauto.backend.compliance.RegistrarAceiteCadastroCommand;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.response.UsuarioResponse;
import repasse.phcauto.backend.usuarios.internal.infrastructure.service.UsuariosFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.request.AtualizarUsuarioRequest;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.response.EnderecoUsuarioResponse;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.response.UsuarioDetalhadoResponse;

import repasse.phcauto.backend.usuarios.internal.core.domain.CriarUsuarioCommand;
import repasse.phcauto.backend.usuarios.internal.core.domain.EnderecoCadastro;
import repasse.phcauto.backend.usuarios.internal.core.domain.UsuarioCompleto;
import repasse.phcauto.backend.usuarios.internal.core.exception.CadastroInvalidoException;
import repasse.phcauto.backend.usuarios.internal.core.usecase.AtualizarUsuarioUseCase;
import repasse.phcauto.backend.usuarios.internal.core.usecase.BuscarUsuarioUseCase;
import repasse.phcauto.backend.usuarios.internal.core.usecase.CriarUsuarioUseCase;
import repasse.phcauto.backend.usuarios.internal.core.usecase.ExcluirUsuarioUseCase;
@Service
public class DefaultUsuariosFacade implements UsuariosFacade {
    private final CriarUsuarioUseCase criarUsuario;
    private final BuscarUsuarioUseCase buscarUsuario;
    private final AtualizarUsuarioUseCase atualizarUsuario;
    private final ExcluirUsuarioUseCase excluirUsuario;
    private final ComplianceFacade compliance;

    public DefaultUsuariosFacade(CriarUsuarioUseCase criarUsuario, BuscarUsuarioUseCase buscarUsuario,
            AtualizarUsuarioUseCase atualizarUsuario, ExcluirUsuarioUseCase excluirUsuario,
            ComplianceFacade compliance) {
        this.criarUsuario = criarUsuario;
        this.buscarUsuario = buscarUsuario;
        this.atualizarUsuario = atualizarUsuario;
        this.excluirUsuario = excluirUsuario;
        this.compliance = compliance;
    }

    @Override
    @Transactional(transactionManager = "writeTransactionManager")
    public UsuarioResponse criar(CriarUsuarioRequest request, String enderecoRede) {
        if (request == null) throw new CadastroInvalidoException("Cadastro obrigatório");
        var e = request.endereco();
        var endereco = e == null ? null : new EnderecoCadastro(e.cep(), e.cidade(), e.bairro(), e.rua(),
                e.numero(), e.complemento(), e.uf());
        var command = new CriarUsuarioCommand(request.tipoPessoa(), request.nome(), request.email(),
                request.telefone(), request.senha(), request.cpf(), request.dataNascimento(),
                request.cnpj(), request.razaoSocial(), endereco, request.papel());
        var resultado = criarUsuario.execute(command);
        if (resultado.papel() != repasse.phcauto.backend.domain.model.identidade.PapelUsuario.DONO) {
            compliance.registrarAceiteCadastro(new RegistrarAceiteCadastroCommand(
                    resultado.id(), request.aceitouTermos(), request.aceiteTermosEm(),
                    request.versaoTermosUso(), request.versaoPoliticaPrivacidade(), enderecoRede));
        }
        return new UsuarioResponse(resultado.id(), resultado.tipoPessoa(), resultado.papel(), resultado.nome(),
                resultado.email(), resultado.criadoEm());
    }

    @Override
    @Transactional(transactionManager = "writeTransactionManager", readOnly = true)
    public UsuarioDetalhadoResponse buscar(UUID usuarioId) {
        return response(buscarUsuario.execute(usuarioId));
    }

    @Override
    @Transactional(transactionManager = "writeTransactionManager")
    public UsuarioDetalhadoResponse atualizar(UUID usuarioId, AtualizarUsuarioRequest request) {
        var enderecoRequest = request.endereco();
        var enderecoPatch = enderecoRequest == null ? null
                : new repasse.phcauto.backend.usuarios.internal.core.domain.AtualizarUsuarioCommand.EnderecoUsuarioPatch(
                        enderecoRequest.cep(), enderecoRequest.cidade(), enderecoRequest.bairro(),
                        enderecoRequest.rua(), enderecoRequest.numero(), enderecoRequest.complemento(),
                        enderecoRequest.uf());
        var command = new repasse.phcauto.backend.usuarios.internal.core.domain.AtualizarUsuarioCommand(
                request.nome(), request.email(), request.telefone(), request.senha(), request.cpf(),
                request.dataNascimento(), request.cnpj(), request.razaoSocial(), enderecoPatch);
        return response(atualizarUsuario.execute(usuarioId, command));
    }

    @Override
    @Transactional(transactionManager = "writeTransactionManager")
    public void excluir(UUID usuarioId) { excluirUsuario.execute(usuarioId); }

    private UsuarioDetalhadoResponse response(UsuarioCompleto usuario) {
        var endereco = usuario.endereco();
        return new UsuarioDetalhadoResponse(usuario.id(), usuario.tipoPessoa(), usuario.nome(),
                usuario.email(), usuario.telefone(), usuario.ativo(), usuario.cpf(),
                usuario.dataNascimento(), usuario.cnpj(), usuario.razaoSocial(),
                new EnderecoUsuarioResponse(endereco.cep(), endereco.cidade(), endereco.bairro(),
                        endereco.rua(), endereco.numero(), endereco.complemento(), endereco.uf()),
                usuario.criadoEm(), usuario.atualizadoEm());
    }
}
