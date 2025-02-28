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
package io.herd.common.web.notification.controller;

import io.herd.common.web.notification.domain.NotificationMessage;
import io.herd.common.web.notification.domain.NotificationMessageDecoder;
import io.herd.common.web.notification.domain.NotificationMessageEncoder;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.atmosphere.config.service.*;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.BroadcasterFactory;

// Cannot be singleton!
@Slf4j
@Getter
@ManagedService(path = "/{notificationPath}/{broadcaster}")
public class NotificationController {

    /**
     * <a href="https://github.com/Atmosphere/atmosphere/wiki/Injecting-Atmosphere-components-and-factories-using-the-javax.inject.Inject">
     * Injecting Atmosphere components and factories using the javax.inject.Inject
     * </a>
     */
    @Inject
    private BroadcasterFactory broadcasterFactory;

    @PathParam("notificationPath")
    @SuppressWarnings("unused")
    private String notificationPath;

    @PathParam("broadcaster")
    @SuppressWarnings("unused")
    private String broadcaster;

    /**
     * Executed when the connection was fully established, in other words, ready to receive messages.
     * <p>
     * If necessary, it will be possible to return a message to the listener of this resource using an encoder.
     * E.g.: @Ready(encoders = {NotificationJacksonEncoder.class})
     * <p>
     *
     * <a href="https://github.com/Atmosphere/atmosphere/wiki/Getting-Started-with-the-ManagedService-Annotation">
     * Getting Started with the ManagedService Annotation
     * </a>
     */
    @Ready
    public void onReady(AtmosphereResource resource) {
        log.info("Client '{}' connected on Broadcast '{}'", resource.uuid(), resource.getBroadcaster().getID());
    }

    /**
     * Called when the client disconnects or when an unexpected connection closes.
     */
    @Disconnect
    public void onDisconnect(AtmosphereResourceEvent event) {
        log.info("Client '{}' disconnected [{}]", event.getResource().uuid(),
            (event.isCancelled() ? "cancelled" : "closed")
        );
    }

    /**
     * Necessary method for: 1) to receive the message sent by a client,
     * 2) to perform some type of treatment and 3) to resend it to all listeners.
     */
    @Message(encoders = NotificationMessageEncoder.class, decoders = NotificationMessageDecoder.class)
    public NotificationMessage<?> onMessage(AtmosphereResource resource, NotificationMessage<?> notification) {
        if (notification.getUuid() == null) {
            log.trace("Notification message '{}' sent to all clients of broadcast '{}'",
                notification.getPayload(), resource.getBroadcaster().getID()
            );
        } else {
            log.trace("Notification message '{}' sent to client '{}' of broadcast '{}'",
                notification.getPayload(), notification.getUuid(), resource.getBroadcaster().getID()
            );
        }
        return notification;
    }
}
