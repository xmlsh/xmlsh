/*
 * $Id: BaseXMLStreamReader.java,v 1.2 2004/07/15 02:51:32 cniles Exp $
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

import javanet.staxutils.SimpleLocation;
import javanet.staxutils.StaticLocation;
import javanet.staxutils.XMLStreamUtils;
import javanet.staxutils.error.IllegalStreamStateException;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Abstract base class for {@link XMLStreamReader} implementations.
 * 
 * @author Christian Niles
 * @version $Revision: 1.2 $
 */
public abstract class BaseXMLStreamReader implements XMLStreamReader {

    /** The stream system ID. */
    protected String systemId;

    /** The stream encoding. */
    protected String encoding;

    public BaseXMLStreamReader() {

    }

    public BaseXMLStreamReader(String systemId, String encoding) {

        this.systemId = systemId;
        this.encoding = encoding;

    }

    public String getSystemId() {

        return systemId;

    }

    public String getEncoding() {

        return encoding;

    }

    /**
     * Returns the name of the current event type.
     * 
     * @return The name of the current event type.
     */
    public String getEventTypeName() {

        return XMLStreamUtils.getEventTypeName(getEventType());

    }

    public int nextTag() throws XMLStreamException {

        for (int eventType = next(); hasNext(); eventType = next()) {

            switch (eventType) {

                case START_ELEMENT :
                case END_ELEMENT :
                    return eventType;

                case CHARACTERS :
                case CDATA :
                    if (!isWhiteSpace()) {
                        // throw an error
                        break;
                    } else {
                        // fall through
                    }

                case SPACE :
                case PROCESSING_INSTRUCTION :
                case COMMENT :
                    // skip it
                    continue;

                default :
                    // stop and throw an error
                    break;

            }

        }

        throw new XMLStreamException("Encountered " + getEventTypeName()
                + " when expecting START_ELEMENT or END_ELEMENT",
                getStableLocation());

    }

    public boolean isCharacters() {

        return getEventType() == XMLStreamConstants.CHARACTERS;

    }

    public boolean isEndElement() {

        return getEventType() == XMLStreamConstants.END_ELEMENT;

    }

    public boolean isStartElement() {

        return getEventType() == XMLStreamConstants.START_ELEMENT;

    }

    public boolean isWhiteSpace() {

        return getEventType() == XMLStreamConstants.SPACE;

    }

    public boolean hasName() {

        switch (getEventType()) {

            case XMLStreamConstants.START_ELEMENT :
            case XMLStreamConstants.END_ELEMENT :
                return true;

            default :
                return false;

        }

    }

    public String getPrefix() {

        switch (getEventType()) {

            case XMLStreamConstants.START_ELEMENT :
            case XMLStreamConstants.END_ELEMENT :
                return getName().getPrefix();

            default :
                throw new IllegalStreamStateException(
                        "Expected START_ELEMENT or END_ELEMENT but was "
                                + getEventTypeName(), getStableLocation());

        }

    }

    public boolean hasText() {

        switch (getEventType()) {

            case XMLStreamConstants.SPACE :
            case XMLStreamConstants.CHARACTERS :
            case XMLStreamConstants.COMMENT :
            case XMLStreamConstants.CDATA :
            case XMLStreamConstants.ENTITY_REFERENCE :
                return true;

            default :
                return false;

        }

    }

    public String getNamespaceURI(String prefix) {

        if (prefix == null) {

            throw new IllegalArgumentException("Namespace prefix was null");

        }

        return getNamespaceContext().getNamespaceURI(prefix);

    }

    public String getNamespaceURI() {

        switch (getEventType()) {

            case XMLStreamConstants.START_ELEMENT :
            case XMLStreamConstants.END_ELEMENT :
                return getName().getNamespaceURI();

            default :
                throw new IllegalStreamStateException(
                        "Expected START_ELEMENT or END_ELEMENT state, but found "
                                + getEventTypeName(), getStableLocation());

        }

    }

    public String getAttributeLocalName(int index) {

        return getAttributeName(index).getLocalPart();

    }

    public String getAttributeNamespace(int index) {

        return getAttributeName(index).getNamespaceURI();

    }

    public String getAttributePrefix(int index) {

        return getAttributeName(index).getPrefix();

    }

    public void require(int type, String namespaceURI, String localName)
            throws XMLStreamException {

        int currType = getEventType();
        if (currType != type) {

            throw new XMLStreamException("Expected "
                    + XMLStreamUtils.getEventTypeName(type) + " but found "
                    + XMLStreamUtils.getEventTypeName(currType),
                    getStableLocation());

        }

    }

    public String getElementText() throws XMLStreamException {

        if (getEventType() != XMLStreamConstants.START_ELEMENT) {

            throw new XMLStreamException("Expected START_ELEMENT but found "
                    + getEventTypeName(), getStableLocation());

        }

        // save the element name and location so we can use it in the error message
        // as needed.
        QName elemName = getName();
        Location elemLocation = getStableLocation();

        // read text events until the end tag is reached
        StringBuffer content = null;
        for (int eventType = next(); eventType != END_ELEMENT; eventType = next()) {

            if (hasText()) {

                if (content == null) {

                    content = new StringBuffer();

                }
                content.append(getText());

            } else {

                throw new XMLStreamException("Encountered "
                        + getEventTypeName()
                        + " event within text-only element " + elemName,
                        elemLocation);

            }

        }

        // return content
        return (content == null ? "" : content.toString());

    }

    /**
     * Constructs a new, stable {@link Location} from the current stream location.
     * If the stream location implements {@link StaticLocation}, then the stream
     * location will be returned directly.
     * 
     * @return Constructs a new, stable {@link Location} from the current stream
     * 		location, or the current {@link Location} itself if it is already
     * 		stable.
     */
    public Location getStableLocation() {

        Location location = getLocation();
        if (!(location instanceof StaticLocation)) {

            // create copy
            location = new SimpleLocation(location);

        }

        return location;

    }

}