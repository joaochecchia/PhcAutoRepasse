package repasse.phcauto.backend.fipe;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.http.ParallelumClient;
class FipeParallelumTests {
    @Test void convertePrecoMesEZeroKmSemUsarPrecoDoCliente(){
        var json=JsonMapper.builder().findAndAddModules().build();
        var n=json.readTree("""
            {"vehicleType":1,"codeFipe":"014001-4","brand":"Honda","model":"Civic EXL Flex",
            "modelYear":32000,"fuel":"Gasolina","price":"R$ 123.456,78","referenceMonth":"outubro de 2026"}
            """);
        var r=ParallelumClient.converter(n);
        assertThat(r.valorCentavos()).isEqualTo(12345678);assertThat(r.mesReferencia()).hasToString("2026-10-01");assertThat(r.zeroKm()).isTrue();
    }
    @Test void rejeitaRespostaSemDados(){
        assertThatThrownBy(()->ParallelumClient.converter(JsonMapper.builder().build().readTree("{}"))).isInstanceOf(IllegalArgumentException.class);
    }
}
