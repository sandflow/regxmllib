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

#include <com/sandflow/smpte/util/AUID.h>
#include <stdint.h>
#include <iostream>
#include <string>
#include "MemoryTriplet.h"

namespace rxml {

enum ByteOrder { BIG_ENDIAN_BYTE_ORDER, LITTLE_ENDIAN_BYTE_ORDER };

template <class CharT, class Traits = std::char_traits<CharT> >
class basic_klvistream : public std::basic_istream<CharT, Traits> {
 public:
  basic_klvistream(std::basic_streambuf<CharT, Traits>* sb, ByteOrder bo = BIG_ENDIAN_BYTE_ORDER)
      : std::basic_istream<CharT, Traits>(sb), byteorder(bo) {}

  void readTriplet(MemoryTriplet& t);
  AUID readAUID();
  UL readUL();
  unsigned long int readBERLength();
  unsigned char readUnsignedByte();
  char readByte();
  unsigned short int readUnsignedShort();
  short int readShort();
  unsigned long readUnsignedInt() {
    return readUnsignedLong();
  }
  long readLong();
  unsigned long readUnsignedLong();
  long long readLongLong();
  unsigned long long readUnsignedLongLong();

  void readBytes(unsigned char* buffer, size_t length);

  ByteOrder getByteOrder() const {
    return this->byteorder;
  };

 private:
  ByteOrder byteorder;
};

template <class CharT, class Traits = std::char_traits<CharT> >
class basic_klvostream : public std::basic_ostream<CharT, Traits> {
 public:
  basic_klvostream(std::basic_streambuf<CharT, Traits>* sb, ByteOrder bo = BIG_ENDIAN_BYTE_ORDER)
      : std::basic_ostream<CharT, Traits>(sb), byteorder(bo) {}

  void writeTriplet(const Triplet& t);
  void writeAUID(const AUID& auid);
  void writeUL(const UL& ul);
  void writeBERLength(uint64_t length);
  void writeUnsignedByte(uint8_t value);
  void writeByte(int8_t value);
  void writeUnsignedShort(uint16_t value);
  void writeShort(int16_t value);
  void writeUnsignedLong(uint32_t value);
  void writeLong(int32_t value);
  void writeUnsignedLongLong(uint64_t value);
  void writeLongLong(int64_t value);
  void writeBytes(const unsigned char* buffer, size_t length);
  ByteOrder getByteOrder() const {
    return this->byteorder;
  };

 private:
  ByteOrder byteorder;

  void writeUnsignedShortBE(uint16_t value);
  void writeUnsignedLongBE(uint32_t value);
  void writeUnsignedLongLongBE(uint64_t value);
};

typedef basic_klvistream<char> KLVStream;

typedef basic_klvostream<char> KLVOutputStream;

}  // namespace rxml

#endif