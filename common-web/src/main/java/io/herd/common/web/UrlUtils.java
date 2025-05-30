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
package io.herd.common.web;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

/**
 * Utility class for URL manipulation and standardization.
 * <p>
 * This class provides methods to sanitize, format, and manipulate URL paths
 * to ensure consistent formatting across the application.
 */
@UtilityClass
public class UrlUtils {

    /**
     * Constant representing the standard URL path separator character.
     */
    // If you remove the static keyword, a compilation error occurs.
    @SuppressWarnings("RedundantModifiersUtilityClassLombok")
    public static final String URL_PATH_SEPARATOR = "/";

    /**
     * Sanitizes a URL path by removing duplicate separators and trailing separators.
     * <p>
     * If the input path is blank, returns a single separator.
     *
     * @param path the URL path to sanitize
     * @return the sanitized URL path
     */
    public String sanitizeUrl(String path) {
        if (StringUtils.isBlank(path)) {
            path = URL_PATH_SEPARATOR;
        } else {
            // Some sanitization. (Replace all duplicated)
            path = path.replaceAll(URL_PATH_SEPARATOR + "+", URL_PATH_SEPARATOR);
            if (path.endsWith(URL_PATH_SEPARATOR)) {
                path = path.substring(0, path.length() - 1);
            }
        }
        return path;
    }

    /**
     * Sanitizes a URL path by removing duplicate separators, ensuring it starts with a separator,
     * and removing trailing separators.
     * <p>
     * If the input path is blank, returns a single separator.
     *
     * @param path the URL path to sanitize
     * @return the sanitized URL path
     */
    public String sanitize(String path) {
        if (StringUtils.isBlank(path)) {
            path = URL_PATH_SEPARATOR;
        } else {
            // Some sanitization. (Replace all duplicated)
            path = path.replaceAll(URL_PATH_SEPARATOR + "+", URL_PATH_SEPARATOR);
            if (!path.startsWith(URL_PATH_SEPARATOR)) {
                path = URL_PATH_SEPARATOR + path;
            }
            if (path.endsWith(URL_PATH_SEPARATOR)) {
                path = path.substring(0, path.length() - 1);
            }
        }
        return path;
    }

    /**
     * Appends "/**" to the end of a URL path.
     * <p>
     * This is commonly used for creating wildcard patterns for URL matching.
     * The path is first sanitized using the {@link #sanitize(String)} method.
     *
     * @param pathUrl the URL path to append the double asterisk to
     * @return the URL path with "/**" appended
     */
    public String appendDoubleAsterisk(String pathUrl) {
        pathUrl = sanitize(pathUrl);
        if (StringUtils.isBlank(pathUrl) || pathUrl.equals(URL_PATH_SEPARATOR)) {
            return URL_PATH_SEPARATOR + "**";
        } else {
            return pathUrl.endsWith(URL_PATH_SEPARATOR) ?
                pathUrl + "**" :
                pathUrl + URL_PATH_SEPARATOR + "**";
        }
    }

    /**
     * Appends a slash to the end of a URL path if it doesn't already end with one.
     * <p>
     * The path is first sanitized using the {@link #sanitize(String)} method.
     *
     * @param pathUrl the URL path to append a slash to
     * @return the URL path with a trailing slash
     */
    public String appendSlash(String pathUrl) {
        pathUrl = sanitize(pathUrl);
        if (StringUtils.isBlank(pathUrl) || pathUrl.equals(URL_PATH_SEPARATOR)) {
            return pathUrl;
        } else {
            return pathUrl.endsWith(URL_PATH_SEPARATOR) ? pathUrl : pathUrl + URL_PATH_SEPARATOR;
        }
    }
}
