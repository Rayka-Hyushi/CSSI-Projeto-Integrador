package com.projetointegrador.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.projetointegrador.model.Solicitacao;
import com.projetointegrador.model.StatusAprovacao;
import com.projetointegrador.model.Usuario;
import com.projetointegrador.model.Cliente;
import com.projetointegrador.model.Prestador;
import com.projetointegrador.model.Veiculo;
import com.projetointegrador.service.UsuarioService;
import com.projetointegrador.service.VeiculoService;
import com.projetointegrador.service.SolicitacaoService;
import com.projetointegrador.service.ClienteService;
import com.projetointegrador.service.PrestadorService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller para as paginas do administrador.
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')") // Somente usuários com a role ADMIN podem acessar as rotas deste controller
public class AdminController {

    @Autowired
    private SolicitacaoService solicitacaoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private VeiculoService veiculoService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private PrestadorService prestadorService;

    @GetMapping("/usuarios")
    public String usuarios(Model model) {
        // Obter o usuário admin atualmente autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String usernameAtual = authentication.getName();

        // Buscar o email/username do admin atual
        Optional<Usuario> adminAtual = usuarioService.buscarPorEmail(usernameAtual);

        // Filtrar todos os usuários excluindo o admin atual
        List<Usuario> usuarios = usuarioService.listarTodos()
                .stream()
                .filter(u -> !u.getId().equals(adminAtual.map(Usuario::getId).orElse(-1L)))
                .collect(Collectors.toList());

        // Carrega veículos para prestadores (mapa: prestadorId -> lista de veículos)
        java.util.Map<Long, List<Veiculo>> veiculosMap = new java.util.HashMap<>();
        for (Usuario u : usuarios) {
            if (u instanceof Prestador) {
                veiculosMap.put(u.getId(), veiculoService.buscarPorPrestadorId(u.getId()));
            }
        }

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("veiculosMap", veiculosMap);
        model.addAttribute("totalUsuarios", usuarios.size());
        return "admin/usuarios";
    }

