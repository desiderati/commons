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
package io.herd.common.web.configuration.graphql.voyager;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Data
@ConfigurationProperties(prefix = "spring.graphql.voyager")
public class VoyagerConfigurationProperties {

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class Cdn {
        private boolean enabled = false;
        private String version = "latest";
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class DisplayOptions {
        private boolean skipRelay = true;
        private boolean skipDeprecated = true;
        private String rootType = "Query";
        private boolean sortByAlphabet = false;
        private boolean showLeafFields = true;
        private boolean hideRoot = false;
    }

    private boolean enabled = false;

    private String endpoint = "/graphql";

    private String pageTitle = "Voyager";

    private String basePath = "/";

    @JsonIgnore
    @NestedConfigurationProperty
    private Cdn cdn = new Cdn();

    @JsonIgnore
    @NestedConfigurationProperty
    private DisplayOptions displayOptions = new DisplayOptions();

    @JsonIgnore
    private boolean hideDocs = false;

    @JsonIgnore
    private boolean hideSettings = false;

}
