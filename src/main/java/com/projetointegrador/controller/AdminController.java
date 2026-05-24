package com.projetointegrador.controller;

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
import com.projetointegrador.repository.UsuarioRepository;
import com.projetointegrador.service.UsuarioService;
import com.projetointegrador.service.VeiculoService;
import com.projetointegrador.service.SolicitacaoService;
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
    private UsuarioRepository usuarioRepository;


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

            v.setPlaca(placa);
            try {
                v.setTipo(com.projetointegrador.model.TipoVeiculo.valueOf(tipoStr));
            } catch (IllegalArgumentException e) {
                // ignore invalid tipo
            }
            v.setCapacidadeCarga(capacidade != null ? capacidade : 0.0);
            v.setFechado("on".equalsIgnoreCase(fechado));
            v.setPrestador(prestador);

            veiculoService.salvar(v);
            redirectAttributes.addFlashAttribute("sucesso", "Veículo salvo com sucesso.");
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
        long totalResolvidas = solicitacaoService.contarPorStatus(StatusAprovacao.APROVADO) + solicitacaoService.contarPorStatus(StatusAprovacao.REJEITADO);
        long totalUsuarios = usuarioRepository.count();

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

            // Se for de cadastro ou outro que envolva usuário com pendência, altera para aprovado
            Usuario u = s.getUsuario();
            if (u != null) {
                if (u instanceof Cliente) {
                    ((Cliente) u).setStatusAprovacao(StatusAprovacao.APROVADO);
                    usuarioRepository.save(u);
                } else if (u instanceof Prestador) {
                    ((Prestador) u).setStatusAprovacao(StatusAprovacao.APROVADO);
                    usuarioRepository.save(u);
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
                if (u instanceof Cliente) {
                    ((Cliente) u).setStatusAprovacao(StatusAprovacao.REJEITADO);
                    usuarioRepository.save(u);
                } else if (u instanceof Prestador) {
                    ((Prestador) u).setStatusAprovacao(StatusAprovacao.REJEITADO);
                    usuarioRepository.save(u);
                }
            }

            solicitacaoService.atualizar(id, s);
            redirectAttributes.addFlashAttribute("sucesso", "A solicitação foi Recusada. (Não esqueça de avisar o usuário por e-mail, se for o caso).");
        }
        return "redirect:/admin/solicitacoes";
    }

    @PostMapping("/usuarios/editar")
    public String editarUsuario(@RequestParam("id") Long id, @RequestParam("nomeCompleto") String nomeCompleto,
                                @RequestParam("email") String email, @RequestParam("whatsapp") String whatsapp,
                                @RequestParam("cpf") String cpf, @RequestParam(value = "statusAprovacao", required = false) String statusAprovacao,
                                RedirectAttributes redirectAttributes) {
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(id);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            usuario.setNomeCompleto(nomeCompleto);
            usuario.setEmail(email);
            usuario.setWhatsapp(whatsapp);
            usuario.setCpf(cpf);

            // Se o usuário for Cliente ou Prestador e o status foi informado, atualiza
            if (statusAprovacao != null) {
                try {
                    StatusAprovacao status = StatusAprovacao.valueOf(statusAprovacao);
                    if (usuario instanceof Cliente) {
                        ((Cliente) usuario).setStatusAprovacao(status);
                    } else if (usuario instanceof Prestador) {
                        ((Prestador) usuario).setStatusAprovacao(status);
                    }
                } catch (IllegalArgumentException e) {
                    // Status inválido, ignora
                }
            }

            usuarioRepository.save(usuario);
            redirectAttributes.addFlashAttribute("sucesso", "Dados do usuário atualizados com sucesso!");
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
                if (usuario instanceof Cliente) {
                    ((Cliente) usuario).setStatusAprovacao(novoStatus);
                } else if (usuario instanceof Prestador) {
                    ((Prestador) usuario).setStatusAprovacao(novoStatus);
                }
                usuarioRepository.save(usuario);

                String mensagem = novoStatus == StatusAprovacao.APROVADO ?
                        "Usuário ativado com sucesso!" :
                        "Usuário desativado com sucesso!";
                redirectAttributes.addFlashAttribute("sucesso", mensagem);
            } catch (IllegalArgumentException e) {
                redirectAttributes.addFlashAttribute("erro", "Status inválido!");
            }
        }
        return "redirect:/admin/usuarios";
    }
}
