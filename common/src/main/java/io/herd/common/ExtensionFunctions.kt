@file:Suppress("unused")

package io.herd.common

import java.text.Normalizer

fun String.stripAccents(): String {
    return Normalizer.normalize(this, Normalizer.Form.NFD).let {
        Regex("\\p{InCombiningDiacriticalMarks}+").replace(it, "")
            .replace(" ", "")
    }
}

@Suppress("unused")
fun <T> List<T>.copyOf(): List<T> {
    return mutableListOf<T>().also { it.addAll(this) }
}

fun <T> List<T>.mutableCopyOf(): MutableList<T> {
    return mutableListOf<T>().also { it.addAll(this) }
}
