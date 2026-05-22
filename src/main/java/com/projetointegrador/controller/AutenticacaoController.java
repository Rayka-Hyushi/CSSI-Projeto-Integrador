package com.projetointegrador.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.projetointegrador.model.*;
import com.projetointegrador.repository.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller para gerenciar a autenticao e redirecionamento dos usurios aps o login e telas livres.
 */
@Controller
public class AutenticacaoController {

    @Autowired
    private BairroRepository bairroRepository;

    @Autowired
    private ServicoAdicionalRepository servicoAdicionalRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PrestadorRepository prestadorRepository;

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private SolicitacaoRepository solicitacaoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/")
    public String login(Authentication authentication) {
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
        model.addAttribute("bairros", bairroRepository.findAll());
        model.addAttribute("servicos", servicoAdicionalRepository.findAll());
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
            p.setSenha(passwordEncoder.encode(senha));
            p.setTipoUsuario(tipoUsuario);

            // Bairros
            if (bairrosIds != null && !bairrosIds.isEmpty()) {
                p.setBairros(bairroRepository.findAllById(bairrosIds));
            } else {
                p.setBairros(new ArrayList<>());
            }

            // Servicos
            if (servicosIds != null && !servicosIds.isEmpty()) {
                p.setServicos(servicoAdicionalRepository.findAllById(servicosIds));
            } else {
                p.setServicos(new ArrayList<>());
            }

            prestadorRepository.save(p);

            // Veiculo se existir informações
            if(placaVeiculo != null && !placaVeiculo.isBlank()){
                Veiculo v = new Veiculo();
                v.setPrestador(p);
                v.setPlaca(placaVeiculo);
                v.setTipo(TipoVeiculo.valueOf(tipoVeiculoStr));
                v.setCapacidadeCarga(capacidade != null ? capacidade : 0.0);
                v.setFechado("ABERTO".equalsIgnoreCase(abertoFechadoStr));
                veiculoRepository.save(v);
            }
            novoUsuario = p;

        } else {
            Cliente c = new Cliente();
            c.setStatusAprovacao(StatusAprovacao.PENDENTE); // Usuário cliente fica pendente para aprovação do admin
            c.setNomeCompleto(nomeCompleto);
            c.setEmail(email);
            c.setWhatsapp(whatsapp);
            c.setCpf(cpf);
            c.setSenha(passwordEncoder.encode(senha));
            c.setTipoUsuario(tipoUsuario);
            clienteRepository.save(c);

            novoUsuario = c;
        }

        // Criar a solicitação para o admin aprovar
        Solicitacao sol = new Solicitacao();
        sol.setTipo(TipoSolicitacao.CADASTRO);
        sol.setUsuario(novoUsuario);
        sol.setNome(novoUsuario.getNomeCompleto());
        sol.setEmail(novoUsuario.getEmail());
        sol.setWhatsapp(novoUsuario.getWhatsapp());
        sol.setCpf(novoUsuario.getCpf());
        sol.setStatus(StatusAprovacao.PENDENTE);

        String formDetalhes = "Nova conta tipo: " + tipoUsuario.name() + "\n";
        if(isPrestador) {
             formDetalhes += "Placa: " + placaVeiculo + "\n";
        }
        sol.setDetalhes(formDetalhes);
        solicitacaoRepository.save(sol);

        redirectAttributes.addFlashAttribute("sucesso", "Cadastro realizado com sucesso! Aguarde a aprovação da nossa equipe antes de tentar fazer login.");
        return "redirect:/"; // Volta pro login
    }
}
