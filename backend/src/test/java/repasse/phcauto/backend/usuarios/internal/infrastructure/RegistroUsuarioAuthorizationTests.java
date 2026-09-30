package repasse.phcauto.backend.usuarios.internal.infrastructure;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import repasse.phcauto.backend.domain.model.identidade.*;
import repasse.phcauto.backend.usuarios.CriarUsuarioRequest;
class RegistroUsuarioAuthorizationTests {
    private final RegistroUsuarioAuthorization regra=new RegistroUsuarioAuthorization();
    @Test void clientePermanecePublicoMasPrivilegiadosExigemDono() {
        assertThat(regra.podeRegistrar(request(null),null)).isTrue();
        assertThat(regra.podeRegistrar(request(PapelUsuario.ADMIN),null)).isFalse();
        assertThat(regra.podeRegistrar(request(PapelUsuario.DONO),auth("ROLE_ADMIN"))).isFalse();
        assertThat(regra.podeRegistrar(request(PapelUsuario.ADMIN),auth("ROLE_DONO"))).isTrue();
        assertThat(regra.podeRegistrar(request(PapelUsuario.DONO),auth("ROLE_DONO"))).isTrue();
    }
    private CriarUsuarioRequest request(PapelUsuario papel){return new CriarUsuarioRequest(null,papel,"Nome","a@b.com",null,"senha123",null,null,null,null,null,null,null,null,null);}
    private Authentication auth(String role){var a=mock(Authentication.class);when(a.getAuthorities()).thenAnswer(i->List.of(new SimpleGrantedAuthority(role)));return a;}
}
