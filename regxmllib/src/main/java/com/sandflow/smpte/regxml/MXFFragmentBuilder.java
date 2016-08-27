/*
 * Copyright (c) 2015, Pierre-Anthony Lemieux (pal@sandflow.com)
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
package com.sandflow.smpte.regxml;

import com.sandflow.smpte.klv.Group;
import com.sandflow.smpte.klv.KLVInputStream;
import com.sandflow.smpte.klv.LocalSet;
import com.sandflow.smpte.klv.LocalTagRegister;
import com.sandflow.smpte.klv.Triplet;
import com.sandflow.smpte.klv.exceptions.KLVException;
import com.sandflow.smpte.mxf.FillItem;
import com.sandflow.smpte.mxf.PartitionPack;
import com.sandflow.smpte.mxf.PrimerPack;
import com.sandflow.smpte.mxf.RandomIndexPack;
import com.sandflow.smpte.mxf.Set;
import com.sandflow.smpte.regxml.dict.DefinitionResolver;
import com.sandflow.smpte.regxml.dict.definitions.ClassDefinition;
import com.sandflow.smpte.regxml.dict.definitions.Definition;
import com.sandflow.smpte.util.AUID;
import com.sandflow.smpte.util.CountingInputStream;
import com.sandflow.smpte.util.UL;
import com.sandflow.smpte.util.UUID;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

/**
 * Builds a RegXML Fragment (SMPTE ST 2001-1) from an MXF file (SMPTE ST 377-1).
 */
public class MXFFragmentBuilder {

    private final static Logger LOG = Logger.getLogger(MXFFragmentBuilder.class.getName());

    private static final UL INDEX_TABLE_SEGMENT_UL
            = UL.fromURN("urn:smpte:ul:060e2b34.02530101.0d010201.01100100");

    private static final UL PREFACE_KEY
            = UL.fromURN("urn:smpte:ul:060e2b34.027f0101.0d010101.01012f00");

    /**
     * Seeks to the first byte of the Header partition, assuming the current position of the
     * channel is within the run-in (SMPTE ST 377-1 Section 6.5)
     *
     * @param mxffile Channel containing an MXF file
     * @return Offset of the first byte of the Header Partition, or -1 if 
     * the Header Partition was not found
     * @throws IOException
     */
    public static long seekHeaderPartition(SeekableByteChannel mxffile) throws IOException {

        ByteBuffer ulbytes = ByteBuffer.allocate(16);

        long offset = mxffile.position();

        while (mxffile.read(ulbytes) == ulbytes.limit() && offset <= 65536) {

            UL ul = new UL(ulbytes.array());

            if (ul.equalsWithMask(PartitionPack.getKey(), 0b1111111011100000 /* first eleven bytes minus the version byte */)) {
                mxffile.position(offset);

                return offset;
            }

            mxffile.position(++offset);

        }

        return -1;
    }

    /**
     * Seeks to the footer partition, assuming the current position of the
     * channel is within the run-in (SMPTE ST 377-1 Section 6.5), the footer partition
     * offset is listed in the Header Partition Pack or a Random Index Pack is 
     * present.
     *
     * @param mxffile Channel containing an MXF file
     * @return Offset of the Footer Partition, or -1 if a Footer Partition was not found
     * @throws IOException
     * @throws com.sandflow.smpte.klv.exceptions.KLVException
     */
    public static long seekFooterPartition(SeekableByteChannel mxffile) throws IOException, KLVException {

        long headeroffset = seekHeaderPartition(mxffile);

        KLVInputStream kis = new KLVInputStream(Channels.newInputStream(mxffile));

        Triplet t = kis.readTriplet();

        if (t == null) {
            return -1;
        }

        PartitionPack pp = PartitionPack.fromTriplet(t);

        if (pp == null) {
            return -1;
        }

        /* read the footer partition straight from the header partition, if available */
        if (pp.getFooterPartition() != 0) {

            mxffile.position(headeroffset + pp.getFooterPartition());

            return mxffile.position();
        }

        /* look for a RIP */
        mxffile.position(mxffile.size() - 4);

        ByteBuffer bytes = ByteBuffer.allocate(4);

        if (mxffile.read(bytes) != bytes.limit()) {
            return -1;
        }        
        
        mxffile.position(bytes.getInt(0));
        
        kis = new KLVInputStream(Channels.newInputStream(mxffile));

        t = kis.readTriplet();

        if (t == null) {
            return -1;
        }

        RandomIndexPack rip = RandomIndexPack.fromTriplet(t);

        if (rip == null) {
            return -1;
        }

        mxffile.position(rip.getOffsets().get(rip.getOffsets().size() - 1).getOffset());

        return mxffile.position();
    }

