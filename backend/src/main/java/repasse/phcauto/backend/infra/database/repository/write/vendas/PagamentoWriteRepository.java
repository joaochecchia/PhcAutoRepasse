package repasse.phcauto.backend.infra.database.repository.write.vendas;

import repasse.phcauto.backend.infra.database.entity.vendas.PagamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PagamentoWriteRepository extends JpaRepository<PagamentoEntity, UUID> {
}
