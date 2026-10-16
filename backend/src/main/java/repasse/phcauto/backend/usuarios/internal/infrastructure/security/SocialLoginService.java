package repasse.phcauto.backend.usuarios.internal.infrastructure.security;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repasse.phcauto.backend.domain.model.identidade.ProvedorAutenticacao;
import repasse.phcauto.backend.usuarios.*;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.request.CriarUsuarioRequest;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.entity.IdentidadeExternaEntity;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.repository.write.IdentidadeExternaWriteRepository;
import repasse.phcauto.backend.usuarios.internal.infrastructure.service.UsuariosFacade;

@Service
public class SocialLoginService {
    private final IdentidadeExternaWriteRepository identidades;
    private final SegurancaUsuarios usuariosSeguranca;
    private final UsuariosFacade usuarios;
    private final SessoesUsuarios sessoes;
    private final SocialRegistrationTicketService tickets;
    private final Validator validator;
    private final SecureRandom random = new SecureRandom();

    public SocialLoginService(IdentidadeExternaWriteRepository identidades,
            SegurancaUsuarios usuariosSeguranca, UsuariosFacade usuarios, SessoesUsuarios sessoes,
            SocialRegistrationTicketService tickets, Validator validator) {
        this.identidades = identidades;
        this.usuariosSeguranca = usuariosSeguranca;
        this.usuarios = usuarios;
        this.sessoes = sessoes;
        this.tickets = tickets;
        this.validator = validator;
    }

    @Transactional(transactionManager = "writeTransactionManager", readOnly = true)
    public Optional<UsuarioSeguranca> buscar(ProvedorAutenticacao provedor, String subject) {
        return identidades.findByProvedorAndIdentificadorExterno(provedor, subject)
                .map(i -> usuariosSeguranca.buscarAtivo(i.getUsuarioId()));
    }

    public boolean emailPertenceAConta(String email) {
        return usuariosSeguranca.buscarAtivoPorEmail(email).isPresent();
    }

    @Transactional(transactionManager = "writeTransactionManager")
    public CadastroConcluido concluirCadastro(String token, CriarUsuarioRequest informado, String enderecoRede) {
        var social = tickets.buscar(token);
        byte[] senhaBytes = new byte[48];
        random.nextBytes(senhaBytes);
        String senhaInacessivel = Base64.getUrlEncoder().withoutPadding().encodeToString(senhaBytes);
        var request = new CriarUsuarioRequest(informado.tipoPessoa(), informado.papel(), social.nome(),
                social.email().toLowerCase(Locale.ROOT), informado.telefone(), senhaInacessivel, senhaInacessivel,
                informado.cpf(), informado.dataNascimento(), informado.cnpj(), informado.razaoSocial(),
                informado.endereco(), informado.aceitouTermos(), informado.aceiteTermosEm(),
                informado.versaoTermosUso(), informado.versaoPoliticaPrivacidade());
        var violations = validator.validate(request);
        if (!violations.isEmpty()) throw new ConstraintViolationException(violations);
        var criado = usuarios.criar(request, enderecoRede);
        if (identidades.existsByUsuarioIdAndProvedor(criado.id(), social.provedor())) {
            throw new IllegalStateException("Identidade social já vinculada");
        }
        identidades.saveAndFlush(IdentidadeExternaEntity.nova(criado.id(), social.provedor(), social.subject()));
        usuariosSeguranca.desabilitarSenhaLocal(criado.id());
        tickets.consumir(token);
        var usuario = usuariosSeguranca.buscarAtivo(criado.id());
        return new CadastroConcluido(usuario, sessoes.iniciar(usuario.id(), usuario.papel()));
    }

    public record CadastroConcluido(UsuarioSeguranca usuario, SessoesUsuarios.SessaoEmitida sessao) { }
}
