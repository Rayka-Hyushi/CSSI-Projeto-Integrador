package com.projetointegrador.repository;

import com.projetointegrador.model.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {
    java.util.List<Veiculo> findByPrestadorId(Long prestadorId);
}
