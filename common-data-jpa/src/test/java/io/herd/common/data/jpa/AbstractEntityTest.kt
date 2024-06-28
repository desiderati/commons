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
package io.herd.common.data.jpa

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.cglib.proxy.Enhancer
import org.springframework.cglib.proxy.MethodInterceptor

@Suppress("LoggingSimilarMessage")
class AbstractEntityTest {

    companion object {
        private val logger = LoggerFactory.getLogger(AbstractEntityTest::class.java)
    }

    @Test
    fun shouldObjectsBeEqualsWithNotNullId() {
        val classePai1 = ClassePai(1L)
        val classePai2 = ClassePai(1L)
        assertTrue(classePai1 == classePai2)

        val enhancer = Enhancer()
        enhancer.setSuperclass(ClassePai::class.java)
        enhancer.setCallback(MethodInterceptor { obj, method, args, proxy ->
            val ret = proxy.invokeSuper(obj, args)
            logger.info("Invoked method: {}, with return: {}", method.name, ret)
            ret
        })
        val classePai3: ClassePai = enhancer.create(arrayOf(java.lang.Long::class.java), arrayOf(1L)) as ClassePai
        assertTrue(classePai1 == classePai3)

        val classeFilha1 = ClasseFilha(1L, "Filha 1")
        assertTrue(classePai1 == classeFilha1)
    }

    @Test
    fun shouldObjectsBeEqualsWithNullId() {
        val classePai1 = ClassePai()
        val classePai2 = ClassePai()
        assertTrue(classePai1 == classePai2)

        val enhancer = Enhancer()
        enhancer.setSuperclass(ClassePai::class.java)
        enhancer.setCallback(MethodInterceptor { obj, method, args, proxy ->
            val ret = proxy.invokeSuper(obj, args)
            logger.info("Invoked method: {}, with return: {}", method.name, ret)
            ret
        })
        val classePai3: ClassePai = enhancer.create() as ClassePai
        assertTrue(classePai1 == classePai3)

        val classeFilha1 = ClasseFilha(name = "Filha 1")
        assertTrue(classePai1 == classeFilha1)
    }

    @Test
    fun shouldObjectsNotBeEquals() {
        val classePai1 = ClassePai(1L)
        val classePai2 = ClassePai(2L)
        assertFalse(classePai1 == classePai2)

        val enhancer = Enhancer()
        enhancer.setSuperclass(ClassePai::class.java)
        enhancer.setCallback(MethodInterceptor { obj, method, args, proxy ->
            val ret = proxy.invokeSuper(obj, args)
            logger.info("Invoked method: {}, with return: {}", method.name, ret)
            ret
        })
        val classePai3: ClassePai = enhancer.create(arrayOf(java.lang.Long::class.java), arrayOf(2L)) as ClassePai
        assertFalse(classePai1 == classePai3)

        val classeFilha1 = ClasseFilha(2L, "Filha 1")
        assertFalse(classePai1 == classeFilha1)
    }
}

internal open class ClassePai(private var id: Long? = null) : AbstractEntity<Long>() {
    override fun getId(): Long? {
        return id
    }
}

internal class ClasseFilha(
    id: Long? = null,
    @Suppress("UNUSED_PARAMETER") name: String? = null
) : ClassePai(id)
