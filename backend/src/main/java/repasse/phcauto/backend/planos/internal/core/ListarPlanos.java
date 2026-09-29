package repasse.phcauto.backend.planos.internal.core;

public final class ListarPlanos implements ListarPlanosUseCase {
    private final PlanoGateway gateway;
    public ListarPlanos(PlanoGateway gateway) { this.gateway = gateway; }
    @Override public Resultado execute(int offset, int limite) {
        if (offset < 0) throw new PlanoInvalidoException("Offset não pode ser negativo");
        if (limite < 1 || limite > 100) throw new PlanoInvalidoException("Limite deve estar entre 1 e 100");
        return new Resultado(gateway.listar(offset, limite), gateway.contar(), offset, limite);
    }
}
