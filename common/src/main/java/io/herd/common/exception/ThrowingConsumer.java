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
package io.herd.common.exception;

import java.util.function.Consumer;

/**
 * Using the ThrowingConsumer Functional Interface allow us to handle lambda functions
 * which throws checked exceptions, and with this, we didn't have to declare a specific
 * Consumer which handles such checked exception.
 */
@FunctionalInterface
@SuppressWarnings("unused")
public interface ThrowingConsumer<T> {

    @SuppressWarnings("squid:S00112")
    void accept(T t) throws Exception;

    static <T> Consumer<T> silently(ThrowingConsumer<T> f) {
        return t -> {
            try {
                f.accept(t);
            } catch (Exception ex) {
                ThrowingConsumer.sneakyThrow(ex);
            }
        };
    }

    @SuppressWarnings("unchecked")
    static <E extends Exception> void sneakyThrow(Exception e) throws E {
        throw (E) e;
    }
}
