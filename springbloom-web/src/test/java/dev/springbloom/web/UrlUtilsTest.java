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
package dev.springbloom.web;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UrlUtilsTest {

    @Test
    public void testSanitizeUrl() {
        // Test with blank input
        assertThat(UrlUtils.sanitizeUrl("")).isEqualTo("/");
        assertThat(UrlUtils.sanitizeUrl(null)).isEqualTo("/");
        assertThat(UrlUtils.sanitizeUrl("   ")).isEqualTo("/");

        // Test with valid URI
        assertThat(UrlUtils.sanitizeUrl("http:/example.com/path")).isEqualTo("http:/example.com/path");
        assertThat(UrlUtils.sanitizeUrl("http://example.com/path")).isEqualTo("http://example.com/path");
        assertThat(UrlUtils.sanitizeUrl("http:///example.com/path")).isEqualTo("http://example.com/path");

        // Test with duplicate separators
        assertThat(UrlUtils.sanitizeUrl("//path//to//resource")).isEqualTo("/path/to/resource");
        assertThat(UrlUtils.sanitizeUrl("//path//to//resource//")).isEqualTo("/path/to/resource");

        // Test with trailing separator
        assertThat(UrlUtils.sanitizeUrl("/path/to/resource/")).isEqualTo("/path/to/resource");

        // Test with invalid URI (should fall back to simple sanitization)
        assertThat(UrlUtils.sanitizeUrl("invalid:path")).isEqualTo("invalid:path");
        assertThat(UrlUtils.sanitizeUrl("invalid:/path")).isEqualTo("invalid:/path");
        assertThat(UrlUtils.sanitizeUrl("invalid://path")).isEqualTo("invalid://path");
        assertThat(UrlUtils.sanitizeUrl("invalid:///path")).isEqualTo("invalid://path");
    }

    @Test
    public void testSanitize() {
        // Test with blank input
        assertThat(UrlUtils.sanitize("")).isEqualTo("/");
        assertThat(UrlUtils.sanitize(null)).isEqualTo("/");
        assertThat(UrlUtils.sanitize("   ")).isEqualTo("/");

        // Test with duplicate separators
        assertThat(UrlUtils.sanitize("//path//to//resource")).isEqualTo("/path/to/resource");
        assertThat(UrlUtils.sanitize("//path//to//resource//")).isEqualTo("/path/to/resource");

        // Test with a path not starting with a separator
        assertThat(UrlUtils.sanitize("path/to/resource")).isEqualTo("/path/to/resource");

        // Test with trailing separator
        assertThat(UrlUtils.sanitize("/path/to/resource/")).isEqualTo("/path/to/resource");

        // Test with both issues
        assertThat(UrlUtils.sanitize("path///to///resource/")).isEqualTo("/path/to/resource");
    }

    @Test
    public void testAppendDoubleAsterisk() {
        // Test with blank input
        assertThat(UrlUtils.appendDoubleAsterisk("")).isEqualTo("/**");
        assertThat(UrlUtils.appendDoubleAsterisk(null)).isEqualTo("/**");
        assertThat(UrlUtils.appendDoubleAsterisk("   ")).isEqualTo("/**");

        // Test with a root path
        assertThat(UrlUtils.appendDoubleAsterisk("/")).isEqualTo("/**");

        // Test with a path ending with a separator
        assertThat(UrlUtils.appendDoubleAsterisk("/path/to/resource/"))
            .isEqualTo("/path/to/resource/**");

        // Test with a path not ending with a separator
        assertThat(UrlUtils.appendDoubleAsterisk("/path/to/resource"))
            .isEqualTo("/path/to/resource/**");

        // Test with a path not starting with a separator (should be sanitized)
        assertThat(UrlUtils.appendDoubleAsterisk("path/to/resource"))
            .isEqualTo("/path/to/resource/**");
    }

    @Test
    public void testAppendSlash() {
        // Test with blank input
        assertThat(UrlUtils.appendSlash("")).isEqualTo("/");
        assertThat(UrlUtils.appendSlash(null)).isEqualTo("/");
        assertThat(UrlUtils.appendSlash("   ")).isEqualTo("/");

        // Test with a root path
        assertThat(UrlUtils.appendSlash("/")).isEqualTo("/");

        // Test with a path ending with a separator
        assertThat(UrlUtils.appendSlash("/path/to/resource/")).isEqualTo("/path/to/resource/");

        // Test with a path not ending with a separator
        assertThat(UrlUtils.appendSlash("/path/to/resource")).isEqualTo("/path/to/resource/");

        // Test with a path not starting with a separator (should be sanitized)
        assertThat(UrlUtils.appendSlash("path/to/resource")).isEqualTo("/path/to/resource/");
    }
}
