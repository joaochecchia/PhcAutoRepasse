package repasse.phcauto.backend.planos.internal.core;

public interface ListarPlanosUseCase { Resultado execute(int offset, int limite);
    record Resultado(java.util.List<PlanoDados> planos, long total, int offset, int limite) { }
}
