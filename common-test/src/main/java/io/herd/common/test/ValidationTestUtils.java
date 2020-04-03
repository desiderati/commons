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
package io.herd.common.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.herd.common.exception.ValidationResponseExceptionDTO;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UtilityClass
@SuppressWarnings("unused")
public class ValidationTestUtils {

    private ObjectMapper mapper = new ObjectMapper();

    @SneakyThrows
    public void assertBeanValidation(MvcResult result, String ... errorMsgs) {
        ValidationResponseExceptionDTO responseExceptionDTO =
            mapper.readValue(result.getResponse().getContentAsString(), ValidationResponseExceptionDTO.class);

        List<String> errorMsgsList = new ArrayList<>();
        Collections.addAll(errorMsgsList, errorMsgs);
        assertEquals(responseExceptionDTO.getValidationMessages().length, errorMsgsList.size(),
            "All validations messages must be handled!");

        for (String validationMessage : responseExceptionDTO.getValidationMessages()) {
            errorMsgsList.remove(validationMessage);
        }
        assertTrue(errorMsgsList.isEmpty(), "All validations messages must be handled!");
    }
}
