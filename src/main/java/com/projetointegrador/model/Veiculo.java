package com.projetointegrador.model;

import jakarta.persistence.*;

/**
 * Modelo de entidade para representar um veículo no sistema.
 * Esta classe é mapeada para a tabela "veículos" no banco de dados.
 */
@Entity
@Table(name = "veiculos")
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String placa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoVeiculo tipo;

    @Column(length = 100)
    private double capacidadeCarga;

    @Column(nullable = false)
    private boolean isFechado;

    @ManyToOne
    @JoinColumn(name = "id_prestador", nullable = false)
    private Prestador prestador;

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public TipoVeiculo getTipo() {
        return tipo;
    }

    public void setTipo(TipoVeiculo tipo) {
        this.tipo = tipo;
    }

    public double getCapacidadeCarga() {
        return capacidadeCarga;
    }

    public void setCapacidadeCarga(double capacidadeCarga) {
        this.capacidadeCarga = capacidadeCarga;
    }

    public boolean isFechado() {
        return isFechado;
    }

    public void setFechado(boolean fechado) {
        isFechado = fechado;
    }

    public Prestador getPrestador() {
        return prestador;
    }

    public void setPrestador(Prestador prestador) {
        this.prestador = prestador;
    }
}
