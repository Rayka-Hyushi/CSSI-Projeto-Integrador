package com.projetointegrador.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "servicos_adicionais")
public class ServicoAdicional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome do serviço adicional é obrigatório")
    @Column(nullable = false, length = 150)
    private String nomeServico;

    @ManyToOne
    @JoinColumn(name = "id_prestador_servico", nullable = false)
    private Prestador_Servico prestadorServico;

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

    public Prestador_Servico getPrestadorServico() {
        return prestadorServico;
    }

    public void setPrestadorServico(Prestador_Servico prestadorServico) {
        this.prestadorServico = prestadorServico;
    }
}
