/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.atmosphere.config.service.*;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.BroadcasterFactory;

import javax.inject.Inject;

// Não pode ser Singleton!!!
@Slf4j
@SuppressWarnings("unused")
@ManagedService(path = "/{notification}/{user}")
public class NotificationController {

    private String notificationControllerPathName;

    /**
     * link https://github.com/Atmosphere/atmosphere/wiki/Injecting-Atmosphere-components-and-factories-using-the-javax.inject.Inject
     */
    @Inject
    private BroadcasterFactory broadcasterFactory;

    @PathParam("user")
    private String user;

    /**
     * Executado quando a conexão foi totalmente estabelecida, ou seja, pronta para receber mensagens.
     * <p>
     * Caso seja necessário, será possível retornar uma mensagem ao ouvinte deste recurso usando um codificador.
     * Ex.: @Ready(encoders = {NotificationJacksonEncoder.class})
     * <p>
     * @link https://github.com/Atmosphere/atmosphere/wiki/Getting-Started-with-the-ManagedService-Annotation
     */
    @Ready
    public void onReady(AtmosphereResource resource) {
        log.info("Client {} connected on Broadcast {}", resource.uuid(), resource.getBroadcaster().getID());
    }

    /**
     * Chamado quando o cliente desconecta ou quando ocorre um fechamento inesperado da conexão.
     */
    @Disconnect
    public void onDisconnect(AtmosphereResourceEvent event) {
        log.info("Client {} disconnected [{}]", event.getResource().uuid(),
            (event.isCancelled() ? "cancelled" : "closed"));
    }

    /**
     * Método necessário para que o servidor receba a mensagem enviada por algum cliente,
     * execute algum tipo de tratamento e a renvie a todos os ouvintes.
     */
    @Message(encoders = NotificationJacksonEncoder.class, decoders = NotificationJacksonDecoder.class)
    public Notification onMessage(AtmosphereResource resource, Notification notification) {
        if (notification.getUuid() == null) {
            log.info("Notification message '{}' sent to all clients of broadcast '{}'",
                notification.getMessage(), resource.getBroadcaster().getID());
        } else {
            log.info("Notification message '{}' sent to client {} of broadcast '{}'",
                notification.getMessage(), notification.getUuid(), resource.getBroadcaster().getID());
        }
        return notification;
    }
}
