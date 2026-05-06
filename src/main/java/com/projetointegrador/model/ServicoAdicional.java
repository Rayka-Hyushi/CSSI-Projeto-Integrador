package com.projetointegrador.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * Modelo de entidade para representar um serviço adicional no sistema.
 * Esta classe é mapeada para a tabela "servicos_adicionais" no banco de dados.
 */
@Entity
@Table(name = "servicos_adicionais")
public class ServicoAdicional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome do serviço adicional é obrigatório")
    @Column(nullable = false, length = 150)
    private String nomeServico;

    @ManyToMany(mappedBy = "servicos")
    private List<Prestador> prestadores;

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeServico() {
        return nomeServico;
    }

    public void setNomeServico(String nomeServico) {
        this.nomeServico = nomeServico;
    }

    public List<Prestador> getPrestadores() {
        return prestadores;
    }

    public void setPrestadores(List<Prestador> prestadores) {
        this.prestadores = prestadores;
    }
}
