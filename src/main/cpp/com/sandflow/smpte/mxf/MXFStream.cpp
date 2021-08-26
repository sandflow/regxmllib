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

#include "MXFStream.h"


namespace rxml {

	void uuidLEtoBE(unsigned char uuid[16]) {
		/* swap the 32-bit word of the UUID */
		std::swap(uuid[0], uuid[3]);
		std::swap(uuid[1], uuid[2]);

		/* swap the first 16-bit word of the UUID */
		std::swap(uuid[4], uuid[5]);

		/* swap the second 16-bit word of the UUID */
		std::swap(uuid[6], uuid[7]);

	}

	template<class CharT, class Traits>
	UUID basic_mxfistream<CharT, Traits>::readUUID()
	{
		unsigned char buf[16];

		this->read((char*)buf, 16);

		if (!this->good()) throw std::ios_base::failure("UUID read failed");

		if (this->getByteOrder() == LITTLE_ENDIAN_BYTE_ORDER) {

			uuidLEtoBE(buf);

		}

		if (this->good()) {

			return UUID(buf);

		} else {

			throw std::ios_base::failure("UUID read failed");

		}
	}

	template<class CharT, class Traits>
	UMID basic_mxfistream<CharT, Traits>::readUMID()
	{
		unsigned char buf[32];

		this->read((char*)buf, sizeof(buf));

		if (!this->good()) throw std::ios_base::failure("UUID read failed");

		if (this->good()) {

			return UMID(buf);

		} else {

			throw std::ios_base::failure("UMID read failed");

		}
	}

	template<class CharT, class Traits>
	IDAU basic_mxfistream<CharT, Traits>::readIDAU()
	{
		unsigned char buf[16];

		this->read((char*)buf, sizeof(buf));

		if (!this->good()) throw std::ios_base::failure("IDAU read failed");

		if (this->getByteOrder() == LITTLE_ENDIAN_BYTE_ORDER) {

			uuidLEtoBE(buf);

		}

		if (this->good()) {

			return IDAU(buf);

		} else {

			throw std::ios_base::failure("IDAU read failed");

		}
	}

	template class basic_mxfistream<char>;
}