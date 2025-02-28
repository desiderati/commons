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
package io.herd.common.validation

import com.natpryce.hamkrest.assertion.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

@Suppress("unused")
class TypedValidationEntityTest :
    Spek({
        val `test object with blank name` = TypedValidationTestObject("")
        val `test object with invalid name` = TypedValidationTestObject("Not a Test")
        val `test object` = TypedValidationTestObject("Test") // Only value possible!

        describe("O exemplo de teste") {
            it("não pode estar com nome em branco") {
                assertThat(`test object with blank name`,
                    hasExactErrors("TestObject.invalidName", "TestObject.invalidNameLength")
                )
            }

            it("está com nome inválido") {
                assertThat(`test object with invalid name`,
                    hasExactErrors("TestObject.invalidName", "TestObject.invalidNameLength")
                )
            }

            it("está válido") {
                assertThat(`test object`, hasNoErrors())
            }
        }
    })
