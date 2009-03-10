/*
 * $Id: EventMatcher.java,v 1.2 2004/07/12 17:34:54 cniles Exp $
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
package javanet.staxutils.helpers;

import java.util.Iterator;

import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.DTD;
import javax.xml.stream.events.EndDocument;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.EntityDeclaration;
import javax.xml.stream.events.EntityReference;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.NotationDeclaration;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * Provides utility methods useful for comparing two {@link XMLEvent} instances.
 * These methods compare only location/type-independent information, and thus don't
 * perform strict equality testing, which would include the event's location,
 * schema-type and dtd-type.
 * 
 * @author Christian Niles
 * @version $Revision: 1.2 $
 */
public final class EventMatcher {

    /**
     * Prevent instantiation
     */
    private EventMatcher() {

    }

    /**
     * Compares two {@link XMLEvent} instances. This method delegates actual
     * matching to the appropriate overloaded method.
     * 
     * @param a The first event.
     * @param b The second event.
     * @return <code>true</code> if the events match, <code>false</code> otherwise.
     */
    public static boolean eventsMatch(XMLEvent a, XMLEvent b) {

        if (a == b) {

            return true;

        } else if (a == null || b == null) {

            return false;

        } else if (a.getEventType() == b.getEventType()) {

            switch (a.getEventType()) {

                case XMLEvent.START_ELEMENT :
                    return eventsMatch(a.asStartElement(), b.asStartElement());

                case XMLEvent.END_ELEMENT :
                    return eventsMatch(a.asEndElement(), b.asEndElement());

                case XMLEvent.CDATA :
                case XMLEvent.SPACE :
                case XMLEvent.CHARACTERS :
                    return eventsMatch(a.asCharacters(), b.asCharacters());

                case XMLEvent.COMMENT :
                    return eventsMatch((Comment) a, (Comment) b);

                case XMLEvent.ENTITY_REFERENCE :
                    return eventsMatch((EntityReference) a, (EntityReference) b);

                case XMLEvent.ATTRIBUTE :
                    return eventsMatch((Attribute) a, (Attribute) b);

                case XMLEvent.NAMESPACE :
                    return eventsMatch((Namespace) a, (Namespace) b);

                case XMLEvent.START_DOCUMENT :
                    return eventsMatch((StartDocument) a, (StartDocument) b);

                case XMLEvent.END_DOCUMENT :
                    return eventsMatch((EndDocument) a, (EndDocument) b);

                case XMLEvent.PROCESSING_INSTRUCTION :
                    return eventsMatch((ProcessingInstruction) a,
                            (ProcessingInstruction) b);

                case XMLEvent.DTD :
                    return eventsMatch((DTD) a, (DTD) b);

                case XMLEvent.ENTITY_DECLARATION :
                    return eventsMatch((EntityDeclaration) a,
                            (EntityDeclaration) b);

                case XMLEvent.NOTATION_DECLARATION :
                    return eventsMatch((NotationDeclaration) a,
                            (NotationDeclaration) b);

            }

        }

        return false;

    }

    /**
     * Compares two {@link Attribute}s, returning <code>true</code> if their names
     * and values are the same.
     * 
     * @param a The first event.
     * @param b The second event.
     * @return <code>true</code> if the events match, <code>false</code> otherwise.
     */
    public static boolean eventsMatch(Attribute a, Attribute b) {

        if (a == b) {

            return true;

        } else if (a == null || b == null) {

            return false;

        } else if (a.getName().equals(b.getName())) {

            return a.getValue().equals(b.getValue());

        } else {

            return false;

        }
    }

    /**
     * Compares two {@link Characters}s. This method will return <code>true</code>
     * only if they have the same event type ({@link XMLEvent#CHARACTERS}, 
     * {@link XMLEvent#CDATA}, or {@link XMLEvent#SPACE}), and their text content
     * matches.
     * 
     * @param a The first event.
     * @param b The second event.
     * @return <code>true</code> if the events match, <code>false</code> otherwise.
     */
    public static boolean eventsMatch(Characters a, Characters b) {

        if (a == b) {

            return true;

        } else if (a == null || b == null) {

            return false;

        } else if (a.getEventType() == b.getEventType()) {

            return a.getData().equals(b.getData());

        } else {

            return false;

        }

    }

    /**
     * Compares two {@link Comment}s. This method will return <code>true</code>
     * only if their text content matches.
     * 
     * @param a The first event.
     * @param b The second event.
     * @return <code>true</code> if the events match, <code>false</code> otherwise.
     */
    public static boolean eventsMatch(Comment a, Comment b) {

        if (a == b) {

            return true;

        } else if (a == null || b == null) {

            return false;

        } else {

            return a.getText().equals(b.getText());
        }

    }

