package repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.persistence;
import java.util.*;
import java.time.Clock;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.*;
import jakarta.persistence.*;
import repasse.phcauto.backend.fipe.internal.core.domain.DadosReferencia;
import repasse.phcauto.backend.fipe.internal.core.gateway.ImportarLoteFipeGateway;
import repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.persistence.entity.FipeReferenciaEntity;
@Repository
@Transactional(transactionManager="writeTransactionManager",propagation=Propagation.MANDATORY)
public class JpaImportarLoteFipeGateway implements ImportarLoteFipeGateway {
    @PersistenceContext(unitName="write") private EntityManager em;
    private final Clock clock;
    public JpaImportarLoteFipeGateway(Clock clock){this.clock=clock;}
    public void salvar(UUID id,String fonte,List<DadosReferencia> lote){
        em.unwrap(org.hibernate.Session.class).setJdbcBatchSize(50);
        for(var d:lote)em.persist(FipeReferenciaEntity.criar(d,fonte,null,id,clock.instant()));
        em.flush(); // callbacks RowChanged participam da mesma transação do progresso.
    }
}