    /**
     * Returns a DOM Document Fragment containing a RegXML Fragment rooted at
     * the first Header Metadata object with the specified class, with a class
     * that descends from the specified class.
     *
     * @param mxfpartition MXF file
     * @param defresolver MetaDictionary definitions
     * @param rootclasskey Root class of Fragment
     * @param document DOM for which the Document Fragment is created
     * @return Document Fragment containing a single RegXML Fragment
     * @throws IOException
     * @throws KLVException
     * @throws com.sandflow.smpte.regxml.MXFFragmentBuilder.MXFException
     * @throws ParserConfigurationException
     * @throws com.sandflow.smpte.regxml.FragmentBuilder.RuleException
     */
    public static DocumentFragment fromInputStream(InputStream mxfpartition, DefinitionResolver defresolver, UL rootclasskey, Document document) throws IOException, KLVException, MXFException, ParserConfigurationException, FragmentBuilder.RuleException {

        CountingInputStream cis = new CountingInputStream(mxfpartition);

        /* look for the partition pack */
        KLVInputStream kis = new KLVInputStream(cis);

        PartitionPack pp = null;

        for (Triplet t; (t = kis.readTriplet()) != null;) {

            if ((pp = PartitionPack.fromTriplet(t)) != null) {
                break;
            }
        }

        if (pp == null) {
            throw new MXFException("No Partition Pack found.");
        }

        /* start counting header metadata bytes */
        cis.resetCount();

        /* look for the primer pack */
        LocalTagRegister localreg = null;

        for (Triplet t; (t = kis.readTriplet()) != null; cis.resetCount()) {

            /* skip fill items, if any */
            if (!t.getKey().equalsIgnoreVersion(FillItem.getKey())) {
                localreg = PrimerPack.createLocalTagRegister(t);
                break;
            }

        }

        if (localreg == null) {
            System.err.println("No Primer Pack found");
        }

        /* capture all local sets within the header metadata */
        ArrayList<Group> gs = new ArrayList<>();
        HashMap<UUID, Set> setresolver = new HashMap<>();

        for (Triplet t;
                cis.getCount() < pp.getHeaderByteCount()
                && (t = kis.readTriplet()) != null;) {

            if (t.getKey().equalsIgnoreVersion(INDEX_TABLE_SEGMENT_UL)) {

                /* stop if Index Table reached */
                LOG.warning("Index Table Segment encountered before Header Byte Count bytes read.");
                break;
            } else if (t.getKey().equalsIgnoreVersion(FillItem.getKey())) {

                /* skip fill items */
                continue;
            }
            try {
                Group g = LocalSet.fromTriplet(t, localreg);

                if (g != null) {

                    gs.add(g);

                    Set set = Set.fromGroup(g);

                    if (set != null) {
                        setresolver.put(set.getInstanceID(), set);
                    }

                } else {
                    LOG.log(Level.WARNING, "Failed to read Group: {0}", t.getKey().toString());
                }
            } catch (KLVException ke) {
                LOG.warning(
                        String.format(
                                "Failed to read Group %s with error %s",
                                t.getKey().toString(),
                                ke.getMessage()
                        )
                );
            }
        }

        for (Group agroup : gs) {

            /* in MXF, the first header metadata set should be the 
             Preface set according to ST 377-1 Section 9.5.1, preceded
             by Class 14 groups
             */
            if (agroup.getKey().equalsWithMask(PREFACE_KEY, 0b1111101011111111 /* ignore version and Group coding */)) {

                break;

            } else if (!agroup.getKey().isClass14()) {

                LOG.warning(
                        String.format(
                                "Invalid MXF file: at least one non-class 14 Set %s was found between"
                                + " the Primer Pack and the Preface Set.",
                                agroup.getKey()
                        )
                );

                break;

            }

        }

        /* create the fragment */
        FragmentBuilder fb = new FragmentBuilder(defresolver, setresolver);

        Group rootgroup = null;

        if (rootclasskey != null) {

            Iterator<Group> iter = gs.iterator();

            /* find first essence descriptor */
            while (rootgroup == null && iter.hasNext()) {

                Group g = iter.next();

                AUID gid = new AUID(g.getKey());

                /* go up the class hierarchy */
                while (rootgroup == null && gid != null) {

                    Definition def = defresolver.getDefinition(gid);

                    /* skip if not a class instance */
                    if (!(def instanceof ClassDefinition)) {
                        break;
                    }

                    /* is it an instance of the requested root object */
                    UL gul = def.getIdentification().asUL();

                    if (gul.equalsWithMask(rootclasskey, 0b1111101011111111 /* ignore version and Group coding */)) {
                        rootgroup = g;

                    } else {
                        /* get parent class */
                        gid = ((ClassDefinition) def).getParentClass();
                    }
                }

            }

        } else {

            rootgroup = gs.get(0);

        }

        if (rootgroup == null) {
            throw new MXFException("Root object not found");
        }

        return fb.fromTriplet(rootgroup, document);

    }

    public static class MXFException extends Exception {

        public MXFException(String msg) {
            super(msg);
        }
    }
}
