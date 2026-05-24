package com.projetointegrador.controller;

import com.projetointegrador.model.Usuario;
import com.projetointegrador.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

/**
 * Controller que passa o objeto usuário logado para todas as views do Thymeleaf.
 */
@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private UsuarioService usuarioService;

    @ModelAttribute("usuario")
    public Usuario getUsuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            String userEmail = auth.getName();

            Optional<Usuario> userOpt = usuarioService.buscarPorEmail(userEmail);
            if (userOpt.isPresent()) {
                return userOpt.get();
            }
        }

        return null;
    }
}
