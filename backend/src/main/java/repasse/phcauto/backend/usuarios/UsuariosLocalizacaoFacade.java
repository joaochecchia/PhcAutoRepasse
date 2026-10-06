package repasse.phcauto.backend.usuarios;

import java.util.UUID;
import repasse.phcauto.backend.localizacao.Coordenadas;

public interface UsuariosLocalizacaoFacade {
    Coordenadas buscarEnderecoCadastrado(UUID usuarioId);
}
