/*
 * Copyright (c) 2016, pal
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
package com.sandflow.smpte.mxf;

import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import junit.framework.TestCase;

/**
 *
 * @author pal
 */
public class MXFFilesTest extends TestCase {

    public MXFFilesTest(String testName) {
        super(testName);
    }

    public void testSeekFooterPartition() throws Exception {
        /* get the sample files */
        URI uri = ClassLoader.getSystemResource("resources/sample-files/audio1.mxf").toURI();

        assertNotNull(uri);

        SeekableByteChannel faf = Files.newByteChannel(Paths.get(uri));

        assertEquals(0x6258, MXFFiles.seekFooterPartition(faf));
    }
    
    public void testSeekFooterPartitionOpenIncompleteHeader() throws Exception {
        /* get the sample files */
        URI uri = ClassLoader.getSystemResource("resources/sample-files/open-incomplete-header.mxf").toURI();

        assertNotNull(uri);

        SeekableByteChannel faf = Files.newByteChannel(Paths.get(uri));

        assertEquals(0x8df1f, MXFFiles.seekFooterPartition(faf));
    }

    public void testSeekHeaderPartition() throws Exception {
        /* get the sample files */
        URI uri = ClassLoader.getSystemResource("resources/sample-files/audio1.mxf").toURI();

        assertNotNull(uri);

        SeekableByteChannel faf = Files.newByteChannel(Paths.get(uri));

        assertEquals(0, MXFFiles.seekHeaderPartition(faf));
    }

}
