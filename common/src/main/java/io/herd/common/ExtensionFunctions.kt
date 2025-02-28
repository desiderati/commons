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
@file:Suppress("unused")

package io.herd.common

import com.natpryce.hamkrest.Matcher
import java.text.Normalizer

fun String.stripAccents(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD).let {
        Regex("\\p{InCombiningDiacriticalMarks}+").replace(it, "")
            .replace("[^a-zA-Z0-9]", "")
    }

fun <T> List<T>.copyOf(): List<T> = mutableListOf<T>().also { it.addAll(this) }

fun <T> List<T>.mutableCopyOf(): MutableList<T> = mutableListOf<T>().also { it.addAll(this) }

fun <T> Iterable<T>.containsAll(vararg elements : T): Boolean {
    return elements.all { this.contains(it) }
}

fun <T> not(that: Matcher<T>): Matcher<T> = Matcher.Negation(that)

inline fun not(block: () -> Boolean): Boolean = !block()

infix fun CharSequence?.contentNotEquals(other: CharSequence?): Boolean = not { contentEquals(other) }
