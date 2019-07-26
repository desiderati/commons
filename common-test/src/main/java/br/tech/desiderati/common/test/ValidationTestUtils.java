/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import br.tech.desiderati.common.exception.ValidationResponseExceptionDTO;
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
            "All validations messages muts be handled!");

        for (String validationMessage : responseExceptionDTO.getValidationMessages()) {
            errorMsgsList.remove(validationMessage);
        }
        assertTrue(errorMsgsList.isEmpty(), "All validations messages muts be handled!");
    }
}
