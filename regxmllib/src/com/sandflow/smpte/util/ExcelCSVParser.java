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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.AbstractList;
import java.util.ArrayList;

/**
 *
 * @author Pierre-Anthony Lemieux (pal@sandflow.com)
 */
public class ExcelCSVParser {

    Reader reader;

    public static class SyntaxException extends Exception {

        public SyntaxException() {
        }
    }

    private enum State {

        IN_QFIELD,
        DQ_IN_QFIELD,
        IN_PFIELD,
        EOL
    }

    public ExcelCSVParser(Reader reader) {
        this.reader = reader;
    }

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

    public static void main(String args[]) throws FileNotFoundException, UnsupportedEncodingException, SyntaxException, IOException {

        FileInputStream f = new FileInputStream("\\\\SERVER\\Business\\sandflow-consulting\\projects\\imf\\regxml\\register-format\\input\\smpte-ra-frozen-20140304.2118.elements.csv");

        InputStreamReader isr = new InputStreamReader(f, "US-ASCII");

        BufferedReader br = new BufferedReader(isr);

        ExcelCSVParser p = new ExcelCSVParser(br);

        for (int i = 0; i < 3; i++) {
            AbstractList<String> fields = p.getLine();

            for (String field : fields) {
                System.out.print(field + " | ");
            }

            System.out.print("\r\n");
        }
    }

}