    /**
     * Compares two {@link DTD}s. This method will return <code>true</code>
     * only if their declarations are identical.
     * 
     * @param a The first event.
     * @param b The second event.
     * @return <code>true</code> if the events match, <code>false</code> otherwise.
     */
    public static boolean eventsMatch(DTD a, DTD b) {

        if (a == b) {

            return true;

        } else if (a == null || a == null) {

            return false;

        } else {

            // TODO determine the best way to compare DTD events
            return a.getDocumentTypeDeclaration().equals(
                    b.getDocumentTypeDeclaration());

        }

    }

    /**
     * Compares two {@link EndDocument}s. Because {@link EndDocument} events have no
     * real state, two instances always match.
     * 
     * @param a The first event.
     * @param b The second event.
     * @return <code>true</code> if the events match, <code>false</code> otherwise.
     */
    public static boolean eventsMatch(EndDocument a, EndDocument b) {

        return (a != null && b != null);

    }

    /**
     * Compares two {@link EndElement}s. This method will return <code>true</code>
     * only if their names match.
     * 
     * @param a The first event.
     * @param b The second event.
     * @return <code>true</code> if the events match, <code>false</code> otherwise.
     */
    public static boolean eventsMatch(EndElement a, EndElement b) {

        if (a == b) {

            return true;

        } else if (a == null || b == null) {

            return false;

        } else {

            return a.getName().equals(b.getName());

        }

    }

    /**
     * Compares two {@link EntityDeclaration}s. This method will return 
     * <code>true</code> only if the two events' names, replacement text, public IDs,
     * system IDs, and notations are the same.
     * 
     * @param a The first event.
     * @param b The second event.
     * @return <code>true</code> if the events match, <code>false</code> otherwise.
     */
    public static boolean eventsMatch(EntityDeclaration a,
            EntityDeclaration b) {

        if (a == b) {

            return true;

        } else if (a == null || b == null) {

            return false;

        }

        // compare names
        if (!a.getName().equals(b.getName())) {

            return false;

        }

        // compare base uris
        String baseURI = a.getBaseURI();
        if (!(baseURI == null
                ? b.getBaseURI() == null
                : baseURI.equals(b.getBaseURI()))) {

            return false;

        }

        // compare replacement text
        String text = a.getReplacementText();
        if (!(text == null
                ? b.getReplacementText() == null
                : text.equals(b.getReplacementText()))) {

            return false;

        }

        // compare public Ids
        String publicId = a.getPublicId();
        if (!(publicId == null
                ? b.getPublicId() == null
                : publicId.equals(b.getPublicId()))) {

            return false;

        }

        // compare system ids
        String systemId = a.getSystemId();
        if (!(systemId == null
                ? b.getSystemId() == null
                : systemId.equals(b.getSystemId()))) {

            return false;

        }

        // compare notations
        String ndata = a.getNotationName();
        if (!(ndata == null
                ? b.getNotationName() == null
                : ndata.equals(b.getNotationName()))) {

            return false;

        }

        return true;

    }

    /**
     * Compares two {@link EntityReference}s. This method will return 
     * <code>true</code> only if the two references have the same name, and their
     * declarations also match.
     * 
     * @param a The first event.
     * @param b The second event.
     * @return <code>true</code> if the events match, <code>false</code> otherwise.
     */
    public static boolean eventsMatch(EntityReference a, EntityReference b) {

        if (a == b) {

            return true;

        } else if (a == null || b == null) {

            return false;

        }

        if (a.getName().equals(b.getName())) {

            return eventsMatch(a.getDeclaration(), b.getDeclaration());

        } else {

            return false;

        }

    }

    /**
     * Compares two {@link Namespace}s. This method will return <code>true</code>
     * only if the two namespaces have identical prefixes and namespace URIs.
     * 
     * @param a The first event.
     * @param b The second event.
     * @return <code>true</code> if the events match, <code>false</code> otherwise.
     */
    public static boolean eventsMatch(Namespace a, Namespace b) {

        if (a == b) {

            return true;

        } else if (a == null || b == null) {

            return false;

        }

        return (a.getPrefix().equals(b.getPrefix()) && a.getNamespaceURI()
                .equals(b.getNamespaceURI()));

    }

