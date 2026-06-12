package com.projetointegrador.controller;

import com.projetointegrador.model.Prestador;
import com.projetointegrador.service.PrestadorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/prestador")
public class PrestadorController {

    @Autowired
    private PrestadorService prestadorService;

    @GetMapping("/inicio")
    @Transactional(readOnly = true)
    public String homePrestador(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            Optional<Prestador> prestadorOpt = prestadorService.buscarPorEmail(email);
            if (prestadorOpt.isPresent()) {
                Prestador prestador = prestadorOpt.get();

                // Inicializa as coleções Lazy dentro da transação para evitar
                // LazyInitializationException
                if (prestador.getVeiculos() != null) {
                    prestador.getVeiculos().size();
                }
                if (prestador.getBairros() != null) {
                    prestador.getBairros().size();
                }
                if (prestador.getServicos() != null) {
                    prestador.getServicos().size();
                }

                model.addAttribute("prestador", prestador);
                model.addAttribute("quantidadeRecomendacoes", prestador.getRecomendacoes());
                model.addAttribute("quantidadeVeiculos",
                        prestador.getVeiculos() != null ? prestador.getVeiculos().size() : 0);
                model.addAttribute("quantidadeBairros",
                        prestador.getBairros() != null ? prestador.getBairros().size() : 0);
                model.addAttribute("quantidadeServicos",
                        prestador.getServicos() != null ? prestador.getServicos().size() : 0);
            }
        }
        return "prestador/inicio";
    }

    @GetMapping("/veiculos")
    @Transactional(readOnly = true)
    public String listarVeiculos(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            Optional<Prestador> prestadorOpt = prestadorService.buscarPorEmail(email);
            prestadorOpt.ifPresent(prestador -> {
                // Initialize lazy collections
                if (prestador.getVeiculos() != null) {
                    prestador.getVeiculos().size();
                }
                model.addAttribute("prestador", prestador);
            });
        }
        return "prestador/veiculos";
    }

    @PostMapping("/status")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> alterarStatusOnline(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("erro", "Não autenticado"));
        }

        String email = authentication.getName();
        Optional<Prestador> prestadorOpt = prestadorService.buscarPorEmail(email);
        if (prestadorOpt.isPresent()) {
            Prestador prestador = prestadorOpt.get();
            prestador.setStatusOnline(!prestador.isStatusOnline());
            prestadorService.salvar(prestador);
            return ResponseEntity.ok(Map.of("statusOnline", prestador.isStatusOnline()));
        }

        return ResponseEntity.status(404).body(Map.of("erro", "Prestador não encontrado"));
    }
}
