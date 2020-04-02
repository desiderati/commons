/*
 * Copyright (c) 2020 - Felipe Desiderati
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.herd.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.atmosphere.cpr.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
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
