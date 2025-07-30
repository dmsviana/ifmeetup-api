package br.edu.ifpb.ifmeetup.domain.projection;

import java.time.LocalDateTime;
import java.util.UUID;
import br.edu.ifpb.ifmeetup.domain.enums.EventStatus;
import br.edu.ifpb.ifmeetup.domain.enums.EventType;


public interface EventProjection {
    UUID getId();
    String getTitle();
    String getDescription();
    LocalDateTime getStartDateTime();
    LocalDateTime getEndDateTime();
    Integer getMaxParticipants();
    EventStatus getStatus();
    EventType getEventType();
    boolean isPublicEvent();
    
    UUID getRoomId();
    String getRoomName();
    
    UUID getOrganizerId();
    String getOrganizerName();
    String getOrganizerEmail();
}