package com.projetointegrador.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.factory.annotation.Autowired;

import com.projetointegrador.model.*;
import com.projetointegrador.service.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller para gerenciar a autenticao e redirecionamento dos usurios aps o login e telas livres.
 */
@Controller
public class AutenticacaoController {

    @Autowired
    private BairroService bairroService;

    @Autowired
    private ServicoAdicionalService servicoAdicionalService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private PrestadorService prestadorService;

    @Autowired
    private VeiculoService veiculoService;

    @Autowired
    private SolicitacaoService solicitacaoService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/")
    public String login(Authentication authentication) {
        // Se não houver nenhum usuário no sistema, redireciona para setup
        if (usuarioService.contar() == 0) {
            return "redirect:/setup";
        }

        if (authentication != null && authentication.isAuthenticated()) {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isPrestador = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_PRESTADOR"));
            boolean isCliente = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));

            if (isAdmin) {
                return "redirect:/admin/solicitacoes";
            }
            if (isPrestador) {
                return "redirect:/prestador/inicio";
            }
            if (isCliente) {
                return "redirect:/cliente/inicio";
            }
        }
        return "index";
    }

    @GetMapping("/cadastro")
    public String abrirCadastro(Model model) {
        model.addAttribute("bairros", bairroService.listarTodos());
        model.addAttribute("servicos", servicoAdicionalService.listarTodos());
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String realizarCadastro(
            @RequestParam("nomeCompleto") String nomeCompleto,
            @RequestParam("email") String email,
            @RequestParam("whatsapp") String whatsapp,
            @RequestParam("cpf") String cpf,
            @RequestParam("senha") String senha,
            @RequestParam("tipoUsuario") String tipoUsuarioStr,

            // Campos de Prestador
            @RequestParam(value = "placaVeiculo", required = false) String placaVeiculo,
            @RequestParam(value = "tipoVeiculo", required = false) String tipoVeiculoStr,
            @RequestParam(value = "capacidadeModel", required = false) Double capacidade,
            @RequestParam(value = "abertoFechado", required = false) String abertoFechadoStr,
            @RequestParam(value = "servicos", required = false) List<Long> servicosIds,
            @RequestParam(value = "bairros", required = false) List<Long> bairrosIds,

            RedirectAttributes redirectAttributes) {

        TipoUsuario tipoUsuario = TipoUsuario.valueOf(tipoUsuarioStr.toUpperCase());
        Usuario novoUsuario;
        boolean isPrestador = (tipoUsuario == TipoUsuario.ROLE_PRESTADOR);

        if (isPrestador) {
            Prestador p = new Prestador();
            p.setStatusAprovacao(StatusAprovacao.PENDENTE); // Usuário prestador fica pendente para aprovação do admin
            p.setNomeCompleto(nomeCompleto);
            p.setEmail(email);
            p.setWhatsapp(whatsapp);
            p.setCpf(cpf);
            p.setSenha(senha);
            p.setTipoUsuario(tipoUsuario);

            // Bairros
            if (bairrosIds != null && !bairrosIds.isEmpty()) {
                p.setBairros(bairroService.buscarPorIds(bairrosIds));
            } else {
                p.setBairros(new ArrayList<>());
            }

            // Servicos
            if (servicosIds != null && !servicosIds.isEmpty()) {
                p.setServicos(servicoAdicionalService.buscarPorIds(servicosIds));
            } else {
                p.setServicos(new ArrayList<>());
            }

            prestadorService.salvar(p);

            // Veiculo se existir informações
            if (placaVeiculo != null && !placaVeiculo.isBlank()) {
                Veiculo v = new Veiculo();
                v.setPrestador(p);
                v.setPlaca(placaVeiculo);
                v.setTipo(TipoVeiculo.valueOf(tipoVeiculoStr));
                v.setCapacidadeCarga(capacidade != null ? capacidade : 0.0);
                v.setFechado("ABERTO".equalsIgnoreCase(abertoFechadoStr));
                veiculoService.salvar(v);
            }
            novoUsuario = p;

        } else {
            Cliente c = new Cliente();
            c.setStatusAprovacao(StatusAprovacao.PENDENTE); // Usuário cliente fica pendente para aprovação do admin
            c.setNomeCompleto(nomeCompleto);
            c.setEmail(email);
            c.setWhatsapp(whatsapp);
            c.setCpf(cpf);
            c.setSenha(senha);
            c.setTipoUsuario(tipoUsuario);
            clienteService.salvar(c);

            novoUsuario = c;
        }

        // Criar a solicitação para o admin aprovar
        Solicitacao sol = new Solicitacao();
        sol.setTipoSolicitacao(TipoSolicitacao.CADASTRO);
        sol.setUsuario(novoUsuario);
        sol.setStatusSolicitacao(StatusAprovacao.PENDENTE);

        String formDetalhes = "Solicita-se cadastro de nova conta. \nTipo de conta: " + tipoUsuario.name();
        if (isPrestador) {
            formDetalhes += "\nPlaca: " + placaVeiculo;
	        formDetalhes += "\nTipo de Veículo: " + TipoVeiculo.valueOf(tipoVeiculoStr);
			formDetalhes += "\nCapacidade: " + capacidade;
	        formDetalhes += "\nCarroceria: " + abertoFechadoStr;
        }
        sol.setDetalhes(formDetalhes);
        solicitacaoService.criar(sol);

        redirectAttributes.addFlashAttribute("sucesso", "Cadastro realizado com sucesso! Aguarde a aprovação da nossa equipe antes de tentar fazer login.");
        return "redirect:/"; // Volta pro login
    }
}
