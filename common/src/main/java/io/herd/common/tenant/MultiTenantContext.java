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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Class responsible for storing the client identifier (Tenant).
 */
@Slf4j
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MultiTenantContext {

    private static ThreadLocal<String> currentTenantId = new ThreadLocal<>();
    private static String defaultTenant;

    @Autowired
    @SuppressWarnings("squid:S3010") // We may ignore the error because it will have only one instance by application context.
    public MultiTenantContext(@Value("${app.multitenant.default-tenant:public}") String defaultTenant) {
        MultiTenantContext.defaultTenant = defaultTenant;
    }

    public static void set(String tenant) {
        log.debug("Setting tenant: {}", tenant);
        currentTenantId.set(tenant);
    }

    public static String getId() {
        if (currentTenantId.get() == null) {
            return defaultTenant;
        }
        return currentTenantId.get();
    }

    public static void clear() {
        currentTenantId.remove();
    }
}
