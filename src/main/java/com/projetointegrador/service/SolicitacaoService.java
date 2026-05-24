package com.projetointegrador.service;

import com.projetointegrador.model.Solicitacao;
import com.projetointegrador.model.StatusAprovacao;
import com.projetointegrador.repository.SolicitacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SolicitacaoService {

    @Autowired
    private SolicitacaoRepository solicitacaoRepository;

    /**
     * Listar todas as solicitações
     */
    public List<Solicitacao> listarTodas() {
        return solicitacaoRepository.findAll();
    }

    /**
     * Buscar solicitação por ID
     */
    public Optional<Solicitacao> buscarPorId(Long id) {
        return solicitacaoRepository.findById(id);
    }

    /**
     * Buscar solicitações por status
     */
    public List<Solicitacao> buscarPorStatus(StatusAprovacao status) {
        return solicitacaoRepository.findByStatusSolicitacao(status);
    }

    /**
     * Contar solicitações por status
     */
    public long contarPorStatus(StatusAprovacao status) {
        return solicitacaoRepository.countByStatusSolicitacao(status);
    }

    /**
     * Salvar uma nueva solicitação
     */
    public Solicitacao criar(Solicitacao solicitacao) {
        return solicitacaoRepository.save(solicitacao);
    }

    /**
     * Atualizar uma solicitação existente
     */
    public Solicitacao atualizar(Long id, Solicitacao solicitacaoAtualizada) {
        return solicitacaoRepository.findById(id).map(solicitacao -> {
            solicitacao.setTipoSolicitacao(solicitacaoAtualizada.getTipoSolicitacao());
            solicitacao.setStatusSolicitacao(solicitacaoAtualizada.getStatusSolicitacao());
            solicitacao.setDetalhes(solicitacaoAtualizada.getDetalhes());
            return solicitacaoRepository.save(solicitacao);
        }).orElseThrow(() -> new RuntimeException("Solicitação não encontrada"));
    }

    /**
     * Deletar uma solicitação
     */
    public void deletar(Long id) {
        solicitacaoRepository.deleteById(id);
    }

    /**
     * Atualizar apenas o status de uma solicitação
     */
    public Solicitacao atualizarStatus(Long id, StatusAprovacao novoStatus) {
        return solicitacaoRepository.findById(id).map(solicitacao -> {
            solicitacao.setStatusSolicitacao(novoStatus);
            return solicitacaoRepository.save(solicitacao);
        }).orElseThrow(() -> new RuntimeException("Solicitação não encontrada"));
    }
}

