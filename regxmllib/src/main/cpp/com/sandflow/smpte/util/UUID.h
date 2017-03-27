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
 
#ifndef COM_SANDFLOW_SMPTE_UTIL_UUID_H
#define COM_SANDFLOW_SMPTE_UTIL_UUID_H

#include <string>
#include <cstring>

class UUID {

public:

	/*
	 * Members
	 */

	unsigned char value[16];

	/*
	* Methods
	*/

	static bool urnToBytes(const std::string &urn, unsigned char(&uuid)[16]);

	static std::string bytesToString(const unsigned char uuid[16]);

	UUID(const unsigned char uuid[16]) {
		memcpy(this->value, uuid, 16);
	}

	UUID() {
		memset(this->value, 0, 16);
	}

	std::string to_string() const {
		return bytesToString(this->value);
	}


	UUID& operator=(const unsigned char src[]) {

		std::memcpy(this->value, src, 16);

		return *this;
	}

	bool operator<(const UUID& other) const {

		return std::memcmp(this->value, other.value, 16) < 0;

	}

	
};

#endif