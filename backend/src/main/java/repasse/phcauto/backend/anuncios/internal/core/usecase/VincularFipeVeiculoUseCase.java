package repasse.phcauto.backend.anuncios.internal.core.usecase;
import java.util.UUID;
import repasse.phcauto.backend.anuncios.internal.core.domain.CriarAnuncioCommand;
public interface VincularFipeVeiculoUseCase { CriarAnuncioCommand execute(CriarAnuncioCommand veiculo, UUID referenciaId); }
