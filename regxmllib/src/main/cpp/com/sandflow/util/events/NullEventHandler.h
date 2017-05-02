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
 
#ifndef COM_SANDFLOW_UTIL_EVENTS_NULLEVENTHANDLER
#define COM_SANDFLOW_UTIL_EVENTS_NULLEVENTHANDLER

#include <com/sandflow/util/events/EventHandler.h>

class NullEventHandler: public EventHandler {

public:

	virtual bool info(const std::string &code, const std::string &reason, const std::string &where) {
		return true;
	}

	virtual bool warn(const std::string &code, const std::string &reason, const std::string &where) {
		return true;
	}

	virtual bool error(const std::string &code, const std::string &reason, const std::string &where) {
		return true;
	}

	virtual bool fatal(const std::string &code, const std::string &reason, const std::string &where) {
		return true;
	}

};

extern NullEventHandler NULL_EVENTHANDLER;

#endif