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

#include "KLVStream.h"
#include <climits>

template<class CharT, class Traits>
void basic_klvistream<CharT, Traits>::readTriplet(MemoryTriplet &t)
{
	
	t.fromStream(*this);
	
}


template void basic_klvistream<char>::readTriplet(MemoryTriplet &t);


template<class CharT, class Traits>
AUID basic_klvistream<CharT, Traits>::readAUID() {

	unsigned char buf[16];

	this->read((char*)buf, 16);

	if (this->good()) {

		return AUID(buf);

	} else {

		throw std::ios_base::failure("AUID read failed");

	}

}

template<class CharT, class Traits>
UL basic_klvistream<CharT, Traits>::readUL()
{
	unsigned char buf[16];

	this->read((char*)buf, 16);

	if (this->good()) {

		return UL(buf);

	} else {

		throw std::ios_base::failure("UL read failed");

	}
}


template<class CharT, class Traits>
unsigned long int basic_klvistream<CharT, Traits>::readBERLength()
{
	unsigned long long int val = 0;

	unsigned char b;

	this->read((char*) &b, 1);

	if (! this->good()) throw std::ios_base::failure("BER read failed");

	if ((b & 0x80) == 0) {
		return b;
	}

	int bersz = (b & 0x0f);

	if (bersz > 8) {
		this->setstate(std::ios_base::failbit);

		throw std::ios_base::failure("Max BER length exceeded");
	}

	unsigned char *buf = new unsigned char[bersz];

	this->read((char*) buf, bersz);

	if (this->gcount() < bersz) {
		return 0;
	}

	for (int i = 0; i < bersz; i++) {

		val = (val << 8) + buf[i];

		if (val > ULONG_MAX) {
			this->setstate(std::ios_base::failbit);

			throw std::ios_base::failure("Max BER value exceeded");
		}
	}

	return (unsigned long int) val;
}


template<class CharT, class Traits>
unsigned char basic_klvistream<CharT, Traits>::readUnsignedByte()
{
	unsigned char c;

	this->read((char*)&c, 1);

	if (!this->good()) throw std::ios_base::failure("Unsigned byte read failed");

	return c;
}

template<class CharT, class Traits>
char basic_klvistream<CharT, Traits>::readByte()
{
	char c;

	this->read(&c, 1);

	if (!this->good()) throw std::ios_base::failure("Byte read failed");

	return c;
}

template<class CharT, class Traits>
unsigned short basic_klvistream<CharT, Traits>::readUnsignedShort()
{
	unsigned char c[2];

	this->read((char*)&c, 2);

	if (!this->good()) throw std::ios_base::failure("Unsigned short read failed");

	if (byteorder == KLVStream::BIG_ENDIAN_BYTE_ORDER) {

		return ((uint16_t) c[1]) | ((uint16_t) c[0] << 8);

	} else {

		return ((uint16_t) c[0]) | ((uint16_t) c[1] << 8);

	}

}

template<class CharT, class Traits>
short basic_klvistream<CharT, Traits>::readShort()
{
	unsigned char c[2];

	this->read((char*)&c, 2);

	if (!this->good()) throw std::ios_base::failure("Short read failed");

	if (byteorder == BIG_ENDIAN_BYTE_ORDER) {

		return ((int16_t)c[1]) | ((int16_t)c[0] << 8);

	}
	else {

		return ((int16_t)c[0]) | ((int16_t)c[1] << 8);

	}

}

template<class CharT, class Traits>
unsigned long basic_klvistream<CharT, Traits>::readUnsignedLong()
{
	unsigned char c[4];

	this->read((char*)&c, 4);

	if (!this->good()) throw std::ios_base::failure("Unsigned long read failed");

	if (byteorder == KLVStream::BIG_ENDIAN_BYTE_ORDER) {

		return (uint32_t) c[3] | ((uint32_t) c[2] << 8) | ((uint32_t) c[1] << 16) | ((uint32_t) c[0] << 24);

	} else {

		return (uint32_t) c[0] | ((uint32_t) c[1] << 8) | ((uint32_t) c[2] << 16) | ((uint32_t) c[3] << 24);

	}
}

template<class CharT, class Traits>
long basic_klvistream<CharT, Traits>::readLong()
{
	unsigned char c[4];

	this->read((char*)&c, 4);

	if (!this->good()) throw std::ios_base::failure("Long read failed");

	if (byteorder == KLVStream::BIG_ENDIAN_BYTE_ORDER) {

		return (int32_t)c[3] | ((int32_t)c[2] << 8) | ((int32_t)c[1] << 16) | ((int32_t)c[0] << 24);

	} else {

		return (int32_t) c[0] | ((int32_t)c[1] << 8) | ((int32_t)c[2] << 16) | ((int32_t)c[3] << 24);

	}
}

template<class CharT, class Traits>
long long basic_klvistream<CharT, Traits>::readLongLong()
{
	unsigned char c[8];

	this->read((char*)&c, 8);

	if (!this->good()) throw std::ios_base::failure("Long long read failed");

	if (byteorder == KLVStream::BIG_ENDIAN_BYTE_ORDER) {

		return (int64_t)c[7] | ((int64_t)c[6] << 8) | ((int64_t)c[5] << 16) | ((int64_t)c[4] << 24) | 
			((int64_t)c[3]  << 32)| ((int64_t)c[2] << 40) | ((int64_t)c[1] << 48) | ((int64_t)c[0] << 56);

	} else {

		return (int64_t) c[0] | ((int64_t)c[1] << 8) | ((int64_t)c[2] << 16) | ((int64_t)c[3] << 24) |
			((int64_t) c[4] << 32) | ((int64_t)c[5] << 40) | ((int64_t)c[5] << 48) | ((int64_t)c[7] << 56);

	}
}

template<class CharT, class Traits>
unsigned long long basic_klvistream<CharT, Traits>::readUnsignedLongLong()
{
	return (uint64_t) basic_klvistream<CharT, Traits>::readLongLong();
}

template<class CharT, class Traits>
void basic_klvistream<CharT, Traits>::readBytes(unsigned char * buffer, size_t length) {
	this->read((char*)buffer, length);

	if (!this->good()) throw std::ios_base::failure("Read bytes failed");
}


template class basic_klvistream<char>;