package br.edu.ifpb.ifmeetup.domain.projection;

import java.util.UUID;
import br.edu.ifpb.ifmeetup.domain.enums.RoomStatus;
import br.edu.ifpb.ifmeetup.domain.enums.RoomType;

/**
 * Interface-based projection for Room entity
 * Provides a lightweight view of Room data
 */
public interface RoomProjection {
    UUID getId();
    String getName();
    String getLocation();
    Integer getCapacity();
    RoomType getType();
    RoomStatus getStatus();
    String getDescription();
}