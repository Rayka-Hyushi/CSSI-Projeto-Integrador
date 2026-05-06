package com.projetointegrador.model;

import jakarta.persistence.*;

import java.util.List;

/**
 * Modelo de entidade para representar um prestador no sistema.
 * Esta classe é mapeada para a tabela "prestadores" no banco de dados.
 */
@Entity
@Table(name = "prestadores")
@PrimaryKeyJoinColumn(name = "id_usuario")
public class Prestador extends Usuario {

    @Column(name = "status_aprovacao")
    @Enumerated(EnumType.STRING)
    private StatusAprovacao statusAprovacao;

    @Column(name = "recomendacoes")
    private int recomendacoes;

    @OneToMany(mappedBy = "prestador", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Veiculo> veiculos;

    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(
            name = "prestador_servico", // Nome da tabela no seu ER
            joinColumns = @JoinColumn(name = "id_prestador"),
            inverseJoinColumns = @JoinColumn(name = "id_servico")
    )
    private List<ServicoAdicional> servicos;

    @ManyToMany(mappedBy = "prestadores")
    private List<Cliente> clientes;

    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(
            name = "prestador_bairros",
            joinColumns = @JoinColumn(name = "id_prestador"),
            inverseJoinColumns = @JoinColumn(name = "id_bairro")
    )
    private List<Bairro> bairros;

    // Getters e Setters
    public StatusAprovacao getStatusAprovacao() {
        return statusAprovacao;
    }

    public void setStatusAprovacao(StatusAprovacao statusAprovacao) {
        this.statusAprovacao = statusAprovacao;
    }

    public int getRecomendacoes() {
        return recomendacoes;
    }

    public void setRecomendacoes(int recomendacoes) {
        this.recomendacoes = recomendacoes;
    }

    public List<Veiculo> getVeiculos() {
        return veiculos;
    }

    public void setVeiculos(List<Veiculo> veiculos) {
        this.veiculos = veiculos;
    }

    public List<ServicoAdicional> getServicos() {
        return servicos;
    }

    public void setServicos(List<ServicoAdicional> servicos) {
        this.servicos = servicos;
    }

    public List<Cliente> getClientes() {
        return clientes;
    }

    public void setClientes(List<Cliente> clientes) {
        this.clientes = clientes;
    }

    public List<Bairro> getBairros() {
        return bairros;
    }

    public void setBairros(List<Bairro> bairrosAtendidos) {
        this.bairros = bairrosAtendidos;
    }
}
