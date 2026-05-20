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
import com.projetointegrador.repository.SolicitacaoRepository;
import com.projetointegrador.repository.UsuarioRepository;
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
    private SolicitacaoRepository solicitacaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/usuarios")
    public String usuarios(Model model) {
        // Obter o usuário admin atualmente autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String usernameAtual = authentication.getName();

        // Buscar o email/username do admin atual
        Optional<Usuario> adminAtual = usuarioRepository.findByEmail(usernameAtual);

        // Filtrar todos os usuários excluindo o admin atual
        List<Usuario> usuarios = usuarioRepository.findAll()
                .stream()
                .filter(u -> !u.getId().equals(adminAtual.map(Usuario::getId).orElse(-1L)))
                .collect(Collectors.toList());

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("totalUsuarios", usuarios.size());
        return "admin/usuarios";
    }

    @GetMapping("/solicitacoes")
    public String solicitacoes(Model model) {

        long totalPendentes = solicitacaoRepository.countByStatus(StatusAprovacao.PENDENTE);
        long totalResolvidas = solicitacaoRepository.countByStatus(StatusAprovacao.APROVADO) + solicitacaoRepository.countByStatus(StatusAprovacao.REJEITADO);
        long totalUsuarios = usuarioRepository.count();

        List<Solicitacao> solicitacoesPendentes = solicitacaoRepository.findByStatus(StatusAprovacao.PENDENTE);

        model.addAttribute("totalPendentes", totalPendentes);
        model.addAttribute("totalResolvidas", totalResolvidas);
        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("solicitacoes", solicitacoesPendentes);

        return "admin/solicitacoes";
    }

    @PostMapping("/solicitacoes/aprovar")
    public String aprovarSolicitacao(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        Optional<Solicitacao> solOpt = solicitacaoRepository.findById(id);
        if (solOpt.isPresent()) {
            Solicitacao s = solOpt.get();
            s.setStatus(StatusAprovacao.APROVADO);

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

            solicitacaoRepository.save(s);
            redirectAttributes.addFlashAttribute("sucesso", "A solicitação foi Aprovada/Finalizada.");
        }
        return "redirect:/admin/solicitacoes";
    }

    @PostMapping("/solicitacoes/recusar")
    public String recusarSolicitacao(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        Optional<Solicitacao> solOpt = solicitacaoRepository.findById(id);
        if (solOpt.isPresent()) {
            Solicitacao s = solOpt.get();
            s.setStatus(StatusAprovacao.REJEITADO);

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

            solicitacaoRepository.save(s);
            redirectAttributes.addFlashAttribute("sucesso", "A solicitação foi Recusada. (Não esqueça de avisar o usuário por e-mail, se for o caso).");
        }
        return "redirect:/admin/solicitacoes";
    }

    @PostMapping("/usuarios/editar")
    public String editarUsuario(@RequestParam("id") Long id, @RequestParam("nomeCompleto") String nomeCompleto,
                                @RequestParam("email") String email, @RequestParam("whatsapp") String whatsapp,
                                @RequestParam("cpf") String cpf, @RequestParam(value = "statusAprovacao", required = false) String statusAprovacao,
                                RedirectAttributes redirectAttributes) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
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
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
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
