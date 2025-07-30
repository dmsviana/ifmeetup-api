package br.edu.ifpb.ifmeetup.domain.projection;


public interface EventProjectionWithParticipants extends EventProjection {
    
    Long getCurrentParticipants();
}