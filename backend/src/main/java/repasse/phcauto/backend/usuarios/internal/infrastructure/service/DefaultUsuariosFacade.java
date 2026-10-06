package repasse.phcauto.backend.usuarios.internal.infrastructure.service;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import repasse.phcauto.backend.compliance.ComplianceFacade;
import repasse.phcauto.backend.compliance.RegistrarAceiteCadastroCommand;
import repasse.phcauto.backend.localizacao.LocalizacaoFacade;
import repasse.phcauto.backend.usuarios.internal.core.domain.*;
import repasse.phcauto.backend.usuarios.internal.core.exception.CadastroInvalidoException;
import repasse.phcauto.backend.usuarios.internal.core.usecase.*;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.request.*;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.response.*;

@Service
public class DefaultUsuariosFacade implements UsuariosFacade {
    private final CriarUsuarioUseCase criarUsuario;
    private final BuscarUsuarioUseCase buscarUsuario;
    private final AtualizarUsuarioUseCase atualizarUsuario;
    private final ExcluirUsuarioUseCase excluirUsuario;
    private final ComplianceFacade compliance;
    private final LocalizacaoFacade localizacao;
    private final TransactionTemplate transacaoWrite;

    public DefaultUsuariosFacade(CriarUsuarioUseCase criarUsuario, BuscarUsuarioUseCase buscarUsuario,
            AtualizarUsuarioUseCase atualizarUsuario, ExcluirUsuarioUseCase excluirUsuario,
            ComplianceFacade compliance, LocalizacaoFacade localizacao,
            @Qualifier("writeTransactionManager") PlatformTransactionManager transactionManager) {
        this.criarUsuario = criarUsuario;
        this.buscarUsuario = buscarUsuario;
        this.atualizarUsuario = atualizarUsuario;
        this.excluirUsuario = excluirUsuario;
        this.compliance = compliance;
        this.localizacao = localizacao;
        this.transacaoWrite = new TransactionTemplate(transactionManager);
    }

    @Override
    public UsuarioResponse criar(CriarUsuarioRequest request, String enderecoRede) {
        if (request == null) throw new CadastroInvalidoException("Cadastro obrigatório");
        var e = request.endereco();
        EnderecoCadastro endereco = null;
        if (e != null) {
            var municipio = localizacao.buscarMunicipio(e.cidade(), e.uf());
            endereco = new EnderecoCadastro(e.cep(), municipio.nome(), e.bairro(), e.rua(), e.numero(),
                    e.complemento(), municipio.uf(), municipio.codigoIbge());
        }
        var command = new CriarUsuarioCommand(request.tipoPessoa(), request.nome(), request.email(),
                request.telefone(), request.senha(), request.cpf(), request.dataNascimento(),
                request.cnpj(), request.razaoSocial(), endereco, request.papel());
        return transacaoWrite.execute(status -> {
            var resultado = criarUsuario.execute(command);
            if (resultado.papel() != repasse.phcauto.backend.domain.model.identidade.PapelUsuario.DONO) {
                compliance.registrarAceiteCadastro(new RegistrarAceiteCadastroCommand(
                        resultado.id(), request.aceitouTermos(), request.aceiteTermosEm(),
                        request.versaoTermosUso(), request.versaoPoliticaPrivacidade(), enderecoRede));
            }
            return new UsuarioResponse(resultado.id(), resultado.tipoPessoa(), resultado.papel(), resultado.nome(),
                    resultado.email(), resultado.criadoEm());
        });
    }

    @Override
    @Transactional(transactionManager = "writeTransactionManager", readOnly = true)
    public UsuarioDetalhadoResponse buscar(UUID usuarioId) { return response(buscarUsuario.execute(usuarioId)); }

    @Override
    public UsuarioDetalhadoResponse atualizar(UUID usuarioId, AtualizarUsuarioRequest request) {
        if (request == null) throw new CadastroInvalidoException("Alterações obrigatórias");
        var enderecoRequest = request.endereco();
        repasse.phcauto.backend.usuarios.internal.core.domain.AtualizarUsuarioCommand.EnderecoUsuarioPatch enderecoPatch = null;
        if (enderecoRequest != null) {
            var atual = transacaoWrite.execute(status -> buscarUsuario.execute(usuarioId).endereco());
            var cep = valor(enderecoRequest.cep(), atual.cep());
            var cidade = valor(enderecoRequest.cidade(), atual.cidade());
            var bairro = valor(enderecoRequest.bairro(), atual.bairro());
            var rua = valor(enderecoRequest.rua(), atual.rua());
            var numero = valor(enderecoRequest.numero(), atual.numero());
            var uf = valor(enderecoRequest.uf(), atual.uf());
            var municipio = localizacao.buscarMunicipio(cidade, uf);
            enderecoPatch = new repasse.phcauto.backend.usuarios.internal.core.domain.AtualizarUsuarioCommand.EnderecoUsuarioPatch(
                    enderecoRequest.cep(), municipio.nome(), enderecoRequest.bairro(), enderecoRequest.rua(),
                    enderecoRequest.numero(), enderecoRequest.complemento(), municipio.uf(),
                    municipio.codigoIbge());
        }
        var command = new repasse.phcauto.backend.usuarios.internal.core.domain.AtualizarUsuarioCommand(
                request.nome(), request.email(), request.telefone(), request.senha(), request.cpf(),
                request.dataNascimento(), request.cnpj(), request.razaoSocial(), enderecoPatch);
        return transacaoWrite.execute(status -> response(atualizarUsuario.execute(usuarioId, command)));
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

    private static String valor(String novo, String atual) { return novo == null ? atual : novo; }
}
