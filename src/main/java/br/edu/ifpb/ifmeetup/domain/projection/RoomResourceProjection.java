package br.edu.ifpb.ifmeetup.domain.projection;

import java.util.UUID;
import br.edu.ifpb.ifmeetup.domain.enums.ResourceType;

public interface RoomResourceProjection {
    UUID getId();
    ResourceType getResourceType();
    Integer getQuantity();
    String getDetails();
}