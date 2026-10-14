package repasse.phcauto.backend.usuarios.internal.core.usecase;
import java.util.UUID;
import repasse.phcauto.backend.usuarios.internal.core.domain.PerfilPublico;
public interface ConsultarPerfilPublicoUseCase { PerfilPublico execute(UUID id); }
