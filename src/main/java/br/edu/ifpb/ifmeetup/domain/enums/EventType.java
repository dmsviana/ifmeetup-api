package br.edu.ifpb.ifmeetup.domain.enums;

import java.util.Arrays;

public enum EventType {


    COURSE("Curso"),
    WORKSHOP("Workshop"),
    LECTURE("Palestra"),
    MEETING("Reunião"),
    SEMINAR("Seminário"),
    MINICOURSE("Minicurso"),
    OTHER("Outro");

    private final String description;

    EventType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static EventType fromDescription(String description) {
        return Arrays.stream(EventType.values())
                .filter(type -> type.getDescription().equalsIgnoreCase(description))
                .findFirst()
                .orElse(null);
    }
}