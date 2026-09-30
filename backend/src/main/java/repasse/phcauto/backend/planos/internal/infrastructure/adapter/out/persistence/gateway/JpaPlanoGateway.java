package repasse.phcauto.backend.planos.internal.infrastructure.adapter.out.persistence.gateway;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import repasse.phcauto.backend.planos.internal.core.domain.NovoPlano;
import repasse.phcauto.backend.planos.internal.core.domain.PlanoDados;
import repasse.phcauto.backend.planos.internal.core.gateway.PlanoGateway;
import repasse.phcauto.backend.planos.internal.infrastructure.adapter.out.persistence.entity.PlanoEntity;
import repasse.phcauto.backend.planos.internal.infrastructure.adapter.out.persistence.repository.write.PlanoWriteRepository;

@Component
@Transactional(transactionManager = "writeTransactionManager", propagation = Propagation.MANDATORY)
class JpaPlanoGateway implements PlanoGateway {
    private final PlanoWriteRepository repository;
    @PersistenceContext(unitName = "write")
    private EntityManager entityManager;

    JpaPlanoGateway(PlanoWriteRepository repository) { this.repository = repository; }

    @Override public PlanoDados criar(UUID id, NovoPlano plano, Instant criadoEm) {
        return repository.save(PlanoEntity.criar(id, plano, criadoEm)).snapshot();
    }
    @Override public Optional<PlanoDados> buscar(UUID id) {
        return repository.findById(id).map(PlanoEntity::snapshot);
    }
    @Override public List<PlanoDados> listar(int offset, int limite) {
        return entityManager.createQuery("select p from PlanoEntity p order by p.criadoEm desc, p.id", PlanoEntity.class)
                .setFirstResult(offset).setMaxResults(limite).getResultList().stream()
                .map(PlanoEntity::snapshot).toList();
    }
    @Override public long contar() { return repository.count(); }
    @Override public PlanoDados atualizar(UUID id, PlanoDados plano) {
        var entity = repository.findById(id).orElseThrow();
        entity.atualizar(plano);
        return entity.snapshot();
    }
    @Override public boolean excluir(UUID id) {
        var entity = repository.findById(id).orElse(null);
        if (entity == null) return false;
        repository.delete(entity);
        repository.flush();
        return true;
    }
    @Override public boolean existeNome(String nome, UUID ignorarId) {
        return ignorarId == null ? repository.existsByNome(nome) : repository.existsByNomeAndIdNot(nome, ignorarId);
    }
}
