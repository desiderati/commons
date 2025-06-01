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
package dev.springbloom.data.jpa

import org.springframework.data.util.ProxyUtils
import java.io.Serializable

abstract class AbstractIdentity<I : Serializable> : Identity<I> {

    @Override
    abstract override fun getId(): I?

    @Override
    override fun equals(other: Any?): Boolean {
        if (null == other) {
            return false
        }

        if (this === other) {
            return true
        }

        if (javaClass != ProxyUtils.getUserClass(other) && other !is AbstractIdentity<*>) {
            return false
        }

        val that = other as AbstractIdentity<*>
        return this.id == that.id
    }

    @Override
    override fun hashCode(): Int {
        var hashCode = 17
        hashCode += 31 * (id?.hashCode() ?: 0)
        return hashCode
    }

    @Override
    override fun toString(): String {
        return this.javaClass.simpleName + "{" + "id=" + this.id + '}'
    }
}
