package com.projetointegrador.repository;

import com.projetointegrador.model.Prestador;
import com.projetointegrador.model.StatusAprovacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PrestadorRepository extends JpaRepository<Prestador, Long> {
    @Query("SELECT p FROM Prestador p " +
            "LEFT JOIN FETCH p.veiculos " +
            "WHERE p.statusAprovacao = :status")
    List<Prestador> findByStatusAprovacao(@Param("status") StatusAprovacao statusAprovacao);

    Optional<Prestador> findByEmail(String email);
}
