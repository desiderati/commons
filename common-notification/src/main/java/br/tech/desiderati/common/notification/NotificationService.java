/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.atmosphere.cpr.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@SuppressWarnings("unused")
public class NotificationService {

    private final BroadcasterFactory atmosphereBroadcasterFactory;

    private final AtmosphereResourceFactory atmosphereResourceFactory;

    @Autowired
    public NotificationService(BroadcasterFactory atmosphereBroadcasterFactory,
                               AtmosphereResourceFactory atmosphereResourceFactory) {
        this.atmosphereBroadcasterFactory = atmosphereBroadcasterFactory;
        this.atmosphereResourceFactory = atmosphereResourceFactory;
    }

    public void broadcast(String message) {
        for (Broadcaster broadcaster : atmosphereBroadcasterFactory.lookupAll()) {
            broadcaster.broadcast(new Notification(message));
        }
    }

    public void broadcastToSpecificBroadcaster(String broadcasterId, String message) {
        Broadcaster broadcaster = atmosphereBroadcasterFactory.lookup(broadcasterId);
        if (broadcaster == null) {
            log.warn("Atmosphere Broadcaster '{}' not found!", broadcasterId);
            return;
        }

        broadcaster.broadcast(new Notification(message));
    }

    public void broadcastToSpecificResource(String resourceId, String message) {
        AtmosphereResource atmosphereResource = atmosphereResourceFactory.find(resourceId);
        if (atmosphereResource == null) {
            log.warn("Atmosphere Resource '{}' not found!", resourceId);
            return;
        }

        for (Broadcaster broadcaster : atmosphereResource.broadcasters()) {
            broadcaster.broadcast(new Notification(resourceId, message), atmosphereResource);
        }
    }
}
