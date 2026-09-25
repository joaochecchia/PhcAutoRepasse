package repasse.phcauto.backend.usuarios.internal.infrastructure;

import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;
import repasse.phcauto.backend.usuarios.internal.core.*;
import repasse.phcauto.backend.usuarios.internal.infrastructure.entity.*;
import repasse.phcauto.backend.usuarios.internal.infrastructure.repository.write.*;

@Component
@Transactional(transactionManager = "writeTransactionManager", propagation = Propagation.MANDATORY)
public class JpaUsuarioAggregateGateway implements ConsultarUsuarioGateway,
        AtualizarUsuarioGateway, ExcluirUsuarioGateway {
    private final UsuarioWriteRepository usuarios;
    private final UsuarioPfWriteRepository pessoasFisicas;
    private final UsuarioPjWriteRepository pessoasJuridicas;
    private final EnderecoUsuarioWriteRepository enderecos;

    public JpaUsuarioAggregateGateway(UsuarioWriteRepository usuarios,
            UsuarioPfWriteRepository pessoasFisicas,
            UsuarioPjWriteRepository pessoasJuridicas,
            EnderecoUsuarioWriteRepository enderecos) {
        this.usuarios = usuarios;
        this.pessoasFisicas = pessoasFisicas;
        this.pessoasJuridicas = pessoasJuridicas;
        this.enderecos = enderecos;
    }

    @Override public UsuarioCompleto buscar(UUID usuarioId) {
        var usuario = usuarios.findById(usuarioId).orElseThrow(UsuarioNaoEncontradoException::new);
        return completo(usuario);
    }

    @Override public boolean existeEmailDeOutroUsuario(String email, UUID usuarioId) {
        return usuarios.existsByEmailIgnoreCaseAndIdNot(email, usuarioId);
    }

    @Override public boolean existeDocumentoDeOutroUsuario(UsuarioCompleto atual, String documento) {
        return atual.tipoPessoa() == TipoPessoa.PF
                ? pessoasFisicas.existsByCpfAndUsuarioIdNot(documento, atual.id())
                : pessoasJuridicas.existsByCnpjAndUsuarioIdNot(documento, atual.id());
    }

    @Override public UsuarioCompleto atualizar(UUID usuarioId, AlteracoesUsuario alteracoes, Instant agora) {
        try {
            var usuario = usuarios.findById(usuarioId).orElseThrow(UsuarioNaoEncontradoException::new);
            usuario.aplicarPatch(alteracoes.nome(), alteracoes.email(), alteracoes.telefone(),
                    alteracoes.senhaHash(), agora);
            usuarios.saveAndFlush(usuario);
            if (usuario.getTipoPessoa() == TipoPessoa.PF) {
                var perfil = pessoasFisicas.findById(usuarioId)
                        .orElseThrow(() -> new IllegalStateException("Perfil PF obrigatório ausente"));
                perfil.aplicarPatch(alteracoes.cpf(), alteracoes.dataNascimento());
                pessoasFisicas.saveAndFlush(perfil);
            } else {
                var perfil = pessoasJuridicas.findById(usuarioId)
                        .orElseThrow(() -> new IllegalStateException("Perfil PJ obrigatório ausente"));
                perfil.aplicarPatch(alteracoes.cnpj(), alteracoes.razaoSocial(), alteracoes.nome());
                pessoasJuridicas.saveAndFlush(perfil);
            }
            if (alteracoes.endereco() != null) {
                var endereco = enderecos.findById(usuarioId)
                        .orElseThrow(() -> new IllegalStateException("Endereço obrigatório ausente"));
                endereco.aplicarPatch(alteracoes.endereco());
                enderecos.saveAndFlush(endereco);
            }
            return completo(usuario);
        } catch (DataIntegrityViolationException failure) {
            if (isUniqueViolation(failure)) {
                throw new CadastroDuplicadoException("Já existe cadastro com os dados informados");
            }
            throw failure;
        }
    }

    @Override public UsuarioCompleto excluir(UUID usuarioId) {
        var usuario = usuarios.findById(usuarioId).orElseThrow(UsuarioNaoEncontradoException::new);
        var completo = completo(usuario);
        try {
            enderecos.deleteById(usuarioId);
            enderecos.flush();
            if (usuario.getTipoPessoa() == TipoPessoa.PF) {
                pessoasFisicas.deleteById(usuarioId);
                pessoasFisicas.flush();
            } else {
                pessoasJuridicas.deleteById(usuarioId);
                pessoasJuridicas.flush();
            }
            usuarios.delete(usuario);
            usuarios.flush();
            return completo;
        } catch (DataIntegrityViolationException failure) {
            throw new ExclusaoUsuarioBloqueadaException();
        }
    }

    private UsuarioCompleto completo(UsuarioEntity usuario) {
        var endereco = enderecos.findById(usuario.getId())
                .orElseThrow(() -> new IllegalStateException("Endereço obrigatório ausente"));
        var enderecoDados = new EnderecoUsuarioDados(endereco.getCep(), endereco.getCidade(),
                endereco.getBairro(), endereco.getRua(), endereco.getNumero(),
                endereco.getComplemento(), endereco.getUf());
        if (usuario.getTipoPessoa() == TipoPessoa.PF) {
            var perfil = pessoasFisicas.findById(usuario.getId())
                    .orElseThrow(() -> new IllegalStateException("Perfil PF obrigatório ausente"));
            return new UsuarioCompleto(usuario.getId(), usuario.getTipoPessoa(), usuario.getNome(),
                    usuario.getEmail(), usuario.getTelefone(), usuario.getAtivo(), usuario.getSenhaHash(),
                    perfil.getCpf(), perfil.getDataNascimento(), null, null, enderecoDados,
                    usuario.getCriadoEm(), usuario.getAtualizadoEm());
        }
        var perfil = pessoasJuridicas.findById(usuario.getId())
                .orElseThrow(() -> new IllegalStateException("Perfil PJ obrigatório ausente"));
        return new UsuarioCompleto(usuario.getId(), usuario.getTipoPessoa(), usuario.getNome(),
                usuario.getEmail(), usuario.getTelefone(), usuario.getAtivo(), usuario.getSenhaHash(),
                null, null, perfil.getCnpj(), perfil.getRazaoSocial(), enderecoDados,
                usuario.getCriadoEm(), usuario.getAtualizadoEm());
    }

    private boolean isUniqueViolation(DataIntegrityViolationException failure) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sql && "23505".equals(sql.getSQLState())) return true;
        }
        return false;
    }
}
