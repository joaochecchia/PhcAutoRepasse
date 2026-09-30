package repasse.phcauto.backend.planos.internal.core.domain;

public record PlanoPatch(String nome, Long valorCentavos, Integer periodoMeses,
        Integer limiteAnuncios, Integer limiteVistoriasCautelares, Boolean ativo) {
    public boolean vazio() {
        return nome == null && valorCentavos == null && periodoMeses == null
                && limiteAnuncios == null && limiteVistoriasCautelares == null && ativo == null;
    }
}
