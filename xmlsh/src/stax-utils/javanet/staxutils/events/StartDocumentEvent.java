/*
 * $Id: StartDocumentEvent.java,v 1.2 2004/07/15 02:11:01 cniles Exp $
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
package javanet.staxutils.events;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.events.StartDocument;

/**
 * {@link StartDocument} implementation.
 * 
 * @author Christian Niles
 * @version $Revision: 1.2 $
 */
public class StartDocumentEvent extends AbstractXMLEvent
        implements
            StartDocument {

    /** Default XML version returned by {@link #getVersion()}. */
    public static final String DEFAULT_VERSION = "1.0";

    /** Default system id returned by {@link #getSystemId()}. */
    public static final String DEFAULT_SYSTEM_ID = "";

    /** Default encoding returned by {@link #getCharacterEncodingScheme()}. */
    public static final String DEFAULT_ENCODING = "UTF-8";

    /** The document encoding, or <code>null</code> if none was specified. */
    protected String encoding;

    /** The document standalone value, or <code>null</code> if none was specified. */
    protected Boolean standalone;

    /** The XML version, or <code>null</code> if none was specified. */
    protected String version;

    public StartDocumentEvent() {

    }

    public StartDocumentEvent(Location location) {

        super(location);

    }

    public StartDocumentEvent(String encoding, Location location) {

        super(location);
        this.encoding = encoding;

    }

    public StartDocumentEvent(String encoding, Boolean standalone,
            String version, Location location) {

        super(location);
        this.encoding = encoding;
        this.standalone = standalone;
        this.version = version;

    }

    public StartDocumentEvent(String encoding, Boolean standalone,
            String version, Location location, QName schemaType) {

        super(location, schemaType);
        this.encoding = encoding;
        this.standalone = standalone;
        this.version = version;

    }

    /**
     * Copy constructor.
     * 
     * @param that The {@link StartDocument} event to copy.
     */
    public StartDocumentEvent(StartDocument that) {

        super(that);

        // copy encoding
        if (that.encodingSet()) {

            this.encoding = that.getCharacterEncodingScheme();

        }

        // copy standalone
        if (standaloneSet()) {

            this.standalone = that.isStandalone()
                    ? Boolean.TRUE
                    : Boolean.FALSE;

        }

        this.version = DEFAULT_VERSION.equals(that.getVersion())
                ? null
                : that.getVersion();

    }

    /** Returns {@link #START_DOCUMENT}. */
    public int getEventType() {

        return START_DOCUMENT;

    }

    public boolean encodingSet() {

        return encoding != null;

    }

    public String getCharacterEncodingScheme() {

        return (encoding == null ? DEFAULT_ENCODING : encoding);

    }

    public String getSystemId() {

        Location location = getLocation();
        if (location != null) {

            String systemId = location.getSystemId();
            if (systemId != null) {

                return systemId;

            }

        }

        return DEFAULT_SYSTEM_ID;

    }

    public String getVersion() {

        return (version == null ? DEFAULT_VERSION : version);

    }

    public boolean isStandalone() {

        return (standalone == null ? false : standalone.booleanValue());

    }

    public boolean standaloneSet() {

        return standalone != null;

    }

}