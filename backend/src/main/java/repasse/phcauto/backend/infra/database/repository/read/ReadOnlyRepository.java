package repasse.phcauto.backend.infra.database.repository.read;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
@Transactional(transactionManager = "readTransactionManager", readOnly = true)
public interface ReadOnlyRepository<T, ID> extends Repository<T, ID> {
    Optional<T> findById(ID id);
    boolean existsById(ID id);
    Page<T> findAll(Pageable pageable);
    long count();
}
