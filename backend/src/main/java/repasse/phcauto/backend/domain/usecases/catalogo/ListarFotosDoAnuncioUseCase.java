package repasse.phcauto.backend.domain.usecases.catalogo;

import java.util.List;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.catalogo.Foto;

/** Retorna as fotos ordenadas por posição; lista vazia quando não houver fotos. */
public interface ListarFotosDoAnuncioUseCase {

    List<Foto> executar(UUID anuncioId);
}
