package repasse.phcauto.backend.usuarios.internal.infrastructure.service;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import repasse.phcauto.backend.usuarios.internal.core.exception.CadastroInvalidoException;
import static org.junit.jupiter.api.Assertions.*;
class FotoPerfilServiceTests {
    @Test void rejeitaConteudoFalsoMesmoComMimeDeImagem() {
        var arquivo = new MockMultipartFile("arquivo", "foto.jpg", "image/jpeg", "<script>alert(1)</script>".getBytes());
        assertThrows(CadastroInvalidoException.class, () -> FotoPerfilService.converter(arquivo));
    }
    @Test void limitaTamanhoAntesDaDecodificacao() {
        var arquivo = new MockMultipartFile("arquivo", "foto.jpg", "image/jpeg", new byte[5*1024*1024+1]);
        assertThrows(CadastroInvalidoException.class, () -> FotoPerfilService.converter(arquivo));
    }
    @Test void convertePngParaJpegERedimensiona() throws Exception {
        var original = new BufferedImage(1600, 800, BufferedImage.TYPE_INT_ARGB);
        var buffer = new ByteArrayOutputStream(); ImageIO.write(original, "png", buffer);
        var jpeg = FotoPerfilService.converter(new MockMultipartFile("arquivo", "foto.png", "image/png", buffer.toByteArray()));
        assertEquals(0xff, jpeg[0] & 0xff); assertEquals(0xd8, jpeg[1] & 0xff);
        var resultado = ImageIO.read(new java.io.ByteArrayInputStream(jpeg));
        assertEquals(800, resultado.getWidth()); assertEquals(400, resultado.getHeight());
    }
}
