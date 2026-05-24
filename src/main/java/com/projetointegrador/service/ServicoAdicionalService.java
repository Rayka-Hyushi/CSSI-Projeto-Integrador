package com.projetointegrador.service;

import com.projetointegrador.model.ServicoAdicional;
import com.projetointegrador.repository.ServicoAdicionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServicoAdicionalService {

    @Autowired
    private ServicoAdicionalRepository servicoAdicionalRepository;

    public List<ServicoAdicional> listarTodos() {
        return servicoAdicionalRepository.findAll();
    }

    public Optional<ServicoAdicional> buscarPorId(Long id) {
        return servicoAdicionalRepository.findById(id);
    }

    public ServicoAdicional salvar(ServicoAdicional servicoAdicional) {
        return servicoAdicionalRepository.save(servicoAdicional);
    }

    public ServicoAdicional atualizar(Long id, ServicoAdicional servicoAdicionalAtualizado) {
        return servicoAdicionalRepository.findById(id).map(servicoAdicional -> {
            servicoAdicional.setNomeServico(servicoAdicionalAtualizado.getNomeServico());
            return servicoAdicionalRepository.save(servicoAdicional);
        }).orElseThrow(() -> new RuntimeException("Serviço adicional não encontrado"));
    }

    public void deletar(Long id) {
        servicoAdicionalRepository.deleteById(id);
    }

    public List<ServicoAdicional> buscarPorIds(List<Long> ids) {
        return servicoAdicionalRepository.findAllById(ids);
    }
}


