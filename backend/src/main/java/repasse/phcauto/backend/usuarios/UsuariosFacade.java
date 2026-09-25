package repasse.phcauto.backend.usuarios;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repasse.phcauto.backend.usuarios.internal.core.*;
import repasse.phcauto.backend.usuarios.request.AtualizarUsuarioRequest;
import repasse.phcauto.backend.usuarios.response.EnderecoUsuarioResponse;
import repasse.phcauto.backend.usuarios.response.UsuarioDetalhadoResponse;

@Service
public class UsuariosFacade {
    private final CriarUsuarioUseCase criarUsuario;
    private final BuscarUsuarioUseCase buscarUsuario;
    private final AtualizarUsuarioUseCase atualizarUsuario;
    private final ExcluirUsuarioUseCase excluirUsuario;

    public UsuariosFacade(CriarUsuarioUseCase criarUsuario, BuscarUsuarioUseCase buscarUsuario,
            AtualizarUsuarioUseCase atualizarUsuario, ExcluirUsuarioUseCase excluirUsuario) {
        this.criarUsuario = criarUsuario;
        this.buscarUsuario = buscarUsuario;
        this.atualizarUsuario = atualizarUsuario;
        this.excluirUsuario = excluirUsuario;
    }

    @Transactional(transactionManager = "writeTransactionManager")
    public UsuarioResponse criar(CriarUsuarioRequest request) {
        if (request == null || request.endereco() == null) {
            throw new CadastroInvalidoException("Cadastro e endereço obrigatórios");
        }
        var e = request.endereco();
        var endereco = new EnderecoCadastro(e.cep(), e.cidade(), e.bairro(), e.rua(),
                e.numero(), e.complemento(), e.uf());
        var command = new CriarUsuarioCommand(request.tipoPessoa(), request.nome(), request.email(),
                request.telefone(), request.senha(), request.cpf(), request.dataNascimento(),
                request.cnpj(), request.razaoSocial(), endereco);
        var resultado = criarUsuario.execute(command);
        return new UsuarioResponse(resultado.id(), resultado.tipoPessoa(), resultado.nome(),
                resultado.email(), resultado.criadoEm());
    }

    @Transactional(transactionManager = "writeTransactionManager", readOnly = true)
    public UsuarioDetalhadoResponse buscar(UUID usuarioId) {
        return response(buscarUsuario.execute(usuarioId));
    }

    @Transactional(transactionManager = "writeTransactionManager")
    public UsuarioDetalhadoResponse atualizar(UUID usuarioId, AtualizarUsuarioRequest request) {
        return response(atualizarUsuario.execute(usuarioId, request));
    }

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
