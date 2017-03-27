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
 
#ifndef COM_SANDFLOW_SMPTE_UTIL_UMID_H
#define COM_SANDFLOW_SMPTE_UTIL_UMID_H

#include <string>
#include <cstring>

class UMID {

public:

	/*
	 * Members
	 */

	unsigned char value[32];

	/*
	* Methods
	*/


	static std::string bytesToString(const unsigned char uuid[32]);

	UMID(const unsigned char umid[32]) {
		memcpy(this->value, umid, 32);
	}

	UMID() {
		memset(this->value, 0, 32);
	}

	std::string to_string() const {
		return bytesToString(this->value);
	}


	UMID& operator=(const unsigned char src[]) {

		std::memcpy(this->value, src, 32);

		return *this;
	}

	bool operator<(const UMID& other) const {

		return std::memcmp(this->value, other.value, 32) < 0;

	}

	
};

#endif