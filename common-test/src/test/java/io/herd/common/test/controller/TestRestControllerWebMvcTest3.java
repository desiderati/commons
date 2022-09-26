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
package io.herd.common.test.controller;

import io.herd.common.test.service.TestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 *  Other option will be removing the API's base path from URL.
 */
//@AutoConfigureCommonWeb // Without it, the controllers will not be prefixed!
@WebMvcTest(controllers = TestRestController.class)
class TestRestControllerWebMvcTest3 {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TestService testServiceMock;

    @BeforeEach
    void setup() {
        Mockito.when(testServiceMock.info()).thenReturn("Test Service");
    }

    @Test
    void shouldReturnInfoController() throws Exception {
        MvcResult result =
            mockMvc.perform(get("/v1/test-rest-controller/info-controller"))
                .andExpect(status().is(200)).andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body).isEqualTo("Test Rest Controller");
    }

    @Test
    void shouldReturnInfoService() throws Exception {
        MvcResult result =
            mockMvc.perform(get("/v1/test-rest-controller/info-service"))
                .andExpect(status().is(200)).andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body).isEqualTo("Test Service");
    }
}
