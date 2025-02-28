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
package io.herd.common.web.configuration.graphql.altair;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class AltairIndexHtmlTemplate {

    private static final String CDN_JSDELIVR_NET_NPM = "//cdn.jsdelivr.net/npm/";
    private static final String ALTAIR = "altair-static";
    private static final String DIST_DIR = "/build/dist/";
    private static final String STATIC_VENDOR_DIR = "vendor/altair/";

    private final AltairConfigurationProperties altairProperties;
    private final AltairConfigurationPropertiesOptions altairOptions;
    private final AltairConfigurationPropertiesResources altairResources;
    private final TemplateEngine templateEngine;

    @PostConstruct
    public void onceConstructed() {
        altairResources.load(altairOptions);
        log.info("Enabling GraphQL Altair Client...");
    }

    public String fillIndexTemplate(String contextPath) throws IOException {
        Context ctx = new Context();
        ctx.setVariable("pageTitle", altairProperties.getPageTitle());
        ctx.setVariable("altairBaseUrl", getResourceUrl());

        configureGraphQlEndpoint(contextPath);
        ctx.setVariable("options", altairOptions);
        return templateEngine.process("html/altair-template", ctx);
    }

    private void configureGraphQlEndpoint(String contextPath) {
        String endpoint = altairOptions.getEndpointURL();
        altairOptions.setEndpointURL(
            (StringUtils.isNotBlank(contextPath) && !endpoint.startsWith(contextPath))
                ? contextPath + endpoint
                : endpoint
        );
    }

    private String getResourceUrl() {
        if (altairProperties.getCdn().isEnabled()) {
            return CDN_JSDELIVR_NET_NPM + ALTAIR + "@" + altairProperties.getCdn().getVersion() + DIST_DIR;
        }
        return altairProperties.getBasePath() + STATIC_VENDOR_DIR;
    }
}
