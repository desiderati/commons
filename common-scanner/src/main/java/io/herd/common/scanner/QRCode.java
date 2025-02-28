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
package io.herd.common.scanner;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Getter
@EqualsAndHashCode(of = "id")
@ToString(exclude = "image")
public class QRCode implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private UUID id;

    private String content;

    @Transient
    @Setter(AccessLevel.PACKAGE)
    private byte[] image;

    /**
     * Generates a QRCode with content as UUID.
     */
    QRCode() {
        setId(UUID.randomUUID());
    }

    QRCode(String content) {
        id = UUID.randomUUID();
        this.content = content;
    }

    public void setId(UUID id) {
        this.id = id;
        this.content = id.toString();
    }
}
