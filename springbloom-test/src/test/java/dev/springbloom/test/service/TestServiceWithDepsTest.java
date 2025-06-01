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
package dev.springbloom.test.service;

import dev.springbloom.test.MockitoLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 *    ___           __  _      __   _____          __          __     __                ___
 *   / _ \___ _____/ /_(_)__ _/ /  / ___/__  ___  / /______ __/ /_   / /  ___  ___ ____/ (_)__  ___ _
 *  / ___/ _ `/ __/ __/ / _ `/ /  / /__/ _ \/ _ \/ __/ -_) \ / __/  / /__/ _ \/ _ `/ _  / / _ \/ _ `/
 * /_/   \_,_/_/  \__/_/\_,_/_/   \___/\___/_//_/\__/\__/_\_\\__/  /____/\___/\_,_/\_,_/_/_//_/\_, /
 *                                                                                            /___/
 */
@SpringBootTest
@ContextConfiguration(loader = MockitoLoader.class, classes = TestServiceWithDeps.class)
class TestServiceWithDepsTest {

    @Autowired
    private TestServiceWithDeps testServiceWithDeps;

    @Autowired // Will be a Mock!
    private TestServiceDep1 testServiceDep1;

    @Autowired // Will be a Mock!
    private TestServiceDep2 testServiceDep2;

    @BeforeEach
    void setup() {
        when(testServiceDep1.info()).thenReturn("Test1");
        when(testServiceDep2.info()).thenReturn("Test2");
    }

    @Test
    public void shouldNotBeNull() {
        assertThat(testServiceWithDeps.info()).isEqualTo("Test1 - Test2");
    }
}
