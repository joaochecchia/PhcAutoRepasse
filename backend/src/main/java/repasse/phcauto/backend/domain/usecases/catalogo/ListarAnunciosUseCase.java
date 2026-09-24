package repasse.phcauto.backend.domain.usecases.catalogo;

import java.util.List;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.catalogo.Anuncio;
import repasse.phcauto.backend.domain.model.catalogo.StatusAnuncio;

/** Consulta paginada. Filtros nulos não restringem resultados. Offset >= 0 e limite > 0. A implementação deve usar ordenação estável e aplicar as permissões de visibilidade. */
public interface ListarAnunciosUseCase {

    List<Anuncio> executar(UUID anuncianteId, StatusAnuncio status, int offset, int limite);
}
