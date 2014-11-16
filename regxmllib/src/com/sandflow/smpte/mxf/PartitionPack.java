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
package com.sandflow.smpte.mxf;

import com.sandflow.smpte.klv.KLVInputStream;
import com.sandflow.smpte.klv.Triplet;
import com.sandflow.smpte.klv.exception.KLVException;
import com.sandflow.smpte.util.UL;
import com.sandflow.smpte.klv.adapter.ULValueAdapter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Pierre-Anthony Lemieux (pal@sandflow.com)
 */
public class PartitionPack {

    static final UL LABEL = new UL(new byte[]{0x06, 0x0e, 0x2b, 0x34, 0x02, 0x05, 0x01, 0x01, 0x0d, 0x01, 0x02, 0x01, 0x01, 0x00, 0x00, 0x00});

    private int majorVersion;
    private int minorVersion;
    private long kagSize;
    private long thisPartition;
    private long previousPartition;
    private long footerPartition;
    private long headerByteCount;
    private long indexByteCount;
    private long indexSID;
    private long bodyOffset;
    private long bodySID;
    private UL operationalPattern;
    private ArrayList<UL> essenceContainers = new ArrayList<>();
    private Kind kind;
    private Status status;

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public enum Kind {

        HEADER,
        BODY,
        FOOTER
    }

    public enum Status {

        OPEN_INCOMPLETE,
        CLOSED_INCOMPLETE,
        OPEN_COMPLETE,
        CLOSED_COMPLETE
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public void setMajorVersion(int majorVersion) {
        this.majorVersion = majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public void setMinorVersion(int minorVersion) {
        this.minorVersion = minorVersion;
    }

    public long getKagSize() {
        return kagSize;
    }

    public void setKagSize(long kagSize) {
        this.kagSize = kagSize;
    }

    public long getThisPartition() {
        return thisPartition;
    }

    public void setThisPartition(long thisPartition) {
        this.thisPartition = thisPartition;
    }

    public long getPreviousPartition() {
        return previousPartition;
    }

    public void setPreviousPartition(long previousPartition) {
        this.previousPartition = previousPartition;
    }

    public long getFooterPartition() {
        return footerPartition;
    }

    public void setFooterPartition(long footerPartition) {
        this.footerPartition = footerPartition;
    }

    public long getHeaderByteCount() {
        return headerByteCount;
    }

    public void setHeaderByteCount(long headerByteCount) {
        this.headerByteCount = headerByteCount;
    }

    public long getIndexByteCount() {
        return indexByteCount;
    }

    public void setIndexByteCount(long indexByteCount) {
        this.indexByteCount = indexByteCount;
    }

    public long getIndexSID() {
        return indexSID;
    }

    public void setIndexSID(long indexSID) {
        this.indexSID = indexSID;
    }

    public long getBodyOffset() {
        return bodyOffset;
    }

    public void setBodyOffset(long bodyOffset) {
        this.bodyOffset = bodyOffset;
    }

    public long getBodySID() {
        return bodySID;
    }

    public void setBodySID(long bodySID) {
        this.bodySID = bodySID;
    }

    public UL getOperationalPattern() {
        return operationalPattern;
    }

    public void setOperationalPattern(UL operationalPattern) {
        this.operationalPattern = operationalPattern;
    }

    public Collection<UL> getEssenceContainers() {
        return essenceContainers;
    }

    public void setEssenceContainers(Collection<UL> essenceContainers) {
        this.essenceContainers = new ArrayList<>(essenceContainers);
    }

    public static PartitionPack fromTriplet(Triplet triplet) throws KLVException {

        PartitionPack pp = new PartitionPack();

        if (!LABEL.equals(triplet.getKey(), 0xfef9 /*11111110 11111001*/)) {
            return null;
        }

        switch (triplet.getKey().getValueOctet(14)) {
            case 0x01:
                pp.setStatus(Status.OPEN_INCOMPLETE);
                break;
            case 0x02:
                pp.setStatus(Status.CLOSED_INCOMPLETE);
                break;
            case 0x03:
                pp.setStatus(Status.OPEN_COMPLETE);
                break;
            case 0x04:
                pp.setStatus(Status.CLOSED_COMPLETE);
                break;
            default:
                return null;
        }

        switch (triplet.getKey().getValueOctet(13)) {
            case 0x02:
                pp.setKind(Kind.HEADER);

                break;
            case 0x03:
                pp.setKind(Kind.BODY);

                break;
            case 0x04:
                pp.setKind(Kind.FOOTER);
                if (pp.getStatus() == Status.OPEN_COMPLETE
                        || pp.getStatus() == Status.OPEN_INCOMPLETE) {
                    return null;
                }
                break;
            default:
                return null;
        }

        KLVInputStream kis = new KLVInputStream(triplet.getValueAsStream());

        try {

            pp.setMajorVersion(kis.readUnsignedShort());

            pp.setMinorVersion(kis.readUnsignedShort());

            pp.setKagSize(kis.readUnsignedInt());

            pp.setThisPartition(kis.readLong());

            pp.setPreviousPartition(kis.readLong());

            pp.setFooterPartition(kis.readLong());

            pp.setHeaderByteCount(kis.readLong());

            pp.setIndexByteCount(kis.readLong());

            pp.setIndexSID(kis.readUnsignedInt());

            pp.setBodyOffset(kis.readLong());

            pp.setBodySID(kis.readLong());

            pp.setOperationalPattern(kis.readUL());

            pp.setEssenceContainers(kis.<UL, ULValueAdapter>readBatch());

        } catch (IOException e) {
            throw new KLVException(e);
        }

        return pp;
    }
}
