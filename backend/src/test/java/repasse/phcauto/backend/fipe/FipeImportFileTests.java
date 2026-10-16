package repasse.phcauto.backend.fipe;
import static org.assertj.core.api.Assertions.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.json.JsonMapper;
import repasse.phcauto.backend.fipe.internal.infrastructure.service.ArquivoImportacaoFipe;
class FipeImportFileTests {
    @TempDir Path dir;
    @Test void leLotesSemCarregarArquivoInteiroEPreservaUtf8()throws Exception{
        String linha="""
            {"codigoFipe":"014001-4","tipo":"CARRO","marca":"Citroën","modelo":"C3","anoModelo":2022,"combustivel":"Gasolina","mesReferencia":"2026-10-01","valorCentavos":7500000}
            """;
        var arquivo=dir.resolve("t.ndjson");Files.writeString(arquivo,linha.repeat(205));var json=JsonMapper.builder().findAndAddModules().build();
        var a=ArquivoImportacaoFipe.ler(arquivo,0,json);assertThat(a.dados()).hasSize(100);assertThat(a.fim()).isFalse();
        var b=ArquivoImportacaoFipe.ler(arquivo,a.posicao(),json);assertThat(b.dados()).hasSize(100);
        var c=ArquivoImportacaoFipe.ler(arquivo,b.posicao(),json);assertThat(c.dados()).hasSize(5);assertThat(c.fim()).isTrue();
        assertThat(c.dados().get(0).marca()).isEqualTo("Citroën");assertThat(c.posicao()).isEqualTo(Files.size(arquivo));
    }
    @Test void rejeitaLinhaGigante()throws Exception{
        var arquivo=dir.resolve("t.ndjson");Files.writeString(arquivo,"x".repeat(8193));
        assertThatThrownBy(()->ArquivoImportacaoFipe.ler(arquivo,0,JsonMapper.builder().findAndAddModules().build())).isInstanceOf(java.io.IOException.class);
    }
}
