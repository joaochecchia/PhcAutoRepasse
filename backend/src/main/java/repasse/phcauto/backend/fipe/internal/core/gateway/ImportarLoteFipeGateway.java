package repasse.phcauto.backend.fipe.internal.core.gateway;
import java.util.*;
import repasse.phcauto.backend.fipe.internal.core.domain.DadosReferencia;
public interface ImportarLoteFipeGateway { void salvar(UUID importacao,String fonte,List<DadosReferencia> lote); }
