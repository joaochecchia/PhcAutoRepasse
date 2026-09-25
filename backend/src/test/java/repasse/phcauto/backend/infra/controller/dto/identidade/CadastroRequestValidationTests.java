package repasse.phcauto.backend.infra.controller.dto.identidade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

class CadastroRequestValidationTests {
    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();

    @AfterAll
    static void fecharValidator() { FACTORY.close(); }

    @Test
    void cadastroLocalPfExigeSenhaEDadosCompletos() {
        var cadastro = new CadastroPessoaFisicaRequest();
        cadastro.setDados(pessoaFisicaValida());
        assertTrue(camposInvalidos(cadastro).contains("senha"));
        cadastro.setSenha("senha-forte");
        assertTrue(VALIDATOR.validate(cadastro).isEmpty());
    }

    @Test
    void cadastroSocialPfNaoExigeSenhaMasExigeDadosCadastrais() {
        var cadastro = new CompletarCadastroSocialPessoaFisicaRequest();
        cadastro.setDados(pessoaFisicaValida());
        assertTrue(VALIDATOR.validate(cadastro).isEmpty());
        cadastro.getDados().setCpf(null);
        assertTrue(camposInvalidos(cadastro).contains("dados.cpf"));
    }

    @Test
    void cadastroSocialPjNaoExigeSenhaMasExigeDadosCadastrais() {
        var cadastro = new CompletarCadastroSocialPessoaJuridicaRequest();
        cadastro.setDados(pessoaJuridicaValida());
        assertTrue(VALIDATOR.validate(cadastro).isEmpty());
        cadastro.getDados().setCnpj(null);
        assertTrue(camposInvalidos(cadastro).contains("dados.cnpj"));
    }

    @Test
    void cadastroPjExigeEmpresaContatoEnderecoESenhaLocal() {
        var dados = pessoaJuridicaValida();
        var cadastro = new CadastroPessoaJuridicaRequest();
        cadastro.setDados(dados);
        cadastro.setSenha("senha-forte");
        assertTrue(VALIDATOR.validate(cadastro).isEmpty());
        dados.setNomeEmpresa(" ");
        dados.setNumeroContato(null);
        dados.getEndereco().setCidade("");
        var campos = camposInvalidos(cadastro);
        assertTrue(campos.contains("dados.nomeEmpresa"));
        assertTrue(campos.contains("dados.numeroContato"));
        assertTrue(campos.contains("dados.endereco.cidade"));
    }

    @Test
    void complementoENumeroDoEnderecoSaoOpcionais() {
        var endereco = enderecoValido();
        assertTrue(VALIDATOR.validate(endereco).isEmpty());
    }

    @Test
    void dadosDaCompraSaoObrigatoriosPorTipoDePessoa() {
        var pf = new DadosCompraPessoaFisicaRequest();
        var pj = new DadosCompraPessoaJuridicaRequest();
        assertFalse(VALIDATOR.validate(pf).isEmpty());
        assertFalse(VALIDATOR.validate(pj).isEmpty());
        pf.setRg("123456789");
        pf.setNomePai("Nome do pai");
        pf.setNomeMae("Nome da mãe");
        pf.setNaturalidade("São Paulo/SP");
        pf.setGenero("Não informado");
        pj.setInscricaoEstadual("ISENTO");
        pj.setRegimeTributario("Simples Nacional");
        assertTrue(VALIDATOR.validate(pf).isEmpty());
        assertTrue(VALIDATOR.validate(pj).isEmpty());
    }

    @Test
    void responsesDeUsuarioNuncaTransportamSenha() {
        assertSemSenha(PessoaFisicaResponse.class);
        assertSemSenha(PessoaJuridicaResponse.class);
    }

    @Test
    void senhaExisteSomenteNoRequestLocalEComoSomenteEscrita() throws Exception {
        assertEquals(JsonProperty.Access.WRITE_ONLY,
                CadastroPessoaFisicaRequest.class.getDeclaredField("senha")
                        .getAnnotation(JsonProperty.class).access());
        assertEquals(JsonProperty.Access.WRITE_ONLY,
                CadastroPessoaJuridicaRequest.class.getDeclaredField("senha")
                        .getAnnotation(JsonProperty.class).access());
    }

    private static void assertSemSenha(Class<?> responseType) {
        assertTrue(Arrays.stream(responseType.getDeclaredFields())
                .noneMatch(field -> field.getName().toLowerCase().contains("senha")));
        assertTrue(Arrays.stream(responseType.getMethods())
                .noneMatch(method -> method.getName().toLowerCase().contains("senha")));
    }

    private static DadosCadastroPessoaFisicaRequest pessoaFisicaValida() {
        var dados = new DadosCadastroPessoaFisicaRequest();
        dados.setCpf("52998224725");
        dados.setNome("Cliente de Teste");
        dados.setDataNascimento(LocalDate.of(1990, 1, 1));
        dados.setEmail("cliente@example.com");
        dados.setNumeroCelular("+5511999999999");
        dados.setEndereco(enderecoValido());
        return dados;
    }

    private static DadosCadastroPessoaJuridicaRequest pessoaJuridicaValida() {
        var dados = new DadosCadastroPessoaJuridicaRequest();
        dados.setNomeEmpresa("PHC Veículos");
        dados.setCnpj("11222333000181");
        dados.setRazaoSocial("PHC Veículos Ltda.");
        dados.setNumeroContato("+551133334444");
        dados.setEmailContato("contato@example.com");
        dados.setEndereco(enderecoValido());
        return dados;
    }

    private static EnderecoCadastroRequest enderecoValido() {
        var endereco = new EnderecoCadastroRequest();
        endereco.setCep("01001000");
        endereco.setCidade("São Paulo");
        endereco.setBairro("Sé");
        endereco.setRua("Praça da Sé");
        endereco.setUf("SP");
        return endereco;
    }

    private static Set<String> camposInvalidos(Object value) {
        return VALIDATOR.validate(value).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }
}
