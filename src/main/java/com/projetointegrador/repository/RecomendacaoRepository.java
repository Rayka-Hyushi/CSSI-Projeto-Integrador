package com.projetointegrador.repository;

import com.projetointegrador.model.Recomendacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RecomendacaoRepository extends JpaRepository<Recomendacao, Long> {

    @Query("SELECT r FROM Recomendacao r WHERE r.cliente.id = :clienteId AND r.prestador.id = :prestadorId")
    Optional<Recomendacao> findByClienteAndPrestador(@Param("clienteId") Long clienteId, @Param("prestadorId") Long prestadorId);
}

