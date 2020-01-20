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
package io.herd.common.security.sign_request.authorization;

import org.apache.commons.io.IOUtils;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;

/**
 * Since we need to read the entire request body to validate the signature, if we don't use this wrapper,
 * the application will throw an exception when trying to read the body again.
 */
public class SignRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] body;

    public SignRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
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