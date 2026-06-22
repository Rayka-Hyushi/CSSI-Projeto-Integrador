package com.projetointegrador.repository;

import com.projetointegrador.model.Solicitacao;
import com.projetointegrador.model.StatusAprovacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long> {
    @EntityGraph(attributePaths = "usuario")
    List<Solicitacao> findByStatusSolicitacao(StatusAprovacao statusSolicitacao);

    long countByStatusSolicitacao(StatusAprovacao statusSolicitacao);

    List<Solicitacao> findByUsuarioId(Long usuarioId);

    void deleteByUsuarioId(Long usuarioId);
}
