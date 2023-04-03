/*
 * Copyright (c) 2023 - Felipe Desiderati
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
package io.herd.common.web.exception;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.IOException;
import java.util.Collections;

/**
 * This converter was created for cases when the content-type returned by the API is different
 * than {@link MediaType#APPLICATION_JSON}. For these cases, such as {@link MediaType#APPLICATION_OCTET_STREAM},
 * the standard converter was not able to serialize the {@link ResponseExceptionDTO} class
 * when an exception occurred.
 */
public class ResponseExceptionDTOHttpMessageConverter extends AbstractHttpMessageConverter<ResponseExceptionDTO> {

    private final MappingJackson2HttpMessageConverter httpMessageConverter;

    public ResponseExceptionDTOHttpMessageConverter(MappingJackson2HttpMessageConverter httpMessageConverter) {
        this.httpMessageConverter = httpMessageConverter;
        setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
    }

    @Override
    protected boolean supports(@NotNull Class<?> clazz) {
        return ResponseExceptionDTO.class.isAssignableFrom(clazz);
    }

    @Override
    protected void addDefaultHeaders(HttpHeaders headers, @NotNull ResponseExceptionDTO responseExceptionDTO,
            MediaType contentType) throws IOException {

        // It doesn't matter what happened, if there's an error always return as application/json.
        headers.setContentType(MediaType.APPLICATION_JSON);
        super.addDefaultHeaders(headers, responseExceptionDTO, contentType);
    }

    @NotNull
    @Override
    protected ResponseExceptionDTO readInternal(@NotNull Class<? extends ResponseExceptionDTO> clazz,
            @NotNull HttpInputMessage inputMessage) throws IOException {
        return (ResponseExceptionDTO) httpMessageConverter.read(clazz, inputMessage);
    }

    @Override
    protected void writeInternal(@NotNull ResponseExceptionDTO responseExceptionDTO,
            @NotNull HttpOutputMessage outputMessage) throws IOException {
        httpMessageConverter.write(responseExceptionDTO, MediaType.APPLICATION_JSON, outputMessage);
    }
}
