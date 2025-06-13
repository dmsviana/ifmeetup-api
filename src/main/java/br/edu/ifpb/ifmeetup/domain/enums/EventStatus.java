package br.edu.ifpb.ifmeetup.domain.enums;

import java.util.Arrays;

public enum EventStatus {


    PENDING_APPROVAL("Pendente de Aprovação"),
    APPROVED("Aprovado"),
    REJECTED("Rejeitado"),
    CANCELED_BY_ORGANIZER("Cancelado pelo Organizador"), 
    CANCELED_BY_ADMIN("Cancelado pelo Administrador"), 
    CONCLUDED("Concluído"),
    IN_PROGRESS("Em Andamento");

    private final String description;

    EventStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static EventStatus fromDescription(String description) {
        return Arrays.stream(EventStatus.values())
                .filter(status -> status.getDescription().equalsIgnoreCase(description))
                .findFirst()
                .orElse(null);
    }
}