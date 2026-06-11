package com.projetointegrador.controller;

import com.projetointegrador.model.Admin;
import com.projetointegrador.model.TipoUsuario;
import com.projetointegrador.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

/**
 * Controller para configuração inicial do sistema (criação do primeiro admin).
 * Esta tela só aparece se não houver nenhum usuário no banco de dados.
 */
@Controller
@RequestMapping("/setup")
public class SetupController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public String setup() {
        // Se houver qualquer usuário no sistema, redireciona para home
        if (usuarioService.contar() > 0) {
            return "redirect:/";
        }

        return "setup";
    }

    @PostMapping("/criar-admin")
    public String criarAdmin(
            @RequestParam("nomeCompleto") String nomeCompleto,
            @RequestParam("email") String email,
            @RequestParam("senha") String senha,
            @RequestParam("confirmaSenha") String confirmaSenha,
            @RequestParam("whatsapp") String whatsapp,
            @RequestParam("cpf") String cpf,
            RedirectAttributes redirectAttributes) {

        // Se houver qualquer usuário no sistema, redireciona para home
        if (usuarioService.contar() > 0) {
            return "redirect:/";
        }

        // Validar se as senhas coincidem
        if (!senha.equals(confirmaSenha)) {
            redirectAttributes.addFlashAttribute("erro", "As senhas não coincidem!");
            return "redirect:/setup";
        }

        // Validar se a senha tem pelo menos 6 caracteres
        if (senha.length() < 6) {
            redirectAttributes.addFlashAttribute("erro", "A senha deve ter pelo menos 6 caracteres!");
            return "redirect:/setup";
        }

        // Validar email básico
        if (!email.contains("@")) {
            redirectAttributes.addFlashAttribute("erro", "Email inválido!");
            return "redirect:/setup";
        }

        try {
            // Criar usuário admin
            Admin admin = new Admin();
            admin.setNomeCompleto(nomeCompleto);
            admin.setEmail(email);
            admin.setSenha(senha);
            admin.setWhatsapp(whatsapp);
            admin.setCpf(cpf);
            admin.setTipoUsuario(TipoUsuario.ROLE_ADMIN);
            admin.setCreatedAt(LocalDateTime.now());

            usuarioService.salvar(admin);

            redirectAttributes.addFlashAttribute("sucesso", "Administrador criado com sucesso! Agora você pode fazer login.");
            return "redirect:/?admin-criado=true";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao criar administrador: " + e.getMessage());
            return "redirect:/setup";
        }
    }
}


