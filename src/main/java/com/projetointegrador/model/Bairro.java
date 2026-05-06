package com.projetointegrador.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * Modelo de entidade para representar um bairro no sistema.
 * Esta classe é mapeada para a tabela "bairros" no banco de dados.
 */
@Entity
@Table(name = "bairros")
public class Bairro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome do bairro é obrigatório")
    @Column(nullable = false, length = 150)
    private String nomeBairro;

    @ManyToMany(mappedBy = "bairros")
    private List<Prestador> prestadores;

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeBairro() {
        return nomeBairro;
    }

    public void setNomeBairro(String nomeBairro) {
        this.nomeBairro = nomeBairro;
    }
}
