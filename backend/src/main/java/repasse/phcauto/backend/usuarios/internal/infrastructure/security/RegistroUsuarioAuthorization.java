package repasse.phcauto.backend.usuarios.internal.infrastructure.security;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import repasse.phcauto.backend.domain.model.identidade.PapelUsuario;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.request.CriarUsuarioRequest;
@Component("registroUsuarioAuthorization")
public class RegistroUsuarioAuthorization {
    public boolean podeRegistrar(CriarUsuarioRequest request, Authentication authentication) {
        if (request == null) return false;
        var papel = request.papel() == null ? PapelUsuario.CLIENTE : request.papel();
        if (papel == PapelUsuario.CLIENTE) return true;
        if (papel != PapelUsuario.ADMIN && papel != PapelUsuario.DONO) return false;
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DONO"));
    }
}
