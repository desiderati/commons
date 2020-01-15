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
package io.herd.common.tenant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * Class responsible for storing the customer identifier (Tenant).
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

    @SuppressWarnings("unused")
    public static UUID getUUID() {
        return currentTenantUUID.get();
    }

    @SuppressWarnings("unused")
    public static void clear() {
        currentTenantId.remove();
        currentTenantUUID.remove();
    }
}
