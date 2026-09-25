package repasse.phcauto.backend.domain.gateway.identidade;

import repasse.phcauto.backend.domain.event.identidade.UsuarioAlterado;

/** Publicar ao final da operação, na mesma transação de usuário, perfil e endereço. */
public interface PublicarEventoUsuarioGateway {
    void publicar(UsuarioAlterado evento);
}
