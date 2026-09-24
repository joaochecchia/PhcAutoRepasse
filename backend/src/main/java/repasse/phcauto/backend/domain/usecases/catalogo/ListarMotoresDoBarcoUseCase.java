package repasse.phcauto.backend.domain.usecases.catalogo;

import java.util.List;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.catalogo.MotorBarco;

/** Retorna os motores ordenados por posição; lista vazia quando não houver motores. */
public interface ListarMotoresDoBarcoUseCase {

    List<MotorBarco> executar(UUID barcoId);
}
