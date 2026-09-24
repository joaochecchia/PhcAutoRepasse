package repasse.phcauto.backend.domain.usecases.catalogo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.catalogo.MotorBarco;
import repasse.phcauto.backend.domain.usecases.crud.AtualizarUseCase;
import repasse.phcauto.backend.domain.usecases.crud.BuscarPorIdUseCase;
import repasse.phcauto.backend.domain.usecases.crud.CriarUseCase;
import repasse.phcauto.backend.domain.usecases.crud.ExcluirUseCase;
import repasse.phcauto.backend.domain.usecases.crud.ListarUseCase;

/**
 * Base abstrata das operações CRUD de MotorBarco.
 * Não contém persistência nem regras concretas; subclasses cumprem os contratos das interfaces.
 * Consumidores devem depender apenas da interface da operação de que necessitam.
 */
public abstract class AbstractMotorBarcoCrudUseCase implements
        CriarUseCase<MotorBarco>,
        BuscarPorIdUseCase<MotorBarco, UUID>,
        AtualizarUseCase<MotorBarco, UUID>,
        ExcluirUseCase<UUID>,
        ListarUseCase<MotorBarco> {

    @Override
    public abstract MotorBarco criar(MotorBarco dados);

    @Override
    public abstract Optional<MotorBarco> buscarPorId(UUID id);

    @Override
    public abstract MotorBarco atualizar(UUID id, MotorBarco dados);

    @Override
    public abstract boolean excluirPorId(UUID id);

    @Override
    public abstract List<MotorBarco> listar(int offset, int limite);
}
