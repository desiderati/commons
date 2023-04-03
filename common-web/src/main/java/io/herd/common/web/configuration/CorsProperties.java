/*
 * Copyright (c) 2023 - Felipe Desiderati
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
package io.herd.common.web.configuration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import java.util.List;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
public class CorsProperties {

    @NotEmpty
    private List<@NotBlank String> allowedMethods;

    @NotEmpty
    private List<@NotBlank String> allowedHeaders;

    @NotEmpty
    private List<@NotBlank String> allowedOrigins;

    @NotEmpty
    private List<@NotBlank String> exposedHeaders;

    public void addCorsMappings(final @NonNull CorsRegistry registry, final @NonNull String mapping) {
        registry.addMapping(mapping)
            .allowedMethods(getAllowedMethods().toArray(new String[]{}))
            .allowedHeaders(getAllowedHeaders().toArray(new String[]{}))
            .allowedOrigins(getAllowedOrigins().toArray(new String[]{}))
            .exposedHeaders(getExposedHeaders().toArray(new String[]{}));

        log.info("""
                Registering CORS configuration for: {}
                \t[AllowedMethods: {}, AllowedHeaders: {}, AllowedOrigins: {}, ExposedHeaders: {}]""",
            mapping,
            String.join(",", getAllowedMethods()),
            String.join(",", getAllowedHeaders()),
            String.join(",", getAllowedOrigins()),
            String.join(",", getExposedHeaders())
        );
    }
}
