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
package io.herd.common.web;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("unused")
public class MultipartRequest {

    private static final String LINE_FEED = "\r\n";

    private final String boundary;
    private final HttpURLConnection httpConn;
    private final Charset charset;
    private final OutputStream outputStream;
    private final PrintWriter writer;

    public static MultipartRequest as(String requestURL, String auth) throws IOException {
        return new MultipartRequest(requestURL, auth, StandardCharsets.UTF_8);
    }

    public static MultipartRequest as(String requestURL, String auth, Charset charset) throws IOException {
        return new MultipartRequest(requestURL, auth, charset);
    }

    /**
     * This private constructor initializes a new HTTP POST request with content type
     * is set to multipart/form-data.
     */
    private MultipartRequest(String requestURL, String auth, Charset charset) throws IOException {
        this.charset = charset;

        // Creates a unique boundary based on time stamp!
        boundary = "===" + System.currentTimeMillis() + "===";

        URL url = new URL(requestURL);
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setUseCaches(false);
        httpConn.setDoOutput(true); // Indicates POST method!
        httpConn.setDoInput(true);
        httpConn.setRequestProperty("Authorization", auth);
        httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        outputStream = httpConn.getOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);
    }

    /**
     * Adds a form field to the request.
     *
     * @param name  Field name.
     * @param value Field value.
     */
    public MultipartRequest addFormField(String name, String value) {
        writer.append("--").append(boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"").append(name).append("\"").append(LINE_FEED);
        writer.append("Content-Type: text/plain; charset=").append(charset.name()).append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(value).append(LINE_FEED);
        writer.flush();
        return this;
    }

    /**
     * Adds a upload file section to the request.
     *
     * @param fieldName   Name attribute in <input type="file" name="..." />
     * @param inputStream File to be uploaded.
     */
    public MultipartRequest addFilePart(String fieldName, String fileName, String contentType,
                                        InputStream inputStream) throws IOException {

        writer.append("--").append(boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"").append(fieldName).append("\"; ")
            .append("filename=\"").append(fileName).append("\"").append(LINE_FEED);

        writer.append("Content-Type: ").append(contentType).append("; charset=binary").append(LINE_FEED);
        writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();

        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        outputStream.flush();
        inputStream.close();
        writer.append(LINE_FEED);
        writer.flush();
        return this;
    }

    /**
     * Adds a header field to the request.
     *
     * @param name  Name of the header field.
     * @param value Value of the header field.
     */
    public MultipartRequest addHeader(String name, String value) {
        writer.append(name).append(": ").append(value).append(LINE_FEED);
        writer.flush();
        return this;
    }

    /**
     * Completes the request and receives response from the server.
     *
     * @return A list of Strings as response in case the server returned.
     * Status OK, otherwise an exception is thrown.
     */
    public String send() throws IOException {
        writer.append(LINE_FEED).flush();
        writer.append("--").append(boundary).append("--").append(LINE_FEED);
        writer.close();

        // Checks server's status code first.
        String response;
        int status = httpConn.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
            response = IOUtils.toString(reader);
            reader.close();
            httpConn.disconnect();
        } else {
            throw new IOException("Server returned non-OK status: " + status);
        }

        return response;
    }
}
