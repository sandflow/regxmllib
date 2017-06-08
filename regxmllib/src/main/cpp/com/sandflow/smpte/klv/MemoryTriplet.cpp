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

#include "MemoryTriplet.h"
#include "KLVStream.h"

namespace rxml {

	MemoryTriplet::MemoryTriplet() {}

	MemoryTriplet::MemoryTriplet(AUID key, long int length, unsigned char* value) : key(key), value(value, value + length) {

	}

	MemoryTriplet::MemoryTriplet(AUID key, long int length, std::istream & is) {
		this->key = key;
		this->value.resize(length);
		is.read((char*) this->value.data(), this->value.size());
	}

	MemoryTriplet::MemoryTriplet(std::istream & is) {
		fromStream(is);
	}

	MemoryTriplet::MemoryTriplet(const Triplet & t) {
		this->key = t.getKey();
		this->value.resize(t.getLength());

		const unsigned char *v = t.getValue();

		this->value.assign(v, v + this->value.size());
	}

	const AUID& MemoryTriplet::getKey() const {
		return key;
	}

	size_t MemoryTriplet::getLength() const {
		return value.size();
	}

	const unsigned char* MemoryTriplet::getValue() const {
		return value.data();
	}

	void MemoryTriplet::fromStream(std::istream &is) {

		KLVStream kis(is.rdbuf());

		this->key = kis.readAUID();

		size_t len = kis.readBERLength();

		value.resize(len);

		kis.readBytes(this->value.data(), value.size());

	}

}