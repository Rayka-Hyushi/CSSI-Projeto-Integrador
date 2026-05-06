package com.projetointegrador.repository;

import com.projetointegrador.model.Prestador;
import com.projetointegrador.model.StatusAprovacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrestadorRepository extends JpaRepository<Prestador, Long> {
    List<Prestador> findByStatusAprovacao(StatusAprovacao statusAprovacao);
}
