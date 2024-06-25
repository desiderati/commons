/*
 * Copyright (c) 2024 - Felipe Desiderati
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

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static io.herd.common.exception.ThrowingConsumer.silently;
import static io.herd.common.exception.ThrowingConsumer.sneakyThrow;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ThrowingConsumerTest {

    @Test
    public void testSilently() {
        IOException ioException = assertThrows(IOException.class, () ->
            Arrays.asList(1, 2, 3).forEach(silently(i -> {
                if (i == 3) {
                    throw new IOException("i=" + i);
                }
            }))
        );
        assertThat(ioException.getMessage()).isEqualTo("i=3");
    }

    @Test
    public void testSneakyThrow() {
        assertThrows(IOException.class, () -> sneakyThrow(new IOException()));
    }
}
