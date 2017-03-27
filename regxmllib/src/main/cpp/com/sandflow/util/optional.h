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
 
#ifndef COM_SANDFLOW_UTIL_OPTIONAL_H
#define COM_SANDFLOW_UTIL_OPTIONAL_H



	template<typename T> class Optional {

	public:
		Optional() : valid(false) {}

		Optional(T v) : valid(true), value(v) {}

		Optional& operator=(const T& other)
		{
			set(other);

			return *this;
		}

		Optional& operator=(const T* other)
		{
			
			set(other);

			return *this;
		}

		Optional& operator=(const Optional& other)
		{

			value = other.value;
			valid = other.valid;

			return *this;
		}

		const T* operator&() const {
			return is_valid() ? &get() : NULL;
		}

		void clear() {
			valid = false;
		}

		bool is_valid() const {
			return valid;
		}

		const T& get() const {
			return value;
		}

		void set(const T& other) {
			value = other;
			valid = true;
		}

		void set(const T* other) {
			if (other) {
				set(*other);
			} else {
				clear();
			}
		}

	private:
		T		value;
		bool	valid;
	};



#endif
