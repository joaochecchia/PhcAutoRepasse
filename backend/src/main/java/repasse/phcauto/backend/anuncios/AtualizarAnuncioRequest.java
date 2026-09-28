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
        @Valid CriarAnuncioRequest.LinhaAmarelaRequest linhaAmarela) { }
