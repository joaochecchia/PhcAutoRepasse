package repasse.phcauto.backend.usuarios.internal.infrastructure.security;

import java.time.*;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import repasse.phcauto.backend.domain.model.identidade.*;
import repasse.phcauto.backend.usuarios.internal.core.domain.DadosNovoUsuario;
import repasse.phcauto.backend.domain.model.identidade.PapelUsuario;
import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.entity.UsuarioAdminEntity;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.entity.UsuarioEntity;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.repository.write.UsuarioAdminWriteRepository;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.repository.write.UsuarioWriteRepository;
@Component
@ConditionalOnProperty(name = "app.bootstrap-privileged-users.enabled", havingValue = "true")
public class PrivilegedTestUsersBootstrap implements ApplicationRunner {
    private final UsuarioWriteRepository usuarios;
    private final UsuarioAdminWriteRepository administradores;
    private final PasswordEncoder senhas;
    private final String donoEmail, donoSenha, adminEmail, adminSenha;
    public PrivilegedTestUsersBootstrap(UsuarioWriteRepository usuarios,
            UsuarioAdminWriteRepository administradores, PasswordEncoder senhas,
            @Value("${BOOTSTRAP_DONO_EMAIL}") String donoEmail,
            @Value("${BOOTSTRAP_DONO_PASSWORD}") String donoSenha,
            @Value("${BOOTSTRAP_ADMIN_EMAIL}") String adminEmail,
            @Value("${BOOTSTRAP_ADMIN_PASSWORD}") String adminSenha) {
        this.usuarios=usuarios; this.administradores=administradores; this.senhas=senhas;
        this.donoEmail=donoEmail; this.donoSenha=donoSenha; this.adminEmail=adminEmail; this.adminSenha=adminSenha;
    }
    @Override
    @Transactional(transactionManager = "writeTransactionManager")
    public void run(ApplicationArguments args) {
        criarDono(); criarAdmin();
    }
    private void criarDono() {
        if (usuarios.existsByEmailIgnoreCase(donoEmail)) return;
        var agora=Instant.now();
        var dados=new DadosNovoUsuario(TipoPessoa.PF,"Dono",donoEmail,null,senhas.encode(donoSenha),
                null,null,null,null,null,PapelUsuario.DONO);
        usuarios.saveAndFlush(UsuarioEntity.novoCadastro(UUID.fromString("00000000-0000-0000-0000-00000000d001"),dados,agora));
    }
    private void criarAdmin() {
        if (usuarios.existsByEmailIgnoreCase(adminEmail)) return;
        var agora=Instant.now();
        UUID id=UUID.fromString("00000000-0000-0000-0000-00000000ad01");
        var dados=new DadosNovoUsuario(TipoPessoa.PF,"admin",adminEmail,"+5561999999999",senhas.encode(adminSenha),
                "52998224725",LocalDate.of(2004,1,1),null,null,null,PapelUsuario.ADMIN);
        usuarios.saveAndFlush(UsuarioEntity.novoCadastro(id,dados,agora));
        administradores.saveAndFlush(UsuarioAdminEntity.novo(id,TipoPessoa.PF,"52998224725",
                LocalDate.of(2004,1,1),null,null));
    }
}
