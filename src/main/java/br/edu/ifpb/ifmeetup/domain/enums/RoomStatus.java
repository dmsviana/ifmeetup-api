package br.edu.ifpb.ifmeetup.domain.enums;

import java.util.Arrays;

public enum RoomStatus {

    AVAILABLE("Disponível"),
    UNAVAILABLE("Indisponível"),
    UNDER_MAINTENANCE("Manutenção"),
    DISABLED("Desabilitado");

    private final String description;

    RoomStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    
    }

    public static RoomStatus fromDescription(String description) {
        return Arrays.stream(RoomStatus.values())
                .filter(status -> status.getDescription().equals(description))
                .findFirst()
                .orElse(null);
    }


}
