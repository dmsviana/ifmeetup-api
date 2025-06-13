package br.edu.ifpb.ifmeetup.domain.enums;

import java.util.Arrays;

public enum ResourceType {


    PROJECTOR("Projetor"),
    COMPUTER("Computador"),
    WHITEBOARD("Quadro Branco"),
    SOUND_SYSTEM("Sistema de Som"),
    AIR_CONDITIONING("Ar Condicionado"),
    PRINTER("Impressora"),
    VIDEO_CONFERENCE_SYSTEM("Sistema de Videoconferência"),
    OTHER("Outro");

    private final String description;

    ResourceType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static ResourceType fromDescription(String description) {
        return Arrays.stream(ResourceType.values())
                .filter(type -> type.getDescription().equalsIgnoreCase(description))
                .findFirst()
                .orElse(null);
    }
}