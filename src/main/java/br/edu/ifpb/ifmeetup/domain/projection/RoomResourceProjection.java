package br.edu.ifpb.ifmeetup.domain.projection;

import java.util.UUID;
import br.edu.ifpb.ifmeetup.domain.enums.ResourceType;

/**
 * Interface-based projection for RoomResource entity
 * Provides a lightweight view of RoomResource data
 */
public interface RoomResourceProjection {
    UUID getId();
    ResourceType getResourceType();
    Integer getQuantity();
    String getDetails();
}