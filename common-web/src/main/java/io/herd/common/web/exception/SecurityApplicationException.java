/*
 * Copyright (c) 2022 - Felipe Desiderati
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
package io.herd.common.web.exception;

import io.herd.common.exception.ApplicationException;

import java.io.Serializable;

/**
 * This exception will be treated as {@link org.springframework.http.HttpStatus#UNAUTHORIZED}.
 */
@SuppressWarnings("unused")
public class SecurityApplicationException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public SecurityApplicationException(String message) {
        super(message);
    }

    public SecurityApplicationException(String message, Serializable... args) {
        super(message, args);
    }

    public SecurityApplicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SecurityApplicationException(String message, Throwable cause, Serializable... args) {
        super(message, cause, args);
    }
}