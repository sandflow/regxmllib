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

#include "UUID.h"
#include <sstream>  
#include <iomanip>
#include <cstdlib>
#include <cstdio>
#include <stdexcept>

namespace rxml {

	static const std::string UUID_URN_PREFIX = "urn:uuid:";

	bool lowerCmp(char i, char j) {
		return (::towlower(i) == ::towlower(j));
	}

	bool UUID::urnToBytes(const std::string &urn, unsigned char(&uuid)[16]) {

		if (urn.size() != 45) return false;

		if (std::equal(urn.begin(),
			urn.begin() + UUID_URN_PREFIX.size(),
			UUID_URN_PREFIX.begin(), lowerCmp) != true) return false;

		/* process as UUID (urn:uuid:f81d4fae-7dec-11d0-a765-00a0c91e6bf6) */

		int pos = 9;

		for (int i = 0; i < 16; i++) {

			uuid[i] = std::strtol(urn.substr(pos, 2).c_str(), NULL, 16) & 0xff;

			if (i == 3 || i == 5 || i == 7 || i == 9) {
				pos += 3;
			} else {
				pos += 2;
			}

		}

		return true;

	}


	std::string UUID::bytesToString(const unsigned char uuid[16]) {

		char str[128];

		int r = snprintf(str, sizeof(str), "urn:uuid:%02x%02x%02x%02x-%02x%02x-%02x%02x-%02x%02x-%02x%02x%02x%02x%02x%02x",
			uuid[0], uuid[1], uuid[2], uuid[3],
			uuid[4], uuid[5], uuid[6], uuid[7],
			uuid[8], uuid[9], uuid[10], uuid[11],
			uuid[12], uuid[13], uuid[14], uuid[15]
		);

		if (r < 0) throw std::runtime_error("UUID::bytesToString failed.");

		return str;

	}
}