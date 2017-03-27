/*
 * Copyright (c), Pierre-Anthony Lemieux (pal@palemieux.com)
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
 
#ifndef COM_SANDFLOW_SMPTE_KLV_KLVSTREAM_H
#define COM_SANDFLOW_SMPTE_KLV_KLVSTREAM_H

#include "MemoryTriplet.h"
#include <iostream>
#include <com/sandflow/smpte/util/AUID.h>
#include <string>
#include <stdint.h>


template<class CharT, class Traits = std::char_traits<CharT> >
class basic_klvistream : public std::basic_istream<CharT, Traits> {

public:

	enum ByteOrder { BIG_ENDIAN_BYTE_ORDER, LITTLE_ENDIAN_BYTE_ORDER };

	basic_klvistream(std::basic_streambuf<CharT, Traits>* sb, ByteOrder bo = BIG_ENDIAN_BYTE_ORDER) : std::basic_istream<CharT, Traits>(sb), byteorder(bo) {}

	void readTriplet(MemoryTriplet &t);
	AUID readAUID();
	UL readUL();
	unsigned long int readBERLength();
	unsigned char readUnsignedByte();
	char readByte();
	unsigned short int readUnsignedShort();
	short int readShort();
	unsigned long readUnsignedInt() { return readUnsignedLong(); }
	long readLong();
	unsigned long readUnsignedLong();
	long long readLongLong();
	unsigned long long readUnsignedLongLong();

	ByteOrder getByteOrder() const { return this->byteorder; };

private:

	ByteOrder byteorder;

};

typedef basic_klvistream<char> KLVStream;

#endif