package com.projetointegrador.model;

/**
 * Enum representando os tipos de veículos.
 */
public enum TipoVeiculo {
    FIORINO("Fiorino"),
    VAN("Van"),
    CAMINHONETE("Caminhonete"),
    VUC("Caminhão Urbano de Carga"),
    CAMINHAO_BAU("Caminhão Baú");

    private final String descricao;

    TipoVeiculo(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
