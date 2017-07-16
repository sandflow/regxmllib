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
 
#ifndef COM_SANDFLOW_UTIL_STRFORMAT_H
#define COM_SANDFLOW_UTIL_STRFORMAT_H

#include <iostream>
#include <string>
#include <sstream>
#include <stdarg.h>  
#include <iomanip>

namespace rxml {

	/**********************

		std::string

	***********************/

	std::string to_string(int val);

	std::string to_string(unsigned int val);

	std::string to_string(unsigned long val);


	std::string to_string(long val);

	std::string to_string(char val);


	std::string to_string(unsigned char val);

	std::string to_string(short val);


	std::string to_string(unsigned short val);

	std::string to_string(unsigned long long val);

	std::string to_string(long long val);

	std::string to_string(double val);

	std::string to_string(const std::string &s);

	template<class T> std::string to_string(const T &obj) {
		return obj.to_string();
	}

	static const std::string EMPTY_STRING;

	std::string fmt(const std::string &fmt,
		const std::string &s1,
		const std::string &s2 = EMPTY_STRING,
		const std::string &s3 = EMPTY_STRING,
		const std::string &s4 = EMPTY_STRING);

	std::string bytesToString(const unsigned char *data, size_t len);

}

std::string operator+(const std::string& s1, const char *s2);

std::string operator+(const char *s1, const std::string& s2);


#endif