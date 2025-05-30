/*
 * Copyright (c) 2025 - Felipe Desiderati
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
package io.herd.common.web.notification.service;

import io.herd.common.web.UrlUtils;
import io.herd.common.web.notification.domain.NotificationMessage;
import lombok.extern.slf4j.Slf4j;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceFactory;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {

    @Value("${spring.web.atmosphere.url.mapping:/atm}")
    private String atmosphereUrlMapping;

    private final BroadcasterFactory atmosphereBroadcasterFactory;

    private final AtmosphereResourceFactory atmosphereResourceFactory;

    @Autowired
    public NotificationService(
        @Lazy BroadcasterFactory atmosphereBroadcasterFactory,
        @Lazy AtmosphereResourceFactory atmosphereResourceFactory
    ) {
        this.atmosphereBroadcasterFactory = atmosphereBroadcasterFactory;
        this.atmosphereResourceFactory = atmosphereResourceFactory;
    }

    public <P> void broadcastToAll(P payload) {
        for (Broadcaster broadcaster : atmosphereBroadcasterFactory.lookupAll()) {
            broadcaster.broadcast(new NotificationMessage<>(payload));
        }
    }

    public <P> void broadcastToSpecificBroadcaster(String broadcasterId, P payload) {
        String prefix = UrlUtils.appendSlash(atmosphereUrlMapping + "/notification");
        Broadcaster broadcaster = atmosphereBroadcasterFactory.lookup(prefix + broadcasterId);
        if (broadcaster == null) {
            log.warn("Atmosphere Broadcaster '{}' not found!", prefix + broadcasterId);
            return;
        }
        broadcaster.broadcast(new NotificationMessage<>(payload));
    }

    public <P> void broadcastToSpecificResource(String resourceId, P payload) {
        AtmosphereResource atmosphereResource = atmosphereResourceFactory.find(resourceId);
        if (atmosphereResource == null) {
            log.warn("Atmosphere Resource '{}' not found!", resourceId);
            return;
        }

        for (Broadcaster broadcaster : atmosphereResource.broadcasters()) {
            broadcaster.broadcast(new NotificationMessage<>(resourceId, payload), atmosphereResource);
        }
    }
}
