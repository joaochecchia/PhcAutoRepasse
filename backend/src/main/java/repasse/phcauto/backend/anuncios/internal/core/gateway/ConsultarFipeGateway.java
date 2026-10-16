package repasse.phcauto.backend.anuncios.internal.core.gateway;
import java.util.UUID;
import repasse.phcauto.backend.anuncios.internal.core.domain.ReferenciaFipeVeiculo;
public interface ConsultarFipeGateway { ReferenciaFipeVeiculo buscar(UUID id); }
