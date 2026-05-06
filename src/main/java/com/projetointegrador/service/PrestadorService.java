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

    public List<Prestador> buscarPorStatus(StatusAprovacao status) {
        return prestadorRepository.findByStatusAprovacao(status);
    }

    public Prestador salvar(Prestador prestador) {
        return prestadorRepository.save(prestador);
    }

    public Prestador atualizar(Long id, Prestador prestadorAtualizado) {
        return prestadorRepository.findById(id).map(prestador -> {
            prestador.setNomeCompleto(prestadorAtualizado.getNomeCompleto());
            prestador.setEmail(prestadorAtualizado.getEmail());
            if (prestadorAtualizado.getSenha() != null && !prestadorAtualizado.getSenha().isEmpty()) {
                prestador.setSenha(passwordEncoder.encode(prestadorAtualizado.getSenha()));
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
}
