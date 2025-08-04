package br.edu.ifpb.ifmeetup.domain.projection;

import java.util.UUID;
import br.edu.ifpb.ifmeetup.domain.enums.RoomStatus;
import br.edu.ifpb.ifmeetup.domain.enums.RoomType;


public interface RoomProjection {
    UUID getId();
    String getName();
    String getLocation();
    Integer getCapacity();
    RoomType getType();
    RoomStatus getStatus();
    String getDescription();
}