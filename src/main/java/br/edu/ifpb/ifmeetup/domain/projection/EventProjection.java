package br.edu.ifpb.ifmeetup.domain.projection;

import java.time.LocalDateTime;
import java.util.UUID;
import br.edu.ifpb.ifmeetup.domain.enums.EventStatus;
import br.edu.ifpb.ifmeetup.domain.enums.EventType;

/**
 * Interface-based projection for Event entity
 * Provides a lightweight view of Event data
 */
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
    
    // Room information
    UUID getRoomId();
    String getRoomName();
    
    // Organizer information
    UUID getOrganizerId();
    String getOrganizerName();
    String getOrganizerEmail();
}