package repasse.phcauto.backend.fipe.internal.core.usecase;
import java.util.List;
import repasse.phcauto.backend.fipe.ReferenciaFipe;
import repasse.phcauto.backend.fipe.internal.core.domain.PesquisaFipe;
public interface PesquisarFipeUseCase { List<ReferenciaFipe> execute(PesquisaFipe pesquisa); }
