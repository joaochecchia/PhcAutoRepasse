package repasse.phcauto.backend.fipe;
import java.util.UUID;
public interface FipeFacade {
    /** Consulta exclusivamente local, consistente com a escrita. Não realiza HTTP. */
    ReferenciaFipe buscarReferencia(UUID id);
}
