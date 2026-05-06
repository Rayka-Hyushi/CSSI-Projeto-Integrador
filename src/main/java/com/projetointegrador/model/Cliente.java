package com.projetointegrador.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "clientes")
@PrimaryKeyJoinColumn(name = "id_usuario")
public class Cliente extends Usuario {

    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(
            name = "favoritos",
            joinColumns = @JoinColumn(name = "id_cliente"),
            inverseJoinColumns = @JoinColumn(name = "id_prestador")
    )
    private List<Prestador> prestadores;

    // Getters e Setters
    public List<Prestador> getPrestadores() {
        return prestadores;
    }

    public void setPrestadores(List<Prestador> prestadores) {
        this.prestadores = prestadores;
    }
}
