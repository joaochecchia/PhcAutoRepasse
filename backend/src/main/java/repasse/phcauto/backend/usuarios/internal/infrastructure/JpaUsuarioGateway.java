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
public class JpaUsuarioGateway implements UsuarioGateway {
    private final UsuarioWriteRepository usuarios;
    private final UsuarioPfWriteRepository pessoasFisicas;
    private final UsuarioPjWriteRepository pessoasJuridicas;
    private final EnderecoUsuarioWriteRepository enderecos;

    public JpaUsuarioGateway(UsuarioWriteRepository usuarios, UsuarioPfWriteRepository pessoasFisicas,
            UsuarioPjWriteRepository pessoasJuridicas, EnderecoUsuarioWriteRepository enderecos) {
        this.usuarios = usuarios;
        this.pessoasFisicas = pessoasFisicas;
        this.pessoasJuridicas = pessoasJuridicas;
        this.enderecos = enderecos;
    }

    @Override public boolean existeEmail(String email) { return usuarios.existsByEmailIgnoreCase(email); }

    @Override public boolean existeDocumento(TipoPessoa tipo, String documento) {
        return tipo == TipoPessoa.PF ? pessoasFisicas.existsByCpf(documento) : pessoasJuridicas.existsByCnpj(documento);
    }

    @Override public void salvarCadastro(UUID id, DadosNovoUsuario dados, Instant agora) {
        try {
            usuarios.saveAndFlush(UsuarioEntity.novoCadastro(id, dados, agora));
            if (dados.tipoPessoa() == TipoPessoa.PF) {
                pessoasFisicas.saveAndFlush(UsuarioPfEntity.novoCadastro(id, dados, agora));
            } else {
                pessoasJuridicas.saveAndFlush(UsuarioPjEntity.novoCadastro(id, dados, agora));
            }
            enderecos.saveAndFlush(EnderecoUsuarioEntity.novoCadastro(id, dados, agora));
        } catch (DataIntegrityViolationException failure) {
            for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
                if (cause instanceof SQLException sql && "23505".equals(sql.getSQLState())) {
                    throw new CadastroDuplicadoException("Já existe cadastro com os dados informados");
                }
            }
            throw failure;
        }
    }
}
