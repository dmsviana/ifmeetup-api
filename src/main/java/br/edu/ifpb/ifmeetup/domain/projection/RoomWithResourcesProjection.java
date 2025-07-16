package br.edu.ifpb.ifmeetup.domain.projection;

import java.util.Set;

/**
 * Interface-based projection for Room entity that includes resources
 * Extends RoomProjection and adds inventory information
 */
public interface RoomWithResourcesProjection extends RoomProjection {
    Set<RoomResourceProjection> getInventory();
}