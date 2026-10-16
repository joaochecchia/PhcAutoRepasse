package repasse.phcauto.backend.fipe.internal.core.usecase;
import java.util.*;
import java.time.LocalDate;
import repasse.phcauto.backend.fipe.internal.core.domain.DadosReferencia;
public interface ImportarLoteFipeUseCase { void execute(UUID importacao,String fonte,LocalDate mes,List<DadosReferencia> lote); }
