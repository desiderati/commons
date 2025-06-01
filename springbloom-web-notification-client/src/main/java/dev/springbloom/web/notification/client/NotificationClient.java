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
