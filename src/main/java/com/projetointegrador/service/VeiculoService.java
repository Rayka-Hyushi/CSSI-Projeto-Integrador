package com.projetointegrador.service;

import com.projetointegrador.model.Veiculo;
import com.projetointegrador.repository.VeiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VeiculoService {

    @Autowired
    private VeiculoRepository veiculoRepository;

    public List<Veiculo> listarTodos() {
        return veiculoRepository.findAll();
    }

    public Optional<Veiculo> buscarPorId(Long id) {
        return veiculoRepository.findById(id);
    }

    public Veiculo salvar(Veiculo veiculo) {
        return veiculoRepository.save(veiculo);
    }

    public Veiculo atualizar(Long id, Veiculo veiculoAtualizado) {
        return veiculoRepository.findById(id).map(veiculo -> {
            veiculo.setPlaca(veiculoAtualizado.getPlaca());
            veiculo.setTipo(veiculoAtualizado.getTipo());
            veiculo.setCapacidadeCarga(veiculoAtualizado.getCapacidadeCarga());
            veiculo.setFechado(veiculoAtualizado.isFechado());
            return veiculoRepository.save(veiculo);
        }).orElseThrow(() -> new RuntimeException("Veículo não encontrado"));
    }

    public void deletar(Long id) {
        veiculoRepository.deleteById(id);
    }
}
