package br.edu.ifpb.ifmeetup.domain.projection;

import java.util.Set;


public interface RoomWithResourcesProjection extends RoomProjection {
    Set<RoomResourceProjection> getInventory();
}