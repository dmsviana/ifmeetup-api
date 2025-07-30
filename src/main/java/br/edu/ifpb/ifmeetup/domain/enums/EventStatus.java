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
    
    /**
     * Verifica se um evento com este status pode ser atualizado.
     * 
     * <p>Eventos nos seguintes status não podem ser atualizados:
     * <ul>
     *   <li>{@link #CONCLUDED} - Eventos já concluídos</li>
     *   <li>{@link #CANCELED_BY_ADMIN} - Eventos cancelados pelo administrador</li>
     *   <li>{@link #CANCELED_BY_ORGANIZER} - Eventos cancelados pelo organizador</li>
     * </ul>
     * 
     * @return {@code true} se o evento pode ser atualizado, {@code false} caso contrário
     * 
     */
    public boolean isUpdatable() {
    	return this != CONCLUDED &&
    		   this != CANCELED_BY_ADMIN &&
    		   this != CANCELED_BY_ORGANIZER;
    }
}