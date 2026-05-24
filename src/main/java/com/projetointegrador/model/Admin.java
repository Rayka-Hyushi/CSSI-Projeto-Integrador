package com.projetointegrador.model;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

/**
 * Modelo de entidade para representar um administrador no sistema.
 * Esta classe é mapeada para a tabela "admins" no banco de dados.
 * Admins são criados apenas durante a configuração inicial do sistema.
 */
@Entity
@Table(name = "admins")
@PrimaryKeyJoinColumn(name = "id_usuario")
public class Admin extends Usuario {

    // Admin não possui atributos adicionais além de Usuario
    // É apenas uma especialização de Usuario para fins de classificação

    public Admin() {
        super();
    }
}

