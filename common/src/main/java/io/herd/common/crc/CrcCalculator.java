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

package io.herd.common.crc;

/**
 * Copied from http://crccalc.com/
 */
@SuppressWarnings("unused")
public class CrcCalculator {

    private CrcAlgorithm crcAlgorithm;
    private byte hashSize;
    private long mask = 0xFFFFFFFFFFFFFFFFL;
    private long[] table = new long[256];

    public CrcCalculator(CrcAlgorithm crcAlgorithm) {
        this.crcAlgorithm = crcAlgorithm;
        hashSize = (byte) crcAlgorithm.hashSize;
        if (hashSize < 64) {
            mask = (1L << hashSize) - 1;
        }
        createTable();
    }

    public long calc(byte[] data, int offset, int length) {
        long init = crcAlgorithm.refOut ? reverseBits(crcAlgorithm.init, hashSize) : crcAlgorithm.init;
        long hash = computeCrc(init, data, offset, length);
        return (hash ^ crcAlgorithm.xorOut) & mask;
    }

    private long computeCrc(long init, byte[] data, int offset, int length) {
        long crc = init;
        if (crcAlgorithm.refOut) {
            for (int i = offset; i < offset + length; i++) {
                crc = (table[(int) ((crc ^ data[i]) & 0xFF)] ^ (crc >>> 8));
                crc &= mask;
            }
        } else {
            int toRight = (hashSize - 8);
            toRight = Math.max(toRight, 0);
            for (int i = offset; i < offset + length; i++) {
                crc = (table[(int) (((crc >> toRight) ^ data[i]) & 0xFF)] ^ (crc << 8));
                crc &= mask;
            }
        }
        return crc;
    }

    private void createTable() {
        for (int i = 0; i < table.length; i++) {
            table[i] = createTableEntry(i);
        }
    }

    private long createTableEntry(int index) {
        long r = index;
        if (crcAlgorithm.refIn) {
            r = reverseBits(r, hashSize);
        } else if (hashSize > 8) {
            r <<= (hashSize - 8);
        }

        long lastBit = (1L << (hashSize - 1));
        for (int i = 0; i < 8; i++) {
            if ((r & lastBit) != 0) {
                r = ((r << 1) ^ crcAlgorithm.poly);
            } else {
                r <<= 1;
            }
        }

        if (crcAlgorithm.refOut) {
            r = reverseBits(r, hashSize);
        }
        return r & mask;
    }

    private long reverseBits(long ul, int valueLength) {
        long newValue = 0;
        for (int i = valueLength - 1; i >= 0; i--) {
            newValue |= (ul & 1) << i;
            ul >>= 1;
        }
        return newValue;
    }
}