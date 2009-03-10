/*
 * $Id: SimpleLocation.java,v 1.2 2004/07/08 14:29:41 cniles Exp $
 * 
 * Copyright (c) 2004, Christian Niles, unit12.net
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *		*   Redistributions of source code must retain the above copyright
 *          notice, this list of conditions and the following disclaimer.
 * 
 *	    *	Redistributions in binary form must reproduce the above copyright
 *          notice, this list of conditions and the following disclaimer in the
 *          documentation and/or other materials provided with the distribution.
 * 
 *      *   Neither the name of Christian Niles, Unit12, nor the names of its
 *          contributors may be used to endorse or promote products derived from
 *          this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package javanet.staxutils;

import javax.xml.stream.Location;

/**
 * Basic implementation of {@link ExtendedLocation}.
 * 
 * @author Christian Niles
 * @version $Revision: 1.2 $
 */
public class SimpleLocation implements ExtendedLocation, StaticLocation {

    /** The nested location. */
    private Location nestedLocation;

    /** The line number; defaults to <code>-1</code>. */
    private int lineNumber = -1;

    /** The character offset; defaults to <code>-1</code>. */
    private int characterOffset = -1;

    /** The column number; defaults to <code>-1</code>. */
    private int columnNumber = -1;

    /** The system ID; defaults to <code>null</code>. */
    private String publicId;

    /** The public ID; defaults to <code>null</code>. */
    private String systemId;

    public SimpleLocation(String publicId, String systemId, int lineNumber,
            Location nestedLocation) {

        this.publicId = publicId;
        this.systemId = systemId;
        this.lineNumber = lineNumber;
        this.nestedLocation = nestedLocation;

    }

    public SimpleLocation(String publicId, String systemId, int lineNumber,
            int columnNumber, Location nestedLocation) {

        this.publicId = publicId;
        this.systemId = systemId;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
        this.nestedLocation = nestedLocation;

    }

    public SimpleLocation(String publicId, String systemId, int lineNumber,
            int columnNumber, int characterOffset, Location nestedLocation) {

        this.publicId = publicId;
        this.systemId = systemId;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
        this.characterOffset = characterOffset;
        this.nestedLocation = nestedLocation;

    }

    public SimpleLocation(Location loc) {

        this.publicId = loc.getPublicId();
        this.systemId = loc.getSystemId();
        this.lineNumber = loc.getLineNumber();
        this.columnNumber = loc.getColumnNumber();
        this.characterOffset = loc.getCharacterOffset();
        if (loc instanceof ExtendedLocation) {

            this.nestedLocation = ((ExtendedLocation) loc).getNestedLocation();

        }

    }

    public int getCharacterOffset() {

        return this.characterOffset;

    }

    public int getColumnNumber() {

        return this.columnNumber;

    }

    public int getLineNumber() {

        return this.lineNumber;

    }

    public String getPublicId() {

        return this.publicId;

    }

    public String getSystemId() {

        return this.systemId;

    }

    public Location getNestedLocation() {

        return nestedLocation;

    }

    public String toString() {

        StringBuffer buffer = new StringBuffer();

        String publicId = getPublicId();
        String systemId = getSystemId();
        if (publicId != null) {

            buffer.append(publicId);
            if (systemId != null) {

                buffer.append("#").append(systemId);

            }

        } else if (systemId != null) {

            buffer.append(publicId);

        }

        buffer.append('[');
        buffer.append("line=").append(getLineNumber());
        buffer.append("column=").append(getColumnNumber());
        buffer.append(']');

        Location nested = getNestedLocation();
        if (nested != null) {

            buffer.append("->");
            buffer.append(nested);

        }

        return buffer.toString();

    }

}