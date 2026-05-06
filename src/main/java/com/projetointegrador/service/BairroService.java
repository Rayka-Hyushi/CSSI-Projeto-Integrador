package com.projetointegrador.service;

import com.projetointegrador.model.Bairro;
import com.projetointegrador.repository.BairroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BairroService {

    @Autowired
    private BairroRepository bairroRepository;

    public List<Bairro> listarTodos() {
        return bairroRepository.findAll();
    }

    public Optional<Bairro> buscarPorId(Long id) {
        return bairroRepository.findById(id);
    }

    public Bairro salvar(Bairro bairro) {
        return bairroRepository.save(bairro);
    }

    public Bairro atualizar(Long id, Bairro bairroAtualizado) {
        return bairroRepository.findById(id).map(bairro -> {
            bairro.setNomeBairro(bairroAtualizado.getNomeBairro());
            return bairroRepository.save(bairro);
        }).orElseThrow(() -> new RuntimeException("Bairro não encontrado"));
    }

    public void deletar(Long id) {
        bairroRepository.deleteById(id);
    }
}
