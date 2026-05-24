package com.projetointegrador.service;

import com.projetointegrador.model.Cliente;
import com.projetointegrador.model.Prestador;
import com.projetointegrador.model.Recomendacao;
import com.projetointegrador.repository.RecomendacaoRepository;
import com.projetointegrador.repository.PrestadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class RecomendacaoService {

    @Autowired
    private RecomendacaoRepository recomendacaoRepository;

    @Autowired
    private PrestadorRepository prestadorRepository;

    /**
     * Registra uma recomendação de um cliente para um prestador.
     * Incrementa o contador de recomendações do prestador.
     *
     * @param cliente Cliente que está recomendando
     * @param prestadorId ID do prestador sendo recomendado
     * @return true se a recomendação foi registrada, false se o cliente já tinha recomendado
     */
    @Transactional
    public boolean recomendarPrestador(Cliente cliente, Long prestadorId) {
        // Verifica se o cliente já recomendou este prestador
        Optional<Recomendacao> recomendacaoExistente =
            recomendacaoRepository.findByClienteAndPrestador(cliente.getId(), prestadorId);

        if (recomendacaoExistente.isPresent()) {
            return false; // Cliente já recomendou
        }

        // Busca o prestador
        Optional<Prestador> prestadorOpt = prestadorRepository.findById(prestadorId);
        if (prestadorOpt.isEmpty()) {
            return false;
        }

        Prestador prestador = prestadorOpt.get();

        // Cria e salva a recomendação
        Recomendacao recomendacao = new Recomendacao();
        recomendacao.setCliente(cliente);
        recomendacao.setPrestador(prestador);
        recomendacaoRepository.save(recomendacao);

        // Incrementa o contador de recomendações
        prestador.setRecomendacoes(prestador.getRecomendacoes() + 1);
        prestadorRepository.save(prestador);

        return true;
    }
}


