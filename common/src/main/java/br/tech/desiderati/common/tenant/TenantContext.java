/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.tenant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * Classe responsável por armazenar o identiifcador do cliente (Tenant).
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TenantContext {

    private static ThreadLocal<String> currentTenantId = new ThreadLocal<>();
    private static ThreadLocal<UUID> currentTenantUUID = new ThreadLocal<>();
    private static final String DEFAULT_TENANT = "dummy";

    public static void set(String tenant, UUID tenantUUID) {
        log.debug("Setting tenant: {}", tenant);

        currentTenantId.set(tenant);
        currentTenantUUID.set(tenantUUID);
    }

    public static String getId() {
        if (currentTenantId.get() == null) {
            return DEFAULT_TENANT;
        }

        return currentTenantId.get();
    }

    public static UUID getUUID() {
        return currentTenantUUID.get();
    }

    public static void clear() {
        currentTenantId.remove();
        currentTenantUUID.remove();
    }
}
