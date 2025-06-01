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
package dev.springbloom.web.configuration.graphql.altair;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Data
@ConfigurationProperties("spring.graphql.altair.options")
public class AltairConfigurationPropertiesOptions {

    @Data
    static class InitialEnvironments {

        @Data
        static class InitialEnvironmentState {
            private String id;
            private String title;
            private Map<String, String> variables;
        }

        @NestedConfigurationProperty
        InitialEnvironmentState base;

        @NestedConfigurationProperty
        List<InitialEnvironmentState> subEnvironments;
    }

    @Data
    static class InitialSettings {
        private String theme;
        private String language;
        private Integer addQueryDepthLimit;
        private Integer tabSize;
        private Boolean enableExperimental;

        @JsonProperty("theme.fontsize")
        private Integer themeFontSize;

        @JsonProperty("theme.editorFontFamily")
        private String themeEditorFontFamily;

        @JsonProperty("theme.editorFontSize")
        private Integer themeEditorFontSize;

        private Boolean disablePushNotification;

        @JsonProperty("plugin.list")
        private List<String> pluginList;

        @JsonProperty("request.withCredentials")
        private Boolean requestWithCredentials;

        @JsonProperty("schema.reloadOnStart")
        private Boolean schemaReloadOnStart;

        @JsonProperty("alert.disableWarnings")
        private Boolean alertDisableWarnings;

        @JsonProperty("history.depth")
        private Integer historyDepth;

        @JsonProperty("response.hideExtensions")
        private Boolean responseHideExtensions;
    }

    private String endpointURL = "/graphql";

    private String subscriptionsEndpoint = "/subscriptions";

    private String initialQuery;

    private String initialVariables;

    private String initialPreRequestScript;

    private String initialPostRequestScript;

    private Map<String, String> initialHeaders;

    @NestedConfigurationProperty
    InitialEnvironments initialEnvironments;

    private String instanceStorageNamespace;

    @NestedConfigurationProperty
    InitialSettings initialSettings;

    private String initialSubscriptionsProvider;

    private Map<String, String> initialSubscriptionsPayload;

    private Boolean preserveState = true;

    private String initialHttpMethod;

}
