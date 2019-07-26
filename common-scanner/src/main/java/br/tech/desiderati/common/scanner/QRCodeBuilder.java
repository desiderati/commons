/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.scanner;

import br.tech.desiderati.common.exception.ApplicationException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

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

    private QRCode qrCode;

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
