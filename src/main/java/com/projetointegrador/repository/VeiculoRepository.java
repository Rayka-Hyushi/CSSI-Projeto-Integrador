package com.projetointegrador.repository;

import com.projetointegrador.model.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {
    java.util.List<Veiculo> findByPrestadorId(Long prestadorId);

    boolean existsByPlaca(String placa);

    @Modifying
    @Query("DELETE FROM Veiculo v WHERE v.prestador.id = :prestadorId")
    void deleteByPrestadorId(@Param("prestadorId") Long prestadorId);
}
