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
 
#ifndef COM_SANDFLOW_SMPTE_MXF_MXFSTREAM_H
#define COM_SANDFLOW_SMPTE_MXF_MXFSTREAM_H


#include <iostream>
#include "com/sandflow/smpte/klv/KLVStream.h"
#include "com/sandflow/smpte/util/UUID.h"
#include "com/sandflow/smpte/util/UMID.h"
#include "com/sandflow/smpte/util/IDAU.h"

template<class CharT, class Traits = std::char_traits<CharT> >
class basic_mxfistream : public basic_klvistream<CharT, Traits> {

public:

	basic_mxfistream(std::basic_streambuf<CharT, Traits>* sb, typename basic_klvistream<CharT, Traits>::ByteOrder bo = (basic_klvistream<CharT, Traits>::BIG_ENDIAN_BYTE_ORDER)) : basic_klvistream<CharT, Traits>(sb, bo) {}

	/*void readUUID(UUID &uuid);*/
	UUID readUUID();

	UMID readUMID();

	IDAU readIDAU();

	/**
	* Reads an MXF batch into a Java Collection
	*
	* @param <T> Type of the collection elements
	* @param <W> TripletValueAdapter that is used to convert MXF batch elements into Java collection elements
	* @return Collection of elements of type T
	* @throws KLVException
	* @throws IOException
	*/
	template<class A, class T> std::vector<T> readBatch() {
		std::vector<T> batch;

		unsigned int itemcount = this->readUnsignedLong();
		unsigned int itemlength = this->readUnsignedLong();

		/* TODO: is this necessary */
		/*
		if (itemlength > Integer.MAX_VALUE) {
			throw new KLVException(KLVException.MAX_LENGTH_EXCEEED);
		}
		*/

		unsigned char *value = new unsigned char[itemlength];

		for (unsigned int i = 0; i < itemcount; i++) {

			this->read((char*) value, itemlength);

			batch.push_back(A::fromValue(value, itemlength));

		}

		delete[] value;

		return batch;
	}

};

typedef basic_mxfistream<char> MXFInputStream;



#endif


