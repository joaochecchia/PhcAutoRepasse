package repasse.phcauto.backend.infra.controller.mapper.identidade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;
import repasse.phcauto.backend.infra.controller.dto.identidade.CadastroPessoaFisicaRequest;
import repasse.phcauto.backend.infra.controller.dto.identidade.CadastroPessoaJuridicaRequest;
import repasse.phcauto.backend.infra.controller.dto.identidade.CompletarCadastroSocialPessoaFisicaRequest;
import repasse.phcauto.backend.infra.controller.dto.identidade.CompletarCadastroSocialPessoaJuridicaRequest;
import repasse.phcauto.backend.infra.controller.dto.identidade.DadosCadastroPessoaFisicaRequest;
import repasse.phcauto.backend.infra.controller.dto.identidade.DadosCadastroPessoaJuridicaRequest;
import repasse.phcauto.backend.infra.controller.dto.identidade.DadosCompraPessoaFisicaRequest;
import repasse.phcauto.backend.infra.controller.dto.identidade.DadosCompraPessoaJuridicaRequest;
import repasse.phcauto.backend.infra.controller.dto.identidade.EnderecoCadastroRequest;

class CadastroDtoMapperTests {
    private static final UUID USUARIO_ID = UUID.fromString("7f4c012c-37b3-4ceb-b9c8-c95de133342a");
    private static final Instant AGORA = Instant.parse("2026-09-24T12:00:00Z");
    private final CadastroDtoMapper mapper = new CadastroDtoMapper();

    @Test
    void converteCadastroPfEntreRequestDominioEResponseSemSenhaBruta() {
        var request = pessoaFisica();
        var usuario = mapper.paraUsuario(request, USUARIO_ID, "hash-seguro", AGORA);
        var perfil = mapper.paraPerfilPf(request, USUARIO_ID);
        var endereco = mapper.paraEndereco(request.getEndereco(), USUARIO_ID);
        var response = mapper.paraResponse(usuario, perfil, endereco);
        assertEquals(TipoPessoa.PF, usuario.getTipoPessoa());
        assertEquals("hash-seguro", usuario.getSenhaHash());
        assertEquals(request.getCpf(), perfil.getCpf());
        assertEquals(request.getNome(), response.getNome());
        assertEquals(request.getNumeroCelular(), response.getNumeroCelular());
        assertEquals(request.getEndereco().getCep(), response.getEndereco().getCep());
    }

    @Test
    void cadastroSocialPodeGerarDominioSemSenhaLocal() {
        assertNull(mapper.paraUsuario(pessoaFisica(), USUARIO_ID, null, AGORA).getSenhaHash());
    }

    @Test
    void converteCadastroPjEntreRequestDominioEResponse() {
        var request = pessoaJuridica();
        var usuario = mapper.paraUsuario(request, USUARIO_ID, "hash-seguro", AGORA);
        var perfil = mapper.paraPerfilPj(request, USUARIO_ID);
        var endereco = mapper.paraEndereco(request.getEndereco(), USUARIO_ID);
        var response = mapper.paraResponse(usuario, perfil, endereco);
        assertEquals(TipoPessoa.PJ, usuario.getTipoPessoa());
        assertEquals(request.getNomeEmpresa(), usuario.getNome());
        assertEquals(request.getNomeEmpresa(), perfil.getNomeFantasia());
        assertEquals(request.getRazaoSocial(), response.getRazaoSocial());
        assertEquals(request.getEmailContato(), response.getEmailContato());
    }

    @Test
    void converteTodosOsDadosComplementaresDaCompra() {
        var pfRequest = new DadosCompraPessoaFisicaRequest();
        pfRequest.setRg("123456789");
        pfRequest.setNomePai("Nome do pai");
        pfRequest.setNomeMae("Nome da mãe");
        pfRequest.setNaturalidade("São Paulo/SP");
        pfRequest.setGenero("Não informado");
        var pjRequest = new DadosCompraPessoaJuridicaRequest();
        pjRequest.setInscricaoEstadual("ISENTO");
        pjRequest.setRegimeTributario("Simples Nacional");
        var pf = mapper.paraDadosCompraPf(pfRequest, USUARIO_ID);
        var pj = mapper.paraDadosCompraPj(pjRequest, USUARIO_ID);
        assertEquals(pfRequest.getRg(), pf.getRg());
        assertEquals(pfRequest.getNomePai(), pf.getNomePai());
        assertEquals(pfRequest.getNomeMae(), pf.getNomeMae());
        assertEquals(pfRequest.getNaturalidade(), pf.getNaturalidade());
        assertEquals(pfRequest.getGenero(), pf.getGenero());
        assertEquals(pjRequest.getInscricaoEstadual(), pj.getInscricaoEstadual());
        assertEquals(pjRequest.getRegimeTributario(), pj.getRegimeTributario());
    }

