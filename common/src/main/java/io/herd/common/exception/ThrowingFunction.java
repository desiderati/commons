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
package io.herd.common.exception;

import java.util.function.Function;

/**
 * @see ThrowingConsumer
 */
@FunctionalInterface
@SuppressWarnings("unused")
public interface ThrowingFunction<T, R> {

    @SuppressWarnings("squid:S00112")
    R apply(T t) throws Exception;

    static <T, R> Function<T, R> silently(ThrowingFunction<T, R> f) {
        return t -> {
            try {
                return f.apply(t);
            } catch (Exception ex) {
                return ThrowingFunction.sneakyThrow(ex);
            }
        };
    }

    @SuppressWarnings("unchecked")
    static <E extends Exception, R> R sneakyThrow(Exception e) throws E {
        throw (E) e;
    }
}
