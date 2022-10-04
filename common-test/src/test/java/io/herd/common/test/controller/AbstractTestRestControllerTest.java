/*
 * Copyright (c) 2022 - Felipe Desiderati
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
package io.herd.common.test.controller;

import io.herd.common.test.annotation.AutoConfigureCommonWeb;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractTestRestControllerTest {

    /**
     * If you don't define this parameter but have configured the API's base path in your application,
     * you must use the {@link AutoConfigureCommonWeb} annotation, if in your tests you have set the URL
     * with the API base path directly.
     *
     * @see TestRestControllerWebMvcTest2
     * @see TestRestControllerWebMvcTest3
     */
    @Value("${app.api-base-path:}")
    private String apiBasePath;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnInfoController() throws Exception {
        MvcResult result =
            mockMvc.perform(get(apiBasePath + "/v1/test-rest-controller/info-controller"))
                .andExpect(status().is(200)).andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body).isEqualTo("Test Rest Controller");
    }

    @Test
    void shouldReturnInfoService() throws Exception {
        MvcResult result =
            mockMvc.perform(get(apiBasePath + "/v1/test-rest-controller/info-service"))
                .andExpect(status().is(200)).andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body).isEqualTo("Test Service");
    }
}
