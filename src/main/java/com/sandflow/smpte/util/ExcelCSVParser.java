/*
 * Copyright (c) 2014, Pierre-Anthony Lemieux (pal@sandflow.com)
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
package com.sandflow.smpte.util;

import java.io.IOException;
import java.io.Reader;
import java.util.AbstractList;
import java.util.ArrayList;

/**
 * Simple Excel CSV parser.
 */
public class ExcelCSVParser {

    Reader reader;

    /**
     * Indicates an error has been encountered when parsing a CSV file
     */
    public static class SyntaxException extends Exception { }

    private enum State {
        IN_QFIELD,
        DQ_IN_QFIELD,
        IN_PFIELD,
        EOL
    }

    /**
     * Instantiates an ExcelCSVParser
     * @param reader Reader from which CSV lines will be read
     */
    public ExcelCSVParser(Reader reader) {
        this.reader = reader;
    }

    /**
     * Reads one line from the CSV file.
     * @return List of the fields found on the line
     * @throws com.sandflow.smpte.util.ExcelCSVParser.SyntaxException
     * @throws IOException 
     */
    public AbstractList<String> getLine() throws SyntaxException, IOException {
        State s = State.IN_PFIELD;

        int c;
        StringBuilder field = new StringBuilder();

        ArrayList<String> strs = new ArrayList<>();

        boolean done = false;

        while ((!done) && (c = reader.read()) != -1) {
            switch (s) {

                case EOL:

                    if (c != '\n') {
                        throw new SyntaxException();
                    } else {
                        done = true;
                    }

                case DQ_IN_QFIELD:

                    if (c == '"') {
                        field.append('"');
                        s = State.IN_QFIELD;
                        break;
                        
                    } else {

                        s = State.IN_PFIELD;

                    }
                
                case IN_PFIELD:

                    switch (c) {
                        
                        case '\r':
                            s = State.EOL;

                        case ',':

                            if (field.length() == 0) {
                                strs.add(null);
                            } else {
                                strs.add(field.toString());
                                field.setLength(0);
                            }
                            break;

                        case '"':

                            s = State.IN_QFIELD;

                            break;



                        default:
                            field.append((char) c);
                            break;
                    }

                    break;

                case IN_QFIELD:

                    if (c == '"') {
                        s = State.DQ_IN_QFIELD;
                    } else {
                        field.append((char) c);
                    }

                    break;

                
            }

        }

        if (done) {
            return strs;
        } else {
            return null;
        }
    }

}
