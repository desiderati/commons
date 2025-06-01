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
package dev.springbloom.web.configuration.graphql.voyager;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class VoyagerIndexHtmlTemplate {

    private static final String CDN_JSDELIVR_NET_NPM = "//cdn.jsdelivr.net/npm/";
    private static final String VOYAGER = "graphql-voyager";
    private static final String DIST_DIR = "/dist/";
    private static final String STATIC_VENDOR_DIR = "vendor/voyager/";

    private final VoyagerConfigurationProperties voyagerProperties;
    private final TemplateEngine templateEngine;

    @PostConstruct
    public void onceConstructed() {
        log.info("Enabling GraphQL Voyager Client...");
    }

    public String fillIndexTemplate(String contextPath, Object csrf) throws IOException {
        Context ctx = new Context();
        if (csrf != null) {
            ctx.setVariable(VoyagerController.CSRF_ATTRIBUTE_NAME, csrf);
        }

        ctx.setVariable("pageTitle", voyagerProperties.getPageTitle());
        ctx.setVariable("graphqlEndpoint", constructGraphQlEndpoint(contextPath));
        ctx.setVariable("voyagerBaseUrl", getResourceUrl());
        ctx.setVariable("voyagerDisplayOptionsSkipRelay", Boolean.toString(voyagerProperties.getDisplayOptions().isSkipRelay()));
        ctx.setVariable("voyagerDisplayOptionsSkipDeprecated", Boolean.toString(voyagerProperties.getDisplayOptions().isSkipDeprecated()));
        ctx.setVariable("voyagerDisplayOptionsRootType", voyagerProperties.getDisplayOptions().getRootType());
        ctx.setVariable("voyagerDisplayOptionsSortByAlphabet", Boolean.toString(voyagerProperties.getDisplayOptions().isSortByAlphabet()));
        ctx.setVariable("voyagerDisplayOptionsShowLeafFields", Boolean.toString(voyagerProperties.getDisplayOptions().isShowLeafFields()));
        ctx.setVariable("voyagerDisplayOptionsHideRoot", Boolean.toString(voyagerProperties.getDisplayOptions().isHideRoot()));
        ctx.setVariable("voyagerHideDocs", Boolean.toString(voyagerProperties.isHideDocs()));
        ctx.setVariable("voyagerHideSettings", Boolean.toString(voyagerProperties.isHideSettings()));
        return templateEngine.process("html/voyager-template", ctx);
    }

    private String constructGraphQlEndpoint(String contextPath) {
        String endpoint = voyagerProperties.getEndpoint();
        if (StringUtils.isNotBlank(contextPath) && !endpoint.startsWith(contextPath)) {
            return contextPath + endpoint;
        }
        return endpoint;
    }

    private String getResourceUrl() {
        if (voyagerProperties.getCdn().isEnabled()) {
            return CDN_JSDELIVR_NET_NPM + VOYAGER + "@" + voyagerProperties.getCdn().getVersion() + DIST_DIR;
        }
        return voyagerProperties.getBasePath() + STATIC_VENDOR_DIR;
    }
}
