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

package dev.springbloom.web.notification.client;

import io.openapi.client.ApiException;
import io.openapi.client.api.BroadcastControllerApi;
import io.openapi.client.api.model.BroadcastMessageObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("unused")
public class NotificationClient {

    private final BroadcastControllerApi broadcastControllerApi;

    public NotificationClient(BroadcastControllerApi broadcastControllerApi) {
        this.broadcastControllerApi = broadcastControllerApi;
    }

    public <M> void broadcastToAll(M message) {
        try {
            BroadcastMessageObject broadcastMessage = new BroadcastMessageObject();
            broadcastMessage.setPayload(message);
            broadcastControllerApi.broadcast(broadcastMessage);
        } catch (ApiException ex) {
            log.error("It was not possible to broadcast message!", ex);
        }
    }

    public <M> void broadcastToSpecificBroadcaster(String broadcasterId, M message) {
        try {
            BroadcastMessageObject broadcastMessage = new BroadcastMessageObject();
            broadcastMessage.setBroadcastId(broadcasterId);
            broadcastMessage.setPayload(message);
            broadcastControllerApi.broadcast(broadcastMessage);
        } catch (ApiException ex) {
            log.error("It was not possible to broadcast message!", ex);
        }
    }

    public <M> void broadcastToSpecificResource(String resourceId, M message) {
        try {
            BroadcastMessageObject broadcastMessage = new BroadcastMessageObject();
            broadcastMessage.setResourceId(resourceId);
            broadcastMessage.setPayload(message);
            broadcastControllerApi.broadcast(broadcastMessage);
        } catch (ApiException ex) {
            log.error("It was not possible to broadcast message!", ex);
        }
    }
}
