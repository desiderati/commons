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
package io.herd.common.web.security.sign_request;

import io.herd.common.exception.ApplicationException;

import java.io.Serial;

/**
 * A custom exception class that represents errors occurring during the signing of requests.
 * <p>
 * This exception is typically used in situations where the signing process for a request cannot be completed
 * due to issues such as invalid input, missing configuration, or internal processing errors.
 * <p>
 * This class extends {@code ApplicationException}, allowing for additional context or arguments to be
 * passed through the exception’s constructors and supporting structured exception handling
 * for request signing operations.
 */
public class SignRequestException extends ApplicationException {

    @Serial
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    public SignRequestException(String msg) {
        super(msg);
    }

    public SignRequestException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
