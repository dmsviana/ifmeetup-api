package br.edu.ifpb.ifmeetup.domain.projection;

import java.time.LocalDateTime;
import java.util.UUID;
import br.edu.ifpb.ifmeetup.domain.enums.AttendanceStatus;

/**
 * Interface-based projection for EventParticipant entity
 * Provides a lightweight view of EventParticipant data
 */
public interface EventParticipantProjection {


    UUID getId();
    LocalDateTime getRegistrationDateTime();
    AttendanceStatus getAttendanceStatus();
    boolean isCertificateIssued();
    String getFeedback();
    
    UUID getEventId();
    String getEventTitle();
    
    UUID getUserId();
    String getUserFirstName();
    String getUserLastName();
    String getUserEmail();


    default String getUserFullName() {
        return getUserFirstName() + " " + getUserLastName();
    }
}