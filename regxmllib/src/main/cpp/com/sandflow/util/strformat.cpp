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


#include "strformat.h"



std::string strf::to_string(int val) {
	std::stringstream ss;

	ss << val;

	return ss.str();
}

std::string strf::to_string(unsigned int val) {
	std::stringstream ss;

	ss << val;

	return ss.str();
}

std::string strf::to_string(unsigned long val) {
	std::stringstream ss;

	ss << val;

	return ss.str();
}

std::string strf::to_string(long val) {
	std::stringstream ss;

	ss << val;

	return ss.str();
}

std::string strf::to_string(char val) {
	std::stringstream ss;

	ss << (int) val;

	return ss.str();
}

std::string strf::to_string(unsigned char val) {
	std::stringstream ss;

	ss << (int) val;

	return ss.str();
}

std::string strf::to_string(short val) {
	std::stringstream ss;

	ss << val;

	return ss.str();
}

std::string strf::to_string(unsigned short val) {
	std::stringstream ss;

	ss << val;

	return ss.str();
}

std::string strf::to_string(unsigned long long val) {
	std::stringstream ss;

	ss << val;

	return ss.str();
}

std::string strf::to_string(long long val) {
	std::stringstream ss;

	ss << val;

	return ss.str();
}

std::string strf::to_string(double val) {
	std::stringstream ss;

	ss << val;

	return ss.str();
}

std::string strf::to_string(const std::string & s) {
	return s;
}

void _appendfmt(const std::string &fmt,
	const std::string &s,
	size_t &curoff,
	size_t &oldoff,
	std::stringstream& ss) {

	if (curoff != std::string::npos) {

		oldoff = curoff;
		curoff = fmt.find("{}", oldoff);

		ss << fmt.substr(oldoff, curoff - oldoff);

		if (curoff != std::string::npos) {
			ss << s;
			curoff += 2;
		}
	}

}

std::string strf::fmt(const std::string & fmt, const std::string & s1, const std::string & s2, const std::string & s3, const std::string & s4) {

	std::stringstream ss;

	size_t curoff = 0;
	size_t oldoff = 0;

	_appendfmt(fmt, s1, curoff, oldoff, ss);
	_appendfmt(fmt, s2, curoff, oldoff, ss);
	_appendfmt(fmt, s3, curoff, oldoff, ss);
	_appendfmt(fmt, s4, curoff, oldoff, ss);

	return ss.str();
}

std::string operator+(const std::string& s1, const char *s2) {
	return s1 + std::string(s2);
}

std::string operator+(const char *s1, const std::string& s2) {
	return  std::string(s1) + s2;
}