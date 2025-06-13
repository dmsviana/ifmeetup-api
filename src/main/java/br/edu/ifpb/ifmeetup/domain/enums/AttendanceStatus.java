package br.edu.ifpb.ifmeetup.domain.enums;

import java.util.Arrays;

public enum AttendanceStatus {


    REGISTERED("Inscrito"),          
    PRESENT("Presente"),             
    ABSENT("Ausente"),              
    CANCELED("Inscrição Cancelada");

    private final String description;

    AttendanceStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static AttendanceStatus fromDescription(String description) {
        return Arrays.stream(AttendanceStatus.values())
                .filter(status -> status.getDescription().equalsIgnoreCase(description))
                .findFirst()
                .orElse(null);
    }
}