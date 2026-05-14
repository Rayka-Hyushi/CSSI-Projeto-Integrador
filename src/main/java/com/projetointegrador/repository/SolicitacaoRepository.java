package com.projetointegrador.repository;

import com.projetointegrador.model.Solicitacao;
import com.projetointegrador.model.StatusAprovacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long> {
    List<Solicitacao> findByStatus(StatusAprovacao status);
    long countByStatus(StatusAprovacao status);
}
