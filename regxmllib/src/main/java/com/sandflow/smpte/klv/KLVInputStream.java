/*
 * Copyright (c) 2014, Pierre-Anthony Lemieux (pal@sandflow.com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.sandflow.smpte.klv;

import com.sandflow.smpte.klv.exceptions.KLVException;
import static com.sandflow.smpte.klv.exceptions.KLVException.MAX_LENGTH_EXCEEED;
import com.sandflow.smpte.util.UL;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * KLVInputStream allows KLV data structures to be read from an InputStream
 */
public class KLVInputStream extends InputStream implements DataInput {
    
    public enum ByteOrder {
        LITTLE_ENDIAN,
        BIG_ENDIAN
    }
    
    private DataInputStream dis;
    private ByteOrder byteorder;

    /**
     * Assumes big endian byte ordering.
     * 
     * @param is InputStream to read from
     */
    public KLVInputStream(InputStream is) {
        this(is, ByteOrder.BIG_ENDIAN);
    }
    
    /**
     * Allows the byte ordering to be specified.
     * 
     * @param is InputStream to read from
     * @param byteorder Byte ordering of the file
     */
    public KLVInputStream(InputStream is, ByteOrder byteorder) {
        dis = new DataInputStream(is);
        this.byteorder = byteorder;
    }

    /**
     * Byte order of the stream.
     * 
     * @return Byte order of the stream
     */
    public ByteOrder getByteorder() {
        return byteorder;
    }
    
    /**
     * Reads a single UL.
     * 
     * @return UL
     * @throws IOException
     * @throws EOFException 
     */
    public UL readUL() throws IOException, EOFException {
        byte[] ul = new byte[16];

        if (read(ul) < ul.length) {
            throw new EOFException();
        }
        
        return new UL(ul);
    }

    /**
     * Reads a single BER-encoded length. The maximum length of the encoded length is 8 bytes.
     * 
     * @return Length
     * @throws EOFException
     * @throws IOException
     * @throws KLVException 
     */
    public long readBERLength() throws EOFException, IOException, KLVException {

        long val = 0;

        int b = read();

        if (b <= 0) {
            throw new EOFException();
        }
        
        if ((b & 0x80) == 0) {
           return b;
        }

        int bersz =  (b & 0x0f);

        if (bersz > 8) {
            throw new KLVException(MAX_LENGTH_EXCEEED);
        }

        byte[] octets = new byte[bersz];

        if (read(octets) < bersz) {
            throw new EOFException();
        }

        for (int i = 0; i < bersz; i++) {
            int tmp = (((int) octets[i]) & 0xFF);
                val = (val << 8) + tmp;
                
                if (val > Integer.MAX_VALUE) {
                    throw new KLVException(MAX_LENGTH_EXCEEED);
                }
        }

        return val;
    }

    /**
     * Reads a single KLV triplet.
     * 
     * @return KLV Triplet
     * @throws IOException
     * @throws EOFException
     * @throws KLVException 
     */
    public Triplet readTriplet() throws IOException, EOFException, KLVException {
        UL ul = readUL();

        long len = readBERLength();

        if (len > Integer.MAX_VALUE) {
            throw new KLVException(MAX_LENGTH_EXCEEED);
        }

        byte[] value = new byte[(int) len];

        if (len != read(value)) {
            throw new EOFException("EOF reached while reading Value.");
        }

        return new MemoryTriplet(ul, value);
    }

    @Override
    public final int read(byte[] bytes) throws IOException {
        return dis.read(bytes);
    }

    @Override
    public final int read(byte[] bytes, int i, int i1) throws IOException {
        return dis.read(bytes, i, i1);
    }

    @Override
    public final void readFully(byte[] bytes) throws IOException {
        dis.readFully(bytes);
    }

    @Override
    public final void readFully(byte[] bytes, int i, int i1) throws IOException {
        dis.readFully(bytes, i, i1);
    }

    @Override
    public final int skipBytes(int i) throws IOException {
        return dis.skipBytes(i);
    }

    @Override
    public final boolean readBoolean() throws IOException {
        return dis.readBoolean();
    }

