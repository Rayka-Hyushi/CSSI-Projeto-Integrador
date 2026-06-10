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
        // Apenas encripta se a senha não estiver já criptografada (BCrypt começa com $2a$, $2b$ ou $2y$)
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
                cliente.setSenha(passwordEncoder.encode(clienteAtualizado.getSenha()));
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
}
