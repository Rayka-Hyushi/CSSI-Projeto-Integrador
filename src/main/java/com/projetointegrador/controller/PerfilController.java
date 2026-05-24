package com.projetointegrador.controller;

import com.projetointegrador.model.Usuario;
import com.projetointegrador.model.Solicitacao;
import com.projetointegrador.model.StatusAprovacao;
import com.projetointegrador.model.TipoSolicitacao;
import com.projetointegrador.service.UsuarioService;
import com.projetointegrador.service.SolicitacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Controller
public class PerfilController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private SolicitacaoService solicitacaoService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String UPLOAD_DIR = "uploads/";

    @GetMapping("/perfil")
    public String abrirPerfil() {
        return "perfil/profile";
    }

    @PostMapping("/perfil/foto")
    public String uploadFotoPerfil(@RequestParam("foto") MultipartFile foto,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {

        if (foto.isEmpty()) {
            redirectAttributes.addFlashAttribute("erro", "Selecione uma imagem para fazer upload.");
            return "redirect:/perfil";
        }

        try {
            // Cria a pasta uploads caso não exista
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Gera um nome único para o arquivo, evitando sobrescrever
            String fileName = UUID.randomUUID() + "_" + foto.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            // Salva o arquivo no disco
            Files.copy(foto.getInputStream(), filePath);

            // Atualiza o usuário no banco de dados com a nova URL
            String email = authentication.getName();
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorEmail(email);

            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                usuario.setProfilePhotoUrl("/uploads/" + fileName);
                usuarioService.salvarDirecto(usuario);
                redirectAttributes.addFlashAttribute("sucesso", "Foto de perfil atualizada com sucesso!");
            }

        } catch (IOException e) {
            System.err.println("Erro ao fazer upload da imagem: " + e.getMessage());
            redirectAttributes.addFlashAttribute("erro", "Erro ao fazer upload da imagem.");
        }

        return "redirect:/perfil";
    }

    @PostMapping("/perfil/dados")
    public String atualizarDadosAdmin(@RequestParam("nomeCompleto") String nome,
                                      @RequestParam("email") String email,
                                      @RequestParam("whatsapp") String whatsapp,
                                      @RequestParam(value = "cpf", required = false) String cpf,
                                      @RequestParam(value = "senha", required = false) String novaSenha,
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {

        Optional<Usuario> userOpt = usuarioService.buscarPorEmail(authentication.getName());
        if(userOpt.isPresent()) {
            Usuario u = userOpt.get();
            // Permite atualizar apenas se for admin.
            if(authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                u.setNomeCompleto(nome);
                u.setEmail(email);
                u.setWhatsapp(whatsapp);
                if (cpf != null && !cpf.isBlank()) {
                    u.setCpf(cpf);
                }

                if (novaSenha != null && !novaSenha.isBlank()) {
                    u.setSenha(passwordEncoder.encode(novaSenha));
                }

                usuarioService.salvarDirecto(u);
                redirectAttributes.addFlashAttribute("sucesso", "Dados atualizados com sucesso!");
            } else {
                // Se for um usuário comum, permite apenas atualizar a senha
                if (novaSenha != null && !novaSenha.isBlank()) {
                    u.setSenha(passwordEncoder.encode(novaSenha));
                    usuarioService.salvarDirecto(u);
                    redirectAttributes.addFlashAttribute("sucesso", "Senha atualizada com sucesso!");
                } else {
                    redirectAttributes.addFlashAttribute("erro", "Você não tem permissão para alterar estes dados.");
                }
            }
        }
        return "redirect:/perfil";
    }

    @PostMapping("/perfil/solicitacao")
    public String enviarSolicitacaoSuporte(@RequestParam("assunto") String assunto,
                                           @RequestParam("mensagem") String mensagem,
                                           Authentication authentication,
                                           RedirectAttributes redirectAttributes) {

        var userOpt = usuarioService.buscarPorEmail(authentication.getName());
        if (userOpt.isPresent()) {
            Solicitacao solicitacao = new Solicitacao();
            solicitacao.setUsuario(userOpt.get());

            // Define o tipo
            if ("ALTERACAO_DADOS".equals(assunto)) {
                solicitacao.setTipoSolicitacao(TipoSolicitacao.ALTERACAO_DADOS);
            } else if ("SUPORTE".equals(assunto)) {
                solicitacao.setTipoSolicitacao(TipoSolicitacao.SUPORTE);
            } else {
                solicitacao.setTipoSolicitacao(TipoSolicitacao.OUTRO);
            }

            solicitacao.setDetalhes(mensagem);
            solicitacao.setStatusSolicitacao(StatusAprovacao.PENDENTE);

            solicitacaoService.criar(solicitacao);
            redirectAttributes.addFlashAttribute("sucesso", "Sua solicitação foi enviada aos administradores!");
        } else {
            redirectAttributes.addFlashAttribute("erro", "Erro ao encontrar seu usuário para criar solicitação!");
        }

        return "redirect:/perfil";
    }

    @PostMapping("/perfil/solicitacao/senha")
    public String solicitarRecuperacaoSenha(@RequestParam("nome") String nome,
                                            @RequestParam("cpf") String cpf,
                                            @RequestParam("email") String email,
                                            RedirectAttributes redirectAttributes) {

        var userOpt = usuarioService.buscarPorEmail(email);

        if (userOpt.isPresent()) {
            Usuario usuario = userOpt.get();
            boolean nomeConfere = usuario.getNomeCompleto() != null && usuario.getNomeCompleto().trim().equalsIgnoreCase(nome.trim());
            boolean cpfConfere = usuario.getCpf() != null && usuario.getCpf().replaceAll("\\D", "").equals(cpf.replaceAll("\\D", ""));

            if (nomeConfere && cpfConfere) {
                Solicitacao sol = new Solicitacao();
                sol.setTipoSolicitacao(TipoSolicitacao.RECUPERACAO_SENHA);
                sol.setUsuario(usuario);
                sol.setStatusSolicitacao(StatusAprovacao.PENDENTE);
                sol.setDetalhes(
                        "Solicitação de recuperação de senha via tela de login.\n" +
                        "Nome informado: " + nome + "\n" +
                        "CPF informado: " + cpf + "\n" +
                        "E-mail informado: " + email
                );

                solicitacaoService.criar(sol);
                redirectAttributes.addFlashAttribute("sucesso", "Solicitação de recuperação enviada! Aguarde as instruções da nossa equipe no seu e-mail.");
            } else {
                redirectAttributes.addFlashAttribute("erro", "Os dados informados não conferem com um usuário cadastrado.");
            }
        } else {
            redirectAttributes.addFlashAttribute("erro", "Não foi possível localizar um usuário com esse e-mail.");
        }

        return "redirect:/";
    }
}
