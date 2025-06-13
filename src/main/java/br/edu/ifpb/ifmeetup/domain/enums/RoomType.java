package br.edu.ifpb.ifmeetup.domain.enums;

import java.util.Arrays;

public enum RoomType {
    

    CLASSROOM("Sala de aula"),
    AUDITORIUM("Auditório"),
    LABORATORY("Laboratório"),
    MEETING_ROOM("Sala de reunião"),
    SHARED_SPACE("Espaço compartilhado"),
    OTHER("Outro");

    private final String description;

    RoomType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static RoomType fromDescription(String description) {
        return Arrays.stream(RoomType.values())
                .filter(type -> type.getDescription().equals(description))
                .findFirst()
                .orElse(null);
    }
    

}
