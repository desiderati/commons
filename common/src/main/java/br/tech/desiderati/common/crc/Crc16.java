/*
 * Copyright (c) 2019 - Felipe Desiderati
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
package br.tech.desiderati.common.crc;

/**
 * Copied from http://crccalc.com/
 */
@SuppressWarnings("unused")
public class Crc16 {

    public static CrcAlgorithm Crc16CcittFalse = new CrcAlgorithm("CRC-16/CCITT-FALSE", 16, 0x1021, 0xFFFF, false, false, 0x0, 0x29B1);
    public static CrcAlgorithm Crc16Arc = new CrcAlgorithm("CRC-16/ARC", 16, 0x8005, 0x0, true, true, 0x0, 0xBB3D);
    public static CrcAlgorithm Crc16AugCcitt = new CrcAlgorithm("CRC-16/AUG-CCITT", 16, 0x1021, 0x1D0F, false, false, 0x0, 0xE5CC);
    public static CrcAlgorithm Crc16Buypass = new CrcAlgorithm("CRC-16/BUYPASS", 16, 0x8005, 0x0, false, false, 0x0, 0xFEE8);
    public static CrcAlgorithm Crc16Cdma2000 = new CrcAlgorithm("CRC-16/CDMA2000", 16, 0xC867, 0xFFFF, false, false, 0x0, 0x4C06);
    public static CrcAlgorithm Crc16Dds110 = new CrcAlgorithm("CRC-16/DDS-110", 16, 0x8005, 0x800D, false, false, 0x0, 0x9ECF);
    public static CrcAlgorithm Crc16DectR = new CrcAlgorithm("CRC-16/DECT-R", 16, 0x589, 0x0, false, false, 0x1, 0x7E);
    public static CrcAlgorithm Crc16DectX = new CrcAlgorithm("CRC-16/DECT-X", 16, 0x589, 0x0, false, false, 0x0, 0x7F);
    public static CrcAlgorithm Crc16Dnp = new CrcAlgorithm("CRC-16/DNP", 16, 0x3D65, 0x0, true, true, 0xFFFF, 0xEA82);
    public static CrcAlgorithm Crc16En13757 = new CrcAlgorithm("CRC-16/EN-13757", 16, 0x3D65, 0x0, false, false, 0xFFFF, 0xC2B7);
    public static CrcAlgorithm Crc16Genibus = new CrcAlgorithm("CRC-16/GENIBUS", 16, 0x1021, 0xFFFF, false, false, 0xFFFF, 0xD64E);
    public static CrcAlgorithm Crc16Maxim = new CrcAlgorithm("CRC-16/MAXIM", 16, 0x8005, 0x0, true, true, 0xFFFF, 0x44C2);
    public static CrcAlgorithm Crc16Mcrf4Xx = new CrcAlgorithm("CRC-16/MCRF4XX", 16, 0x1021, 0xFFFF, true, true, 0x0, 0x6F91);
    public static CrcAlgorithm Crc16Riello = new CrcAlgorithm("CRC-16/RIELLO", 16, 0x1021, 0xB2AA, true, true, 0x0, 0x63D0);
    public static CrcAlgorithm Crc16T10Dif = new CrcAlgorithm("CRC-16/T10-DIF", 16, 0x8BB7, 0x0, false, false, 0x0, 0xD0DB);
    public static CrcAlgorithm Crc16Teledisk = new CrcAlgorithm("CRC-16/TELEDISK", 16, 0xA097, 0x0, false, false, 0x0, 0xFB3);
    public static CrcAlgorithm Crc16Tms37157 = new CrcAlgorithm("CRC-16/TMS37157", 16, 0x1021, 0x89EC, true, true, 0x0, 0x26B1);
    public static CrcAlgorithm Crc16Usb = new CrcAlgorithm("CRC-16/USB", 16, 0x8005, 0xFFFF, true, true, 0xFFFF, 0xB4C8);
    public static CrcAlgorithm CrcA = new CrcAlgorithm("CRC-A", 16, 0x1021, 0xc6c6, true, true, 0x0, 0xBF05);
    public static CrcAlgorithm Crc16Kermit = new CrcAlgorithm("CRC-16/KERMIT", 16, 0x1021, 0x0, true, true, 0x0, 0x2189);
    public static CrcAlgorithm Crc16Modbus = new CrcAlgorithm("CRC-16/MODBUS", 16, 0x8005, 0xFFFF, true, true, 0x0, 0x4B37);
    public static CrcAlgorithm Crc16X25 = new CrcAlgorithm("CRC-16/X-25", 16, 0x1021, 0xFFFF, true, true, 0xFFFF, 0x906E);
    public static CrcAlgorithm Crc16Xmodem = new CrcAlgorithm("CRC-16/XMODEM", 16, 0x1021, 0x0, false, false, 0x0, 0x31C3);

}