    @Override
    public final byte readByte() throws IOException {
        return dis.readByte();
    }

    @Override
    public final int readUnsignedByte() throws IOException {
        return dis.readUnsignedByte();
    }

    @Override
    public final short readShort() throws IOException {
        
        if (byteorder == ByteOrder.BIG_ENDIAN) {
        
            return dis.readShort();
            
        } else {
            
            int lo = readUnsignedByte();
            int hi = readUnsignedByte();
            
            return (short) (lo + (hi << 8));
            
        }
    }

    @Override
    public final int readUnsignedShort() throws IOException {
        
        if (byteorder == ByteOrder.BIG_ENDIAN) {
        
            return dis.readUnsignedShort();
            
        } else {
            
            int lo = readUnsignedByte();
            int hi = readUnsignedByte();
            
            return lo + hi << 8;
            
        }
    }

    @Override
    public final char readChar() throws IOException {
        return dis.readChar();
    }

    @Override
    public final int readInt() throws IOException {
        
        if (byteorder == ByteOrder.BIG_ENDIAN) {
        
            return dis.readInt();
            
        } else {
            
            int b0 = readUnsignedByte();
            int b1 = readUnsignedByte();
            int b2 = readUnsignedByte();
            int b3 = readUnsignedByte();
            
            return b0 + (b1 << 8) + (b2 << 16) + (b3 << 24);
            
        }
        
    }
    
    public long readUnsignedInt() throws IOException, EOFException {
        
        if (byteorder == ByteOrder.BIG_ENDIAN) {
        
            return ((long) dis.readInt()) & 0xFFFF;
            
        } else {
            
            int b0 = readUnsignedByte();
            int b1 = readUnsignedByte();
            int b2 = readUnsignedByte();
            int b3 = readUnsignedByte();
            
            return ((long) b0 + (b1 << 8) + (b2 << 16) + (b3 << 24)) & 0xFFFF;
            
        }
        
    }

    @Override
    public final long readLong() throws IOException {
        
        if (byteorder == ByteOrder.BIG_ENDIAN) {
        
            return dis.readLong();
            
        } else {
            
            int b0 = readUnsignedByte();
            int b1 = readUnsignedByte();
            int b2 = readUnsignedByte();
            int b3 = readUnsignedByte();
            int b4 = readUnsignedByte();
            int b5 = readUnsignedByte();
            int b6 = readUnsignedByte();
            int b7 = readUnsignedByte();
            
            return b0 + (b1 << 8) + (b2 << 16) + (b3 << 24) + (b4 << 32) + (b5 << 40) + (b6 << 48) + (b7 << 56);
            
        }
    }

    @Override
    public final float readFloat() throws IOException {
        return dis.readFloat();
    }

    @Override
    public final double readDouble() throws IOException {
        return dis.readDouble();
    }

    @Override
    public final String readLine() throws IOException {
        return dis.readLine();
    }

    @Override
    public final String readUTF() throws IOException {
        return dis.readUTF();
    }

    public static final String readUTF(DataInput di) throws IOException {
        return DataInputStream.readUTF(di);
    }

    @Override
    public int read() throws IOException {
        return dis.read();
    }

    @Override
    public long skip(long l) throws IOException {
        return dis.skip(l);
    }

    @Override
    public int available() throws IOException {
        return dis.available();
    }

    @Override
    public void close() throws IOException {
        dis.close();
    }

    @Override
    public synchronized void mark(int i) {
        dis.mark(i);
    }

    @Override
    public synchronized void reset() throws IOException {
        dis.reset();
    }

    @Override
    public boolean markSupported() {
        return dis.markSupported();
    }
    
    protected static final void swap(byte[] array, int i, int j) {
        byte tmp = array[i];
        array[i] = array[j];
        array[j] = tmp;
    }
    
    protected static final void uuidLEtoBE(byte[] uuid) {
            /* swap the 32-bit word of the UUID */
        swap(uuid, 0, 3);
        swap(uuid, 1, 2);

            /* swap the first 16-bit word of the UUID */
        swap(uuid, 4, 5);

            /* swap the second 16-bit word of the UUID */
        swap(uuid, 6, 7);

    }
    
}
