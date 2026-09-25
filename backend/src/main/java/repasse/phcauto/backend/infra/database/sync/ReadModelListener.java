package repasse.phcauto.backend.infra.database.sync;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class ReadModelListener {
    private final ReadModelProjector projector;

    public ReadModelListener(ReadModelProjector projector) { this.projector = projector; }

    // Usa o transaction manager primário (write) para o registro de eventos.
    // O projetor confirma sua transação no read antes do listener retornar com sucesso.
    @ApplicationModuleListener(id = "phcauto-read-model-v1")
    public void on(RowChanged event) { projector.synchronize(event); }
}
