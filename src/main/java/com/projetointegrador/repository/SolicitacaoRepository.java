package com.projetointegrador.repository;

import com.projetointegrador.model.Solicitacao;
import com.projetointegrador.model.StatusAprovacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long> {
    @EntityGraph(attributePaths = "usuario")
    List<Solicitacao> findByStatusSolicitacao(StatusAprovacao statusSolicitacao);

    long countByStatusSolicitacao(StatusAprovacao statusSolicitacao);

    List<Solicitacao> findByUsuarioId(Long usuarioId);

    @Modifying
    @Query("DELETE FROM Solicitacao s WHERE s.usuario.id = :usuarioId")
    void deleteByUsuarioId(@Param("usuarioId") Long usuarioId);
}
