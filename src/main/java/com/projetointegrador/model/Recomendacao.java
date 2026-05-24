package com.projetointegrador.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Modelo de entidade para representar uma recomendação de um cliente para um prestador.
 * Garante que cada cliente possa recomendar cada prestador apenas uma vez.
 */
@Entity
@Table(name = "recomendacoes",
       uniqueConstraints = @UniqueConstraint(columnNames = {"id_cliente", "id_prestador"}))
public class Recomendacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_prestador", nullable = false)
    private Prestador prestador;

    @Column(name = "data_recomendacao", nullable = false)
    private LocalDateTime dataRecomendacao = LocalDateTime.now();

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Prestador getPrestador() {
        return prestador;
    }

    public void setPrestador(Prestador prestador) {
        this.prestador = prestador;
    }

    public LocalDateTime getDataRecomendacao() {
        return dataRecomendacao;
    }

    public void setDataRecomendacao(LocalDateTime dataRecomendacao) {
        this.dataRecomendacao = dataRecomendacao;
    }
}

