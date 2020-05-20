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
package io.herd.common.web;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public class UrlUtils {

    // If you remove the static keyword, an compilation error occurs.
    @SuppressWarnings("RedundantModifiersUtilityClassLombok")
    public static final String URL_PATH_SEPARATOR = "/";

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
}
