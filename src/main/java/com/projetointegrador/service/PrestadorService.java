package com.projetointegrador.service;

import com.projetointegrador.model.Prestador;
import com.projetointegrador.model.StatusAprovacao;
import com.projetointegrador.repository.PrestadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PrestadorService {

    @Autowired
    private PrestadorRepository prestadorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Prestador> listarTodos() {
        return prestadorRepository.findAll();
    }

    public Optional<Prestador> buscarPorId(Long id) {
        return prestadorRepository.findById(id);
    }

    public Optional<Prestador> buscarPorEmail(String email) {
        return prestadorRepository.findByEmail(email);
    }

    public List<Prestador> buscarPorStatus(StatusAprovacao status) {
        return prestadorRepository.findByStatusAprovacao(status);
    }

    public Prestador salvar(Prestador prestador) {
        prestador.setCpf(formatarCpf(prestador.getCpf()));
        prestador.setWhatsapp(formatarWhatsapp(prestador.getWhatsapp()));

        if (prestador.getSenha() != null && !prestador.getSenha().isEmpty()) {
            String senha = prestador.getSenha();
            if (!senha.startsWith("$2a$") && !senha.startsWith("$2b$") && !senha.startsWith("$2y$")) {
                prestador.setSenha(passwordEncoder.encode(senha));
            }
        }
        return prestadorRepository.save(prestador);
    }

    public Prestador atualizar(Long id, Prestador prestadorAtualizado) {
        return prestadorRepository.findById(id).map(prestador -> {
            prestador.setNomeCompleto(prestadorAtualizado.getNomeCompleto());
            prestador.setEmail(prestadorAtualizado.getEmail());
            if (prestadorAtualizado.getSenha() != null && !prestadorAtualizado.getSenha().isEmpty()) {
                String senha = prestadorAtualizado.getSenha();
                if (!senha.startsWith("$2a$") && !senha.startsWith("$2b$") && !senha.startsWith("$2y$")) {
                    prestador.setSenha(passwordEncoder.encode(senha));
                } else {
                    prestador.setSenha(senha);
                }
            }
            prestador.setWhatsapp(prestadorAtualizado.getWhatsapp());
            prestador.setCpf(prestadorAtualizado.getCpf());
            prestador.setStatusAprovacao(prestadorAtualizado.getStatusAprovacao());
            return prestadorRepository.save(prestador);
        }).orElseThrow(() -> new RuntimeException("Prestador não encontrado"));
    }

    public void deletar(Long id) {
        prestadorRepository.deleteById(id);
    }

    public Prestador atualizarStatus(Long id, StatusAprovacao status) {
        return prestadorRepository.findById(id).map(prestador -> {
            prestador.setStatusAprovacao(status);
            return prestadorRepository.save(prestador);
        }).orElseThrow(() -> new RuntimeException("Prestador não encontrado"));
    }

    private static String formatarCpf(String cpf) {
        if (cpf == null) return "";
        String digitos = cpf.replaceAll("\\D+", "");
        if (digitos.length() != 11) return digitos;
        return digitos.substring(0, 3) + "." + digitos.substring(3, 6) + "." + digitos.substring(6, 9) + "-" + digitos.substring(9);
    }

    private static String formatarWhatsapp(String whatsapp) {
        if (whatsapp == null) return "";
        String digitos = whatsapp.replaceAll("\\D+", "");
        if (digitos.length() > 11 && digitos.startsWith("55")) {
            digitos = digitos.substring(2);
        }
        digitos = digitos.substring(0, Math.min(digitos.length(), 11));
        if (digitos.length() == 11) {
            return "(" + digitos.substring(0, 2) + ") " + digitos.substring(2, 7) + "-" + digitos.substring(7);
        } else if (digitos.length() == 10) {
            return "(" + digitos.substring(0, 2) + ") " + digitos.substring(2, 6) + "-" + digitos.substring(6);
        }
        return digitos;
    }
}
