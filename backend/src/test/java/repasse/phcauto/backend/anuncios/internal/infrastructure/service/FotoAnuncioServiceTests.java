package repasse.phcauto.backend.anuncios.internal.infrastructure.service;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import repasse.phcauto.backend.anuncios.internal.core.exception.AnuncioInvalidoException;

class FotoAnuncioServiceTests {
    static {
        System.setProperty("java.awt.headless", "true");
    }

    @Test
    void preservaImagemValidaQueJaEstaAbaixoDoAlvo() throws Exception {
        byte[] png = imagemPng(20, 20);

        var processada = FotoAnuncioService.processar(png, 5 * 1024 * 1024);

        assertArrayEquals(png, processada.conteudo());
        assertEquals(".png", processada.extensao());
    }

    @Test
    void tentaReduzirImagemAcimaDoAlvo() throws Exception {
        byte[] png = imagemPng(800, 800);
        long alvo = Math.max(100, png.length / 2L);

        var processada = FotoAnuncioService.processar(png, alvo);

        assertTrue(processada.conteudo().length <= alvo);
        assertNotNull(ImageIO.read(new java.io.ByteArrayInputStream(processada.conteudo())));
    }

    @Test
    void rejeitaConteudoQueNaoEhImagem() {
        assertThrows(AnuncioInvalidoException.class,
                () -> FotoAnuncioService.processar("nao-e-imagem".getBytes(), 5 * 1024 * 1024));
    }

    private static byte[] imagemPng(int largura, int altura) throws Exception {
        var imagem = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                imagem.setRGB(x, y, ((x * 31 + y * 17) << 8) ^ (x * y));
            }
        }
        var saida = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(imagem, "png", saida));
        return saida.toByteArray();
    }
}