    @PostMapping("/usuarios/remover")
    public String removerUsuario(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(id);
        if (usuarioOpt.isPresent()) {
            usuarioService.deletar(id);
            redirectAttributes.addFlashAttribute("sucesso", "Usuário removido com sucesso.");
        } else {
            redirectAttributes.addFlashAttribute("erro", "Usuário não encontrado.");
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/veiculo/salvar")
    public String salvarVeiculo(@RequestParam(value = "id", required = false) Long id,
            @RequestParam("prestadorId") Long prestadorId,
            @RequestParam("placa") String placa,
            @RequestParam("tipo") String tipoStr,
            @RequestParam(value = "capacidade", required = false) Double capacidade,
            @RequestParam(value = "fechado", required = false) String fechado,
            RedirectAttributes redirectAttributes) {

        Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(prestadorId);
        if (usuarioOpt.isPresent() && usuarioOpt.get() instanceof Prestador prestador) {
            Veiculo v;
            if (id != null) {
                v = veiculoService.buscarPorId(id)
                        .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));
            } else {
                v = new Veiculo();
            }

            // Verifica placa duplicada: novo veículo OU placa alterada na edição
            String placaNormalizada = placa != null ? placa.trim().toUpperCase() : "";
            boolean placaAlterada = (id == null) || !placaNormalizada.equals(v.getPlaca() != null ? v.getPlaca().toUpperCase() : "");
            if (placaAlterada && veiculoService.existePorPlaca(placaNormalizada)) {
                redirectAttributes.addFlashAttribute("erro", "Já existe um veículo cadastrado com a placa '" + placaNormalizada + "'.");
                return "redirect:/admin/usuarios";
            }

            v.setPlaca(placaNormalizada);
            try {
                v.setTipo(com.projetointegrador.model.TipoVeiculo.valueOf(tipoStr));
            } catch (IllegalArgumentException e) {
                redirectAttributes.addFlashAttribute("erro", "Tipo de veículo inválido.");
                return "redirect:/admin/usuarios";
            }
            v.setCapacidadeCarga(capacidade != null ? capacidade : 0.0);
            v.setFechado("on".equalsIgnoreCase(fechado));
            v.setPrestador(prestador);

            try {
                veiculoService.salvar(v);
                redirectAttributes.addFlashAttribute("sucesso", "Veículo salvo com sucesso.");
            } catch (DataIntegrityViolationException e) {
                redirectAttributes.addFlashAttribute("erro", "Já existe um veículo cadastrado com essa placa.");
            }
        } else {
            redirectAttributes.addFlashAttribute("erro", "Prestador não encontrado.");
        }

        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/veiculo/remover")
    public String removerVeiculo(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        Optional<Veiculo> vOpt = veiculoService.buscarPorId(id);
        if (vOpt.isPresent()) {
            veiculoService.deletar(id);
            redirectAttributes.addFlashAttribute("sucesso", "Veículo removido com sucesso.");
        } else {
            redirectAttributes.addFlashAttribute("erro", "Veículo não encontrado.");
        }
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/solicitacoes")
    public String solicitacoes(Model model) {

        long totalPendentes = solicitacaoService.contarPorStatus(StatusAprovacao.PENDENTE);
        long totalResolvidas = solicitacaoService.contarPorStatus(StatusAprovacao.APROVADO)
                + solicitacaoService.contarPorStatus(StatusAprovacao.REJEITADO);
        long totalUsuarios = usuarioService.contarUsuariosAtivos();

        List<Solicitacao> solicitacoesPendentes = solicitacaoService.buscarPorStatus(StatusAprovacao.PENDENTE);

        model.addAttribute("totalPendentes", totalPendentes);
        model.addAttribute("totalResolvidas", totalResolvidas);
        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("solicitacoes", solicitacoesPendentes);

        return "admin/solicitacoes";
    }

    @PostMapping("/solicitacoes/aprovar")
    public String aprovarSolicitacao(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        var solOpt = solicitacaoService.buscarPorId(id);
        if (solOpt.isPresent()) {
            Solicitacao s = solOpt.get();
            s.setStatusSolicitacao(StatusAprovacao.APROVADO);

            // Se for de cadastro ou outro que envolva usuário com pendência, altera para
            // aprovado
            Usuario u = s.getUsuario();
            if (u != null) {
                if (u instanceof Cliente cliente) {
                    clienteService.atualizarStatus(cliente.getId(), StatusAprovacao.APROVADO);
                } else if (u instanceof Prestador prestador) {
                    prestadorService.atualizarStatus(prestador.getId(), StatusAprovacao.APROVADO);
                }
            }

            solicitacaoService.atualizar(id, s);
            redirectAttributes.addFlashAttribute("sucesso", "A solicitação foi Aprovada/Finalizada.");
        }
        return "redirect:/admin/solicitacoes";
    }

    @PostMapping("/solicitacoes/recusar")
    public String recusarSolicitacao(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        var solOpt = solicitacaoService.buscarPorId(id);
        if (solOpt.isPresent()) {
            Solicitacao s = solOpt.get();
            s.setStatusSolicitacao(StatusAprovacao.REJEITADO);

            Usuario u = s.getUsuario();
            if (u != null) {
                if (u instanceof Cliente cliente) {
                    clienteService.atualizarStatus(cliente.getId(), StatusAprovacao.REJEITADO);
                } else if (u instanceof Prestador prestador) {
                    prestadorService.atualizarStatus(prestador.getId(), StatusAprovacao.REJEITADO);
                }
            }

            solicitacaoService.atualizar(id, s);
            redirectAttributes.addFlashAttribute("sucesso",
                    "A solicitação foi Recusada. (Não esqueça de avisar o usuário por e-mail, se for o caso).");
        }
        return "redirect:/admin/solicitacoes";
    }

    @PostMapping("/usuarios/editar")
    public String editarUsuario(@RequestParam("id") Long id, @RequestParam("nomeCompleto") String nomeCompleto,
            @RequestParam("email") String email, @RequestParam("whatsapp") String whatsapp,
            @RequestParam("cpf") String cpf,
            @RequestParam(value = "statusAprovacao", required = false) String statusAprovacao,
            RedirectAttributes redirectAttributes) {
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(id);
        if (!usuarioOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("erro", "Usuário não encontrado.");
            return "redirect:/admin/usuarios";
        }

        try {
            Usuario usuario = usuarioOpt.get();
            usuario.setNomeCompleto(nomeCompleto);
            usuario.setEmail(email);
            usuario.setWhatsapp(whatsapp);
            usuario.setCpf(cpf);

            // Sempre salva os campos básicos
            usuarioService.salvar(usuario);

            // Atualiza status se informado
            if (statusAprovacao != null && !statusAprovacao.isBlank()) {
                try {
                    StatusAprovacao status = StatusAprovacao.valueOf(statusAprovacao);
                    if (usuario instanceof Cliente cliente) {
                        clienteService.atualizarStatus(cliente.getId(), status);
                    } else if (usuario instanceof Prestador prestador) {
                        prestadorService.atualizarStatus(prestador.getId(), status);
                    }
                } catch (IllegalArgumentException e) {
                    // Status inválido, ignora
                }
            }

            redirectAttributes.addFlashAttribute("sucesso", "Dados do usuário atualizados com sucesso!");
        } catch (RuntimeException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Erro ao atualizar usuário.";
            if (msg.toLowerCase().contains("email")) {
                msg = "Este e-mail já está em uso por outro usuário.";
            } else if (msg.toLowerCase().contains("cpf")) {
                msg = "Este CPF já está em uso por outro usuário.";
            }
            redirectAttributes.addFlashAttribute("erro", msg);
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/atualizar-status")
    public String atualizarStatusUsuario(@RequestParam("id") Long id, @RequestParam("status") String status,
            RedirectAttributes redirectAttributes) {
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(id);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            try {
                StatusAprovacao novoStatus = StatusAprovacao.valueOf(status);
                if (usuario instanceof Cliente cliente) {
                    clienteService.atualizarStatus(cliente.getId(), novoStatus);
                } else if (usuario instanceof Prestador prestador) {
                    prestadorService.atualizarStatus(prestador.getId(), novoStatus);
                }

                String mensagem = novoStatus == StatusAprovacao.APROVADO ? "Usuário ativado com sucesso!"
                        : "Usuário desativado com sucesso!";
                redirectAttributes.addFlashAttribute("sucesso", mensagem);
            } catch (IllegalArgumentException e) {
                redirectAttributes.addFlashAttribute("erro", "Status inválido!");
            }
        }
        return "redirect:/admin/usuarios";
    }
}
