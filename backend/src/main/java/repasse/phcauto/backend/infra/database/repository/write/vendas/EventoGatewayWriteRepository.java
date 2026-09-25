package repasse.phcauto.backend.infra.database.repository.write.vendas;

import repasse.phcauto.backend.infra.database.entity.vendas.EventoGatewayEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoGatewayWriteRepository extends JpaRepository<EventoGatewayEntity, Long> {
}
