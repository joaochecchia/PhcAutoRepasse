package repasse.phcauto.backend.infra.database.sync;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import org.springframework.cache.CacheManager;

@Component
public class ReadModelListener {
    private final ReadModelProjector projector;
    private final CacheManager cacheManager;

    public ReadModelListener(ReadModelProjector projector, CacheManager cacheManager) {
        this.projector = projector;
        this.cacheManager = cacheManager;
    }

    // Usa o transaction manager primário (write) para o registro de eventos.
    // O projetor confirma sua transação no read antes do listener retornar com sucesso.
    @ApplicationModuleListener(id = "phcauto-read-model-v1")
    public void on(RowChanged event) {
        projector.synchronize(event);
        if (afetaPaginaInicial(event.table())) {
            var cache = cacheManager.getCache("anunciosPaginaInicial");
            if (cache != null) cache.clear();
        }
    }

    private static boolean afetaPaginaInicial(ProjectionTable table) {
        return switch (table) {
            case ANUNCIOS, VEICULOS, ENDERECOS_ANUNCIO, CARROS, MOTOS, CAMINHOES,
                    CAMINHONETES, BARCOS, LINHA_AMARELA, FOTOS, USUARIOS, USUARIOS_PJ -> true;
            default -> false;
        };
    }
}