    /**
     * Compares two {@link NotationDeclaration}s. This method will return
     * <code>true</code> only if the two namespaces have identical names, public IDs,
     * and system IDs.
     * 
     * @param a The first event.
     * @param b The second event.
     * @return <code>true</code> if the events match, <code>false</code> otherwise.
     */
    public static boolean eventsMatch(NotationDeclaration a,
            NotationDeclaration b) {

        if (a == b) {

            return true;

        } else if (a == null || b == null) {

            return false;

        }

        // compare names
        if (!a.getName().equals(b.getName())) {

            return false;

        }

        // compare public Ids
        String publicId = a.getPublicId();
        if (!(publicId == null
                ? b.getPublicId() == null
                : publicId.equals(b.getPublicId()))) {

            return false;

        }

        // compare system ids
        String systemId = a.getSystemId();
        if (!(systemId == null
                ? b.getSystemId() == null
                : systemId.equals(b.getSystemId()))) {

            return false;

        }

        return true;

    }

    /**
     * Compares two {@link ProcessingInstruction}s. This method will return
     * <code>true</code> only if the two events have identical targets and data.
     * 
     * @param a The first event.
     * @param b The second event.
     * @return <code>true</code> if the events match, <code>false</code> otherwise.
     */
    public static boolean eventsMatch(ProcessingInstruction a,
            ProcessingInstruction b) {

        if (a == b) {

            return true;

        } else if (a == null || b == null) {

            return false;

        }

        return (a.getTarget().equals(b.getTarget()) && a.getData().equals(
                b.getData()));

    }

    /**
     * Compares two {@link StartDocument}s. This method will return
     * <code>true</code> only if the two events have identical encodings, versions,
     * and have the same standalone setting.
     * 
     * @param a The first event.
     * @param b The second event.
     * @return <code>true</code> if the events match, <code>false</code> otherwise.
     */
    public static boolean eventsMatch(StartDocument a, StartDocument b) {

        if (a == b) {

            return true;

        } else if (a == null || b == null) {

            return false;

        }

        if (!a.getCharacterEncodingScheme().equals(
                b.getCharacterEncodingScheme())) {

            return false;

        } else if (a.isStandalone() != b.isStandalone()) {

            return false;

        } else if (!a.getVersion().equals(b.getVersion())) {

            return false;

        } else {

            // TODO match the two system ID fields?
            return true;

        }

    }

    /**
     * Compares two {@link StartElement}s. This method will return
     * <code>true</code> only if the two events have identical names, attributes, and
     * namespaces.
     * 
     * @param a The first event.
     * @param b The second event.
     * @return <code>true</code> if the events match, <code>false</code> otherwise.
     */
    public static boolean eventsMatch(StartElement a, StartElement b) {

        if (a == b) {

            return true;

        } else if (a == null || b == null) {

            return false;

        }

        if (!a.getName().equals(b.getName())) {
            
            return false;
            
        } else if (!matchAttributes(a.getAttributes(), b.getAttributes())) {
            
            return false;
            
        } else if (!matchNamespaces(a.getNamespaces(), b.getNamespaces())) {
            
            return false;
            
        } else {
            
            return true;
            
        }
        
    }

    /**
     * Iterates over two sets of {@link Attribute}s and determines if both contain
     * matching attributes.
     * 
     * @param a The first set of {@link Attribute}s.
     * @param b The second set of {@link Attribute}s.
     * @return <code>true</code> if the two iterators iterate over matching
     * 		attributes, <code>false</code> otherwise.
     */
    public static boolean matchAttributes(Iterator a, Iterator b) {

        // TODO Update attribute matching to allow attributes to appear in different
        // order
        if (a == b) {

            return true;

        } else if (a == null || b == null) {

            return false;

        }

        while (a.hasNext() && b.hasNext()) {

            Attribute A = (Attribute) a.next();
            Attribute B = (Attribute) b.next();

            if (!eventsMatch(A, B)) {

                return false;

            }

        }

        return a.hasNext() == b.hasNext();

    }

    /**
     * Iterates over two sets of {@link Namespace}s and determines if both contain
     * matching namespaces.
     * 
     * @param a The first set of {@link Namespace}s.
     * @param b The second set of {@link Namespace}s.
     * @return <code>true</code> if the two iterators iterate over matching
     * 		namespaces, <code>false</code> otherwise.
     */
    public static boolean matchNamespaces(Iterator a, Iterator b) {

        // TODO Update namespace matching to allow attributes to appear in different
        // order
        if (a == b) {

            return true;

        } else if (a == null || b == null) {

            return false;

        }

        while (a.hasNext() && b.hasNext()) {

            Namespace A = (Namespace) a.next();
            Namespace B = (Namespace) b.next();

            if (!eventsMatch(A, B)) {

                return false;

            }

        }

        return a.hasNext() == b.hasNext();

    }

}