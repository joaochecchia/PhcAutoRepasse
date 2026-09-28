package repasse.phcauto.backend.anuncios;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import repasse.phcauto.backend.domain.model.catalogo.TipoPreco;

/** Campos ausentes permanecem inalterados. O tipo e o anunciante do veículo não podem ser trocados. */
public record AtualizarAnuncioRequest(
        @Size(min = 1, max = 100) String fabricante,
        @Size(min = 1, max = 120) String modelo,
        @Size(max = 180) String versao,
        @Positive Integer anoFabricacao,
        @Positive Integer anoModelo,
        @Size(max = 60) String cor,
        @Size(max = 80) String identificadorPublico,
        repasse.phcauto.backend.domain.model.catalogo.CondicaoVeiculo condicao,
        @Size(max = 60) String tipoFreio,
        @Size(min = 1, max = 180) String titulo,
        @Size(max = 10000) String descricao,
        TipoPreco tipoPreco,
        @Positive Long precoCentavos,
        Boolean aceitaTroca,
        Boolean publicarAgora,
        @Valid EnderecoAnuncioPatchRequest endereco,
        @Valid CriarAnuncioRequest.CarroRequest carro,
        @Valid CriarAnuncioRequest.MotoRequest moto,
        @Valid CriarAnuncioRequest.CaminhaoRequest caminhao,
        @Valid CriarAnuncioRequest.CaminhoneteRequest caminhonete,
        @Valid CriarAnuncioRequest.BarcoRequest barco,
        @Valid CriarAnuncioRequest.LinhaAmarelaRequest linhaAmarela) {


    @jakarta.validation.constraints.AssertTrue(message = "Informe no máximo um bloco de dados específicos")
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isApenasUmBlocoDeDetalhes() {
        return (carro != null ? 1 : 0) + (moto != null ? 1 : 0)
                + (caminhao != null ? 1 : 0) + (caminhonete != null ? 1 : 0)
                + (barco != null ? 1 : 0) + (linhaAmarela != null ? 1 : 0) <= 1;
    }

    @jakarta.validation.constraints.AssertTrue(message = "Informe ao menos um campo para atualizar")
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isPossuiAlgumaAlteracao() {
        return fabricante != null || modelo != null || versao != null || anoFabricacao != null
                || anoModelo != null || cor != null || identificadorPublico != null || condicao != null
                || tipoFreio != null || titulo != null || descricao != null || tipoPreco != null
                || precoCentavos != null || aceitaTroca != null || publicarAgora != null
                || possuiValor(endereco) || possuiValor(carro) || possuiValor(moto)
                || possuiValor(caminhao) || possuiValor(caminhonete)
                || possuiValor(barco) || possuiValor(linhaAmarela);
    }

    private static boolean possuiValor(Object registro) {
        if (registro == null) return false;
        try {
            for (var componente : registro.getClass().getRecordComponents()) {
                if (componente.getAccessor().invoke(registro) != null) return true;
            }
            return false;
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Não foi possível validar a atualização", ex);
        }
    }
}