    @Test
    void recusaMisturarPerfilOuEnderecoDeOutroUsuario() {
        var request = pessoaFisica();
        var usuario = mapper.paraUsuario(request, USUARIO_ID, "hash", AGORA);
        var outroId = UUID.randomUUID();
        var perfilOutro = mapper.paraPerfilPf(request, outroId);
        var enderecoOutro = mapper.paraEndereco(request.getEndereco(), outroId);
        assertThrows(IllegalArgumentException.class, () -> mapper.paraResponse(usuario, perfilOutro, null));
        var perfil = mapper.paraPerfilPf(request, USUARIO_ID);
        assertThrows(IllegalArgumentException.class, () -> mapper.paraResponse(usuario, perfil, enderecoOutro));
    }

    @Test
    void responseAceitaEnderecoAindaNaoPreenchidoParaCadastroLegado() {
        var request = pessoaFisica();
        var usuario = mapper.paraUsuario(request, USUARIO_ID, null, AGORA);
        var perfil = mapper.paraPerfilPf(request, USUARIO_ID);
        assertNull(mapper.paraResponse(usuario, perfil, null).getEndereco());
    }

    @Test
    void recusaRequestOuIdentificadoresNulos() {
        assertThrows(NullPointerException.class,
                () -> mapper.paraUsuario((DadosCadastroPessoaFisicaRequest) null, USUARIO_ID, null, AGORA));
        assertThrows(NullPointerException.class,
                () -> mapper.paraUsuario(pessoaFisica(), null, null, AGORA));
        assertThrows(NullPointerException.class,
                () -> mapper.paraUsuario(pessoaFisica(), USUARIO_ID, null, null));
    }

    @Test
    void converteOsQuatroRequestsDeNivelSuperior() {
        var pfLocal = new CadastroPessoaFisicaRequest();
        pfLocal.setDados(pessoaFisica());
        pfLocal.setSenha("senha-bruta");
        var pjLocal = new CadastroPessoaJuridicaRequest();
        pjLocal.setDados(pessoaJuridica());
        pjLocal.setSenha("senha-bruta");
        var pfSocial = new CompletarCadastroSocialPessoaFisicaRequest();
        pfSocial.setDados(pessoaFisica());
        var pjSocial = new CompletarCadastroSocialPessoaJuridicaRequest();
        pjSocial.setDados(pessoaJuridica());

        assertEquals("hash", mapper.paraUsuario(pfLocal, USUARIO_ID, "hash", AGORA).getSenhaHash());
        assertEquals("hash", mapper.paraUsuario(pjLocal, USUARIO_ID, "hash", AGORA).getSenhaHash());
        assertNull(mapper.paraUsuario(pfSocial, USUARIO_ID, AGORA).getSenhaHash());
        assertNull(mapper.paraUsuario(pjSocial, USUARIO_ID, AGORA).getSenhaHash());
    }

    private static DadosCadastroPessoaFisicaRequest pessoaFisica() {
        var request = new DadosCadastroPessoaFisicaRequest();
        request.setCpf("52998224725");
        request.setNome("Cliente de Teste");
        request.setDataNascimento(LocalDate.of(1990, 1, 1));
        request.setEmail("cliente@example.com");
        request.setNumeroCelular("+5511999999999");
        request.setEndereco(endereco());
        return request;
    }

    private static DadosCadastroPessoaJuridicaRequest pessoaJuridica() {
        var request = new DadosCadastroPessoaJuridicaRequest();
        request.setNomeEmpresa("PHC Veículos");
        request.setCnpj("11222333000181");
        request.setRazaoSocial("PHC Veículos Ltda.");
        request.setNumeroContato("+551133334444");
        request.setEmailContato("contato@example.com");
        request.setEndereco(endereco());
        return request;
    }

    private static EnderecoCadastroRequest endereco() {
        var request = new EnderecoCadastroRequest();
        request.setCep("01001000");
        request.setCidade("São Paulo");
        request.setBairro("Sé");
        request.setRua("Praça da Sé");
        request.setNumero("10");
        request.setComplemento("Sala 2");
        request.setUf("SP");
        return request;
    }
}
