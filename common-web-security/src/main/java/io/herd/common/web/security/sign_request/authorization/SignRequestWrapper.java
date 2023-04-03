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
package io.herd.common.web.security.sign_request.authorization;

import org.apache.commons.io.IOUtils;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.*;

/**
 * Since we need to read the entire request body to validate the signature, if we don't use this wrapper,
 * the application will throw an exception when trying to read the body again.
 * <p>
 * <b>org.springframework.http.converter.HttpMessageNotReadableException: Required request body is missing!</b>
 * <p>
 * We can not use the ContentCachingRequestWrapper, since it only supports the following:
 * Content-Type:application/x-www-form-urlencoded
 */
public class SignRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] body;

    public SignRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);

        // We need to execute the method below, because when calling request.getInputStream(),
        // it will mark the request as read (readState = true). And the application will not be
        // able to retrieve the request parameters since it was marked as read.
        // This will load the parameters!
        request.getParameterMap();
        InputStream inputStream = request.getInputStream();
        body = IOUtils.toByteArray(inputStream);
    }

    @Override
    public ServletInputStream getInputStream() {
        return new SignRequestServletInputStream(new ByteArrayInputStream(body));
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }

    /**
     * Use this method to read the request body as much as you need.
     */
    @SuppressWarnings("unused")
    public String getBody() {
        return new String(body);
    }
}
