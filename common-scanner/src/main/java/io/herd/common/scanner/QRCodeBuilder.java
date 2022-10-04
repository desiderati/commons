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
package io.herd.common.scanner;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import io.herd.common.exception.ApplicationException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

@SuppressWarnings("unused")
public class QRCodeBuilder {

    private static final int DEFAULT_DIMENSION = 250;

    private final QRCode qrCode;

    private int width = DEFAULT_DIMENSION;

    private int height = DEFAULT_DIMENSION;

    private QRCodeBuilder() {
        qrCode = new QRCode();
    }

    private QRCodeBuilder(String content) {
        qrCode = new QRCode(content);
    }

    public static QRCodeBuilder withContentAsUUID() {
        return new QRCodeBuilder();
    }

    public static QRCodeBuilder withContent(String content) {
        return new QRCodeBuilder(content);
    }

    public QRCodeBuilder withDimensions(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public QRCode build() {

        String fileType = "png";

        Map<EncodeHintType, Object> hintMap = new EnumMap<>(EncodeHintType.class);
        hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        // Now with zxing version 3.2.1 you could change border size (white border size to just 1)
        hintMap.put(EncodeHintType.MARGIN, 1); /* default = 4 */
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix byteMatrix;
        try {
            byteMatrix = qrCodeWriter.encode(qrCode.getContent(), BarcodeFormat.QR_CODE, width, height, hintMap);
        } catch (WriterException e) {
            throw new ApplicationException("Unable to generate QR Code image!", e);
        }

        int newWidth = byteMatrix.getWidth();
        int newHeight = byteMatrix.getHeight();
        BufferedImage image = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        image.createGraphics();

        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, newWidth, newHeight);
        graphics.setColor(Color.BLACK);

        for (int i = 0; i < newWidth; i++) {
            for (int j = 0; j < newHeight; j++) {
                if (byteMatrix.get(i, j)) {
                    graphics.fillRect(i, j, 1, 1);
                }
            }
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, fileType, baos);
            qrCode.setImage(baos.toByteArray());
            return qrCode;
        } catch (IOException e) {
            throw new ApplicationException("Unable to generate QR Code image!", e);
        }
    }
}
