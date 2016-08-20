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
package com.sandflow.smpte.register.model2016;

import com.sandflow.smpte.register.GroupsRegister;
import com.sandflow.smpte.util.UL;
import java.io.InputStreamReader;
import java.io.Reader;
import static junit.framework.Assert.assertNotNull;
import junit.framework.TestCase;

/**
 *
 * @author pal
 */
public class GroupsRegisterTest extends TestCase {
    
    private GroupsRegister groups;
    
    public GroupsRegisterTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        /* load the registers */
        Reader fg = new InputStreamReader(ClassLoader.getSystemResourceAsStream("resources/registers/2016/groups.xml"));
        assertNotNull(fg);
        
        groups = GroupsRegister.fromXML(fg);
        assertNotNull(groups);
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Tests that KLVSyntax is read correctly
     */
    public void testKLVSyntax04() {
        GroupsRegister.Entry e = groups.getEntryByUL(UL.fromURN("urn:smpte:ul:060e2b34.027f0101.02070102.11000000"));
        
        assertNotNull(e);
        
        assertEquals(e.getKlvSyntax().size(), 1);
        
        assertTrue(e.getKlvSyntax().contains((byte) 0x04));
    }
    
    /**
     * Tests that KLVSyntax is read correctly
     */
    public void testKLVSyntax0653() {
        GroupsRegister.Entry e = groups.getEntryByUL(UL.fromURN("urn:smpte:ul:060e2b34.027f0101.0d010101.01011000"));
        
        assertNotNull(e);
        
        assertEquals(2, e.getKlvSyntax().size());
        
        assertTrue(e.getKlvSyntax().contains((byte) 0x06));
        
        assertTrue(e.getKlvSyntax().contains((byte) 0x53));
    }

        /**
     * Tests that KLVSyntax is read correctly
     */
    public void testLocalTag() {
        GroupsRegister.Entry e = groups.getEntryByUL(UL.fromURN("urn:smpte:ul:060e2b34.027f0101.0d010101.01011000"));
        
        assertNotNull(e);
        
        boolean found = false;
        
        for(GroupsRegister.Entry.Record r : e.getContents()) {
            if (r.getItem().equals(UL.fromURN("urn:smpte:ul:060e2b34.01010107.06010103.07000000"))) {
                
                assertEquals((long) 0x1103, r.getLocalTag().longValue());
                
                found = true;
                break;
            }    
        }
        
        assertTrue(found);
    }
    
}
