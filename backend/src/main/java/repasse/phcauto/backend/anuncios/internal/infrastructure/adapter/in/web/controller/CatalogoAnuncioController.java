package repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.in.web.controller;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import repasse.phcauto.backend.anuncios.internal.core.domain.CatalogoReferenciaVeicular;

@RestController
@RequestMapping("/api/v1/anuncios/opcoes-cadastro")
public class CatalogoAnuncioController {
    public record OpcaoResponse(String valor, List<String> aliases) { }
    public record CatalogoResponse(List<OpcaoResponse> fabricantes, Map<String, List<String>> modelosPorFabricante,
            List<String> cores, List<String> carrocerias, List<String> cambios, List<String> combustiveis,
            List<String> motorizacoes, List<String> tiposFreio, List<String> tracoes, List<String> direcoes,
            List<OpcaoResponse> fabricantesMotos, Map<String, List<String>> modelosMotosPorFabricante,
            List<String> categoriasMotos, List<String> partidasMotos, List<String> refrigeracoesMotos,
            List<String> cambiosMotos, List<String> tiposFreioMotos,
            List<OpcaoResponse> fabricantesCaminhoes, Map<String, List<String>> modelosCaminhoesPorFabricante,
            List<String> configuracoesCaminhao, List<String> carroceriasCaminhao,
            List<String> cambiosCaminhao, List<String> tracoesCaminhao, List<String> direcoesCaminhao,
            List<String> tiposFreioCaminhao, List<String> implementosCaminhao) { }

    @GetMapping
    public CatalogoResponse consultar() {
        return new CatalogoResponse(
                CatalogoReferenciaVeicular.fabricantes().stream()
                        .map(item -> new OpcaoResponse(item.valor(), item.aliases())).toList(),
                CatalogoReferenciaVeicular.modelosPorFabricante(),
                CatalogoReferenciaVeicular.CORES, CatalogoReferenciaVeicular.CARROCERIAS,
                CatalogoReferenciaVeicular.CAMBIOS, CatalogoReferenciaVeicular.COMBUSTIVEIS,
                CatalogoReferenciaVeicular.MOTORIZACOES, CatalogoReferenciaVeicular.TIPOS_FREIO,
                CatalogoReferenciaVeicular.TRACOES, CatalogoReferenciaVeicular.DIRECOES,
                CatalogoReferenciaVeicular.fabricantesMotos().stream()
                        .map(item -> new OpcaoResponse(item.valor(), item.aliases())).toList(),
                CatalogoReferenciaVeicular.modelosMotosPorFabricante(),
                CatalogoReferenciaVeicular.CATEGORIAS_MOTO, CatalogoReferenciaVeicular.PARTIDAS_MOTO,
                CatalogoReferenciaVeicular.REFRIGERACOES_MOTO, CatalogoReferenciaVeicular.CAMBIOS_MOTO,
                CatalogoReferenciaVeicular.TIPOS_FREIO_MOTO,
                CatalogoReferenciaVeicular.fabricantesCaminhoes().stream()
                        .map(item -> new OpcaoResponse(item.valor(), item.aliases())).toList(),
                CatalogoReferenciaVeicular.modelosCaminhoesPorFabricante(),
                CatalogoReferenciaVeicular.CONFIGURACOES_CAMINHAO,
                CatalogoReferenciaVeicular.CARROCERIAS_CAMINHAO,
                CatalogoReferenciaVeicular.CAMBIOS_CAMINHAO,
                CatalogoReferenciaVeicular.TRACOES_CAMINHAO,
                CatalogoReferenciaVeicular.DIRECOES_CAMINHAO,
                CatalogoReferenciaVeicular.TIPOS_FREIO_CAMINHAO,
                CatalogoReferenciaVeicular.IMPLEMENTOS_CAMINHAO);
    }
}
