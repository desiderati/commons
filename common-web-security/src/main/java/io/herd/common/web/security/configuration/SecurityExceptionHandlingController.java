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
package io.herd.common.web.security.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.herd.common.web.rest.exception.ExceptionHandlingController;
import io.herd.common.web.rest.exception.ResponseExceptionDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ControllerAdvice(annotations = {RestController.class, RepositoryRestController.class})
public class SecurityExceptionHandlingController extends ExceptionHandlingController {

    @Autowired
    public SecurityExceptionHandlingController(
        @Value("${spring.web.exception-handler.should-log-as-warning}") @NotEmpty List<@NotBlank String> shouldLogAsWarning,
        ObjectMapper objectMapper,
        ModelMapper modelMapper
    ) {
        super(shouldLogAsWarning, objectMapper, modelMapper);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseExceptionDTO> handleAccessDeniedException(
        HttpServletRequest request, AccessDeniedException ex
    ) {
        return handleHttpErrorException(request, HttpStatus.FORBIDDEN, ex);
    }
}
