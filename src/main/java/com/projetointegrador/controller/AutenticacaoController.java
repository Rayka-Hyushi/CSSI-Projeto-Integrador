package com.projetointegrador.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.projetointegrador.model.*;
import com.projetointegrador.service.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.security.SecureRandom;

/**
 * Controller para gerenciar a autenticao e redirecionamento dos usurios aps o
 * login e telas livres.
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
    @Transactional(rollbackFor = Exception.class)
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
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            TipoUsuario tipoUsuario = TipoUsuario.valueOf(tipoUsuarioStr.toUpperCase());
            Usuario novoUsuario;
            boolean isPrestador = (tipoUsuario == TipoUsuario.ROLE_PRESTADOR);

            // Validações prévias para capturar erros dentro da transação antes de comitar
            if (usuarioService.existePorEmail(email)) {
                throw new IllegalArgumentException("Este endereço de e-mail já está cadastrado em nosso sistema.");
            }
            if (usuarioService.existePorCpf(cpf)) {
                throw new IllegalArgumentException("Este CPF já está cadastrado em nosso sistema.");
            }

            // Validação prévia de placa duplicada ANTES de persistir qualquer entidade
            if (isPrestador && placaVeiculo != null && !placaVeiculo.isBlank()) {
                if (veiculoService.existePorPlaca(placaVeiculo)) {
                    throw new IllegalArgumentException("Placa duplicada");
                }
            }

            if (isPrestador) {
                // Validação de campos obrigatórios para Prestador
                if (placaVeiculo == null || placaVeiculo.isBlank()) {
                    throw new IllegalArgumentException("A placa do veículo é obrigatória.");
                }
                if (tipoVeiculoStr == null || tipoVeiculoStr.isBlank()) {
                    throw new IllegalArgumentException("O tipo de veículo é obrigatório.");
                }
                if (capacidade == null || capacidade <= 0) {
                    throw new IllegalArgumentException("A capacidade de carga deve ser maior que zero.");
                }
                if (bairrosIds == null || bairrosIds.isEmpty()) {
                    throw new IllegalArgumentException("Selecione pelo menos um bairro atendido.");
                }

                Prestador p = new Prestador();
                p.setStatusAprovacao(StatusAprovacao.PENDENTE);
                p.setNomeCompleto(nomeCompleto);
                p.setEmail(email);
                p.setWhatsapp(whatsapp);
                p.setCpf(cpf);
                p.setSenha(senha);
                p.setTipoUsuario(tipoUsuario);

                p.setBairros(bairroService.buscarPorIds(bairrosIds));

                if (servicosIds != null && !servicosIds.isEmpty()) {
                    p.setServicos(servicoAdicionalService.buscarPorIds(servicosIds));
                } else {
                    p.setServicos(new ArrayList<>());
                }

                prestadorService.salvar(p);

                Veiculo v = new Veiculo();
                v.setPrestador(p);
                v.setPlaca(placaVeiculo);
                v.setTipo(TipoVeiculo.valueOf(tipoVeiculoStr));
                v.setCapacidadeCarga(capacidade);
                v.setFechado("FECHADO".equalsIgnoreCase(abertoFechadoStr));
                veiculoService.salvar(v);

                novoUsuario = p;

            } else {
                Cliente c = new Cliente();
                c.setStatusAprovacao(StatusAprovacao.PENDENTE);
                c.setNomeCompleto(nomeCompleto);
                c.setEmail(email);
                c.setWhatsapp(whatsapp);
                c.setCpf(cpf);
                c.setSenha(senha);
                c.setTipoUsuario(tipoUsuario);
                clienteService.salvar(c);

                novoUsuario = c;
            }

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

            redirectAttributes.addFlashAttribute("sucesso",
                    "Cadastro realizado com sucesso! Aguarde a aprovação da nossa equipe antes de tentar fazer login.");
            return "redirect:/";
        } catch (Exception e) {
            String mensagemErro = getMensagemErro(e);

            model.addAttribute("erro", mensagemErro);
            model.addAttribute("nomeCompleto", nomeCompleto);
            model.addAttribute("email", email);
            model.addAttribute("whatsapp", whatsapp);
            model.addAttribute("cpf", cpf);
            model.addAttribute("tipoUsuario", tipoUsuarioStr);
            model.addAttribute("placaVeiculo", placaVeiculo);
            model.addAttribute("tipoVeiculo", tipoVeiculoStr);
            model.addAttribute("capacidadeModel", capacidade);
            model.addAttribute("abertoFechado", abertoFechadoStr);
            model.addAttribute("servicosIds", servicosIds);
            model.addAttribute("bairrosIds", bairrosIds);

            // Popula as dependências de listas que a página de cadastro precisa renderizar
            model.addAttribute("bairros", bairroService.listarTodos());
            model.addAttribute("servicos", servicoAdicionalService.listarTodos());

            return "cadastro";
        }
    }

    private static String getMensagemErro(Exception e) {
        if (e instanceof IllegalArgumentException && e.getMessage() != null) {
            return e.getMessage();
        }

        String mensagemErro = "Ocorreu um erro inesperado ao realizar seu cadastro. Por favor, tente novamente.";
        String originalMsg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";

        if (originalMsg.contains("duplicate key value_detail: key (email)") || originalMsg.contains("(email)")) {
            mensagemErro = "Este endereço de e-mail já está cadastrado em nosso sistema.";
        } else if (originalMsg.contains("duplicate key value_detail: key (cpf)") || originalMsg.contains("(cpf)")) {
            mensagemErro = "Este CPF já está cadastrado em nosso sistema.";
        } else if (originalMsg.contains("placa duplicada")) {
            mensagemErro = "Esta placa já está cadastrada no sistema.";
        } else if (originalMsg.contains("duplicate key value_detail: key (placa)")) {
            mensagemErro = "Esta placa de veículo já está cadastrada.";
        }
        return mensagemErro;
    }

    @GetMapping("/recuperar-senha")
    public String abrirRecuperarSenha() {
        return "recuperar-senha";
    }

    @PostMapping("/recuperar-senha")
    public String recuperarSenha(
            @RequestParam("email") String email,
            @RequestParam("cpf") String cpf,
            Model model) {

        String cpfDigitos = cpf != null ? cpf.replaceAll("\\D+", "") : "";
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorEmail(email);

        if (usuarioOpt.isEmpty()) {
            model.addAttribute("erro", "E-mail e CPF não correspondem a nenhum usuário cadastrado.");
            model.addAttribute("email", email);
            model.addAttribute("cpf", cpf);
            return "recuperar-senha";
        }

        // Normaliza o CPF armazenado para comparação (pode ter formatação ou não)
        String cpfArmazenado = usuarioOpt.get().getCpf() != null ? usuarioOpt.get().getCpf().replaceAll("\\D+", "") : "";
        if (!cpfDigitos.equals(cpfArmazenado)) {
            model.addAttribute("erro", "E-mail e CPF não correspondem a nenhum usuário cadastrado.");
            model.addAttribute("email", email);
            model.addAttribute("cpf", cpf);
            return "recuperar-senha";
        }

        String novaSenha = gerarSenhaAleatoria(8);
        Usuario usuario = usuarioOpt.get();
        usuario.setSenha(novaSenha);
        usuarioService.salvar(usuario);

        model.addAttribute("novaSenha", novaSenha);
        return "recuperar-senha";
    }

    private String gerarSenhaAleatoria(int tamanho) {
        String caracteres = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(tamanho);
        for (int i = 0; i < tamanho; i++) {
            sb.append(caracteres.charAt(random.nextInt(caracteres.length())));
        }
        return sb.toString();
    }
}
