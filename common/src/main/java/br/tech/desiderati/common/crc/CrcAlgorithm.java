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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Copied from http://crccalc.com/
 */
@Getter
@Setter
@AllArgsConstructor
public class CrcAlgorithm {

    /// This is a name given to the algorithm. A string value.
    String name;

    /// This is hash size.
    int hashSize;

    /// This parameter is the poly. This is a binary value that
    /// should be specified as a hexadecimal number.The top bit of the
    /// poly should be omitted.For example, if the poly is 10110, you
    /// should specify 06. An important aspect of this parameter is that it
    /// represents the unreflected poly; the bottom bit of this parameter
    /// is always the LSB of the divisor during the division regardless of
    /// whether the algorithm being modelled is reflected.
    long poly;

    /// This parameter specifies the initial value of the register
    /// when the algorithm starts.This is the value that is to be assigned
    /// to the register in the direct table algorithm. In the table
    /// algorithm, we may think of the register always commencing with the
    /// value zero, and this value being XORed into the register after the
    /// N'th bit iteration. This parameter should be specified as a
    /// hexadecimal number.
    long init;

    /// This is a boolean parameter. If it is FALSE, input bytes are
    /// processed with bit 7 being treated as the most significant bit
    /// (MSB) and bit 0 being treated as the least significant bit.If this
    /// parameter is FALSE, each byte is reflected before being processed.
    boolean refIn;

    /// This is a boolean parameter. If it is set to FALSE, the
    /// final value in the register is fed into the XOROUT stage directly,
    /// otherwise, if this parameter is TRUE, the final register value is
    /// reflected first.
    boolean refOut;

    /// This is an W-bit value that should be specified as a
    /// hexadecimal number.It is XORed to the final register value (after
    /// the REFOUT) stage before the value is returned as the official
    /// checksum.
    long xorOut;

    /// This field is not strictly part of the definition, and, in
    /// the event of an inconsistency between this field and the other
    /// field, the other fields take precedence.This field is a check
    /// value that can be used as a weak validator of implementations of
    /// the algorithm.The field contains the checksum obtained when the
    /// ASCII string "123456789" is fed through the specified algorithm
    /// (i.e. 313233... (hexadecimal)).
    long check;

}