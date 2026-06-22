package com.projetointegrador.service;

import com.projetointegrador.model.Cliente;
import com.projetointegrador.model.StatusAprovacao;
import com.projetointegrador.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    public Optional<Cliente> buscarPorId(Long id) {
        return clienteRepository.findById(id);
    }

    public Optional<Cliente> buscarPorEmail(String email) {
        return clienteRepository.findByEmail(email);
    }

    public Cliente salvar(Cliente cliente) {
        cliente.setCpf(formatarCpf(cliente.getCpf()));
        cliente.setWhatsapp(formatarWhatsapp(cliente.getWhatsapp()));

        if (cliente.getSenha() != null && !cliente.getSenha().isEmpty()) {
            String senha = cliente.getSenha();
            if (!senha.startsWith("$2a$") && !senha.startsWith("$2b$") && !senha.startsWith("$2y$")) {
                cliente.setSenha(passwordEncoder.encode(senha));
            }
        }
        return clienteRepository.save(cliente);
    }

    public Cliente atualizar(Long id, Cliente clienteAtualizado) {
        return clienteRepository.findById(id).map(cliente -> {
            cliente.setNomeCompleto(clienteAtualizado.getNomeCompleto());
            if (clienteRepository.existsByEmail(cliente.getEmail())) {
                throw new RuntimeException("Email já cadastrado");
            }
            cliente.setEmail(clienteAtualizado.getEmail());
            if (clienteAtualizado.getSenha() != null && !clienteAtualizado.getSenha().isEmpty()) {
                String senha = clienteAtualizado.getSenha();
                if (!senha.startsWith("$2a$") && !senha.startsWith("$2b$") && !senha.startsWith("$2y$")) {
                    cliente.setSenha(passwordEncoder.encode(senha));
                } else {
                    cliente.setSenha(senha);
                }
            }
            cliente.setWhatsapp(clienteAtualizado.getWhatsapp());
            cliente.setCpf(clienteAtualizado.getCpf());
            return clienteRepository.save(cliente);
        }).orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
    }

    public void deletar(Long id) {
        clienteRepository.deleteById(id);
    }

    public Cliente atualizarStatus(Long id, StatusAprovacao status) {
        return clienteRepository.findById(id).map(cliente -> {
            cliente.setStatusAprovacao(status);
            return clienteRepository.save(cliente);
        }).orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
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
