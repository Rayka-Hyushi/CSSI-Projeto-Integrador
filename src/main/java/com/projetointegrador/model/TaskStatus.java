package com.projetointegrador.model;

/**
 * Enum representing the possible statuses of a Task.
 */
public enum TaskStatus {
    PENDING("Pendente"),
    IN_PROGRESS("Em Andamento"),
    DONE("Concluída");

    private final String label;

    TaskStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
