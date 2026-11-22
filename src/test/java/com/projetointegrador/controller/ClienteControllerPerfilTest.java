package com.projetointegrador.controller;

import com.projetointegrador.model.Bairro;
import com.projetointegrador.model.Prestador;
import com.projetointegrador.model.ServicoAdicional;
import com.projetointegrador.model.StatusAprovacao;
import com.projetointegrador.model.TipoVeiculo;
import com.projetointegrador.model.Veiculo;
import com.projetointegrador.service.ClienteService;
import com.projetointegrador.service.PrestadorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteControllerPerfilTest {

    @Mock
    private PrestadorService prestadorService;

    @Mock
    private ClienteService clienteService;

    @InjectMocks
    private ClienteController controller;

    @Test
    void perfilPublicoContemDadosNaoSensiveis() {
        when(prestadorService.buscarPorId(1L)).thenReturn(Optional.of(criarPrestador(StatusAprovacao.APROVADO)));

        Model model = new ExtendedModelMap();
        String view = controller.verPerfilPrestador(1L, null, null, model, new RedirectAttributesModelMap());

        assertThat(view).isEqualTo("cliente/perfil-prestador");
        ClienteController.PerfilPrestadorPublico perfil =
                (ClienteController.PerfilPrestadorPublico) model.asMap().get("prestador");

        assertThat(perfil.getNomeCompleto()).isEqualTo("João da Silva");
        assertThat(perfil.getEmail()).isEqualTo("joao@example.com");
        assertThat(perfil.getWhatsapp()).isEqualTo("(55) 99999-9999");
        assertThat(perfil.getRecomendacoes()).isEqualTo(5);
        assertThat(perfil.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 10, 8, 30));
        assertThat(perfil.getVeiculos()).singleElement().satisfies(veiculo -> {
            assertThat(veiculo.getTipo()).isEqualTo("Caminhão Urbano de Carga");
            assertThat(veiculo.getCapacidadeCarga()).isEqualTo(1500.0);
            assertThat(veiculo.getPlaca()).isEqualTo("ABC1D23");
            assertThat(veiculo.isFechado()).isTrue();
        });
        assertThat(perfil.getBairros()).containsExactly("Centro");
        assertThat(perfil.getServicos()).containsExactly("Empilhadeira");
        assertThat(nomesDosCampos(perfil.getClass())).doesNotContain("cpf", "cnpj", "senha");
        assertThat(nomesDosCampos(perfil.getVeiculos().get(0).getClass())).doesNotContain("prestador");
    }

    @Test
    void templateDoPerfilNaoExibeDadosSensiveis() {
        when(prestadorService.buscarPorId(1L)).thenReturn(Optional.of(criarPrestador(StatusAprovacao.APROVADO)));

        Model model = new ExtendedModelMap();
        controller.verPerfilPrestador(1L, null, null, model, new RedirectAttributesModelMap());

        String html = renderizar("cliente/perfil-prestador", model.asMap());

        assertThat(html).contains("João da Silva", "joao@example.com", "(55) 99999-9999", "ABC1D23", "Centro", "Empilhadeira");
        assertThat(html).doesNotContain("000.000.000-00", "00.000.000/0000-00", "hash-bcrypt");
    }

    @Test
    void cardsDosPrestadoresOferecemAberturaDoPerfil() {
        Prestador prestador = criarPrestador(StatusAprovacao.APROVADO);

        assertThat(renderizar("cliente/favoritos", Map.of("favoritos", List.of(prestador))))
                .contains("/cliente/prestador/1", "Ver perfil completo");

        Map<String, Object> modelInicio = new HashMap<>();
        modelInicio.put("prestadores", List.of(prestador));
        modelInicio.put("totalPrestadores", 1);
        modelInicio.put("buscaRealizada", false);
        modelInicio.put("bairros", List.of());
        modelInicio.put("servicos", List.of());
        modelInicio.put("servicosSelecionados", List.of());
        modelInicio.put("favoritosIds", List.of());

        assertThat(renderizar("cliente/inicio", modelInicio))
                .contains("/cliente/prestador/1", "Ver perfil completo");
    }

    @Test
    void prestadorNaoAprovadoNaoPossuiPerfilPublico() {
        when(prestadorService.buscarPorId(2L)).thenReturn(Optional.of(criarPrestador(StatusAprovacao.PENDENTE)));

        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        String view = controller.verPerfilPrestador(2L, null, null, new ExtendedModelMap(), redirectAttributes);

        assertThat(view).isEqualTo("redirect:/cliente/inicio");
        assertThat(redirectAttributes.getFlashAttributes()).containsKey("erro");
    }

    private Prestador criarPrestador(StatusAprovacao status) {
        Prestador prestador = new Prestador();
        prestador.setId(1L);
        prestador.setNomeCompleto("João da Silva");
        prestador.setEmail("joao@example.com");
        prestador.setSenha("hash-bcrypt");
        prestador.setWhatsapp("(55) 99999-9999");
        prestador.setCpf("000.000.000-00");
        prestador.setCnpj("00.000.000/0000-00");
        prestador.setStatusAprovacao(status);
        prestador.setStatusOnline(true);
        prestador.setRecomendacoes(5);
        prestador.setCreatedAt(LocalDateTime.of(2024, 1, 10, 8, 30));

        Veiculo veiculo = new Veiculo();
        veiculo.setTipo(TipoVeiculo.VUC);
        veiculo.setCapacidadeCarga(1500.0);
        veiculo.setPlaca("ABC1D23");
        veiculo.setFechado(true);
        prestador.setVeiculos(List.of(veiculo));

        Bairro bairro = new Bairro();
        bairro.setNomeBairro("Centro");
        prestador.setBairros(List.of(bairro));

        ServicoAdicional servico = new ServicoAdicional();
        servico.setNomeServico("Empilhadeira");
        prestador.setServicos(List.of(servico));

        return prestador;
    }

    private List<String> nomesDosCampos(Class<?> tipo) {
        return Arrays.stream(tipo.getDeclaredFields()).map(Field::getName).toList();
    }

    private String renderizar(String template, Map<String, Object> model) {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");

        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);

        JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(new MockServletContext());
        IWebExchange exchange = application.buildExchange(new MockHttpServletRequest(), new MockHttpServletResponse());
        WebContext context = new WebContext(exchange, Locale.getDefault(), model);
        return engine.process(template, context);
    }
}
