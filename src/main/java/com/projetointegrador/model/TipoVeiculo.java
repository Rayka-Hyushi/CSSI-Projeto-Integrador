package com.projetointegrador.model;

public enum TipoVeiculo {
    FIORINO("Fiorino"),
    VAN("Van"),
    CAMINHONETE("Caminhonete"),
    VUC("Caminhão Urbano de Carga"),
    BAU_ABERTO("Caminhão Baú - Aberto"),
    BAU_FECHADO("Caminhão Baú - Fechado");

    private final String descricao;

    TipoVeiculo(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
