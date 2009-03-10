/* $Id: StAXSource.java,v 1.5 2007/01/22 23:36:09 ryan_shoemaker Exp $
 *
 * Copyright (c) 2004, Sun Microsystems, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 *     * Neither the name of Sun Microsystems, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package javanet.staxutils;

import javanet.staxutils.helpers.XMLFilterImplEx;
import org.xml.sax.*;
import org.xml.sax.ext.LexicalHandler;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.sax.SAXSource;

/**
 * A JAXP {@link javax.xml.transform.Source} implementation that wraps
 * the specified {@link javax.xml.stream.XMLStreamReader} or
 * {@link javax.xml.stream.XMLEventReader} for use by applications that
 * expext a {@link javax.xml.transform.Source}.
 *
 * <p>
 * The fact that StAXSource derives from SAXSource is an implementation
 * detail. Thus in general applications are strongly discouraged from
 * accessing methods defined on SAXSource. In particular:
 *
 * <ul>
 *   <li> The setXMLReader and setInputSource methods shall never be called.</li>
 *   <li> The XMLReader object obtained by the getXMLReader method shall
 *        be used only for parsing the InputSource object returned by
 *        the getInputSource method.</li>
 *   <li> The InputSource object obtained by the getInputSource method shall
 *        be used only for being parsed by the XMLReader object returned by
 *        the getXMLReader method.</li>
 * </ul>
 *
 * <p>
 * Example:
 *
 * <pre>
 * // create a StAXSource
 * XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new FileReader(args[0]));
 * Source staxSource = new StAXSource(reader);
 *
 * // createa StreamResult
 * Result streamResult = new StreamResult(System.out);
 *
 * // run the transform
 * TransformerFactory.newInstance().newTransformer().transform(staxSource, streamResult);
 * </pre>
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version 1.0
 */
public class StAXSource extends SAXSource {
    
    // StAX to SAX converter that will read from StAX and produce SAX
    // this object will be wrapped by the XMLReader exposed to the client
    private final StAXReaderToContentHandler reader;
    
    // SAX allows ContentHandler to be changed during the parsing,
    // but JAXB doesn't. So this repeater will sit between those
    // two components.
    private XMLFilterImplEx repeater = new XMLFilterImplEx();
    
    // this object will pretend as an XMLReader.
    // no matter what parameter is specified to the parse method,
    // it will just read from the StAX reader.
    private final XMLReader pseudoParser = new XMLReader() {
        public boolean getFeature(String name) throws SAXNotRecognizedException {
            if ("http://xml.org/sax/features/namespaces".equals(name)) {
                return true;
            } else if ("http://xml.org/sax/features/namespace-prefixes".equals(name)) {
                return repeater.getNamespacePrefixes();
            } else if ("http://xml.org/sax/features/external-general-entities".equals(name)) {
                return true;
            } else if ("http://xml.org/sax/features/external-parameter-entities".equals(name)) {
                return true;
            }
            
            throw new SAXNotRecognizedException(name);
        }
        
        public void setFeature(String name, boolean value)
                throws SAXNotRecognizedException, SAXNotSupportedException {
            if ("http://xml.org/sax/features/namespaces".equals(name)) {
                // Presently we only support namespaces==true.  [Issue 9] 
                if (!value) {
                    throw new SAXNotSupportedException(name);
                }
            } else if ("http://xml.org/sax/features/namespace-prefixes".equals(name)) {
                repeater.setNamespacePrefixes(value);
            } else if ("http://xml.org/sax/features/external-general-entities".equals(name)) {
                // Pass over, XOM likes to get this feature
            } else if ("http://xml.org/sax/features/external-parameter-entities".equals(name)) {
                // Pass over, XOM likes to get this feature
            } else {
                throw new SAXNotRecognizedException(name);
            }
        }
        
        public Object getProperty(String name) throws SAXNotRecognizedException {
            if( "http://xml.org/sax/properties/lexical-handler".equals(name) ) {
                return repeater.getLexicalHandler();
            }
            
            throw new SAXNotRecognizedException(name);
        }
        
        public void setProperty(String name, Object value) throws SAXNotRecognizedException {
            if( "http://xml.org/sax/properties/lexical-handler".equals(name) ) {
                repeater.setLexicalHandler((LexicalHandler)value);
            } else {
                throw new SAXNotRecognizedException(name);
            }
        }
        
        // we will store this value but never use it by ourselves.
        private EntityResolver entityResolver;
        public void setEntityResolver(EntityResolver resolver) {
            this.entityResolver = resolver;
        }
        public EntityResolver getEntityResolver() {
            return entityResolver;
        }
        
        private DTDHandler dtdHandler;
        public void setDTDHandler(DTDHandler handler) {
            this.dtdHandler = handler;
        }
        public DTDHandler getDTDHandler() {
            return dtdHandler;
        }
        
        public void setContentHandler(ContentHandler handler) {
            repeater.setContentHandler(handler);
        }
        public ContentHandler getContentHandler() {
            return repeater.getContentHandler();
        }
        
        private ErrorHandler errorHandler;
        public void setErrorHandler(ErrorHandler handler) {
            this.errorHandler = handler;
        }
        public ErrorHandler getErrorHandler() {
            return errorHandler;
        }
        
        public void parse(InputSource input) throws SAXException {
            parse();
        }
        
        public void parse(String systemId) throws SAXException {
            parse();
        }
        
        public void parse() throws SAXException {
            // parses from a StAX reader and generates SAX events which
            // go through the repeater and are forwarded to the appropriate
            // component
            try {
                reader.bridge();
            } catch( XMLStreamException e ) {
                // wrap it in a SAXException
                SAXParseException se =
                        new SAXParseException(
                        e.getMessage(),
                        null,
                        null,
                        e.getLocation().getLineNumber(),
                        e.getLocation().getColumnNumber(),
                        e);
                
                // if the consumer sets an error handler, it is our responsibility
                // to notify it.
                if(errorHandler!=null)
                    errorHandler.fatalError(se);
                
                // this is a fatal error. Even if the error handler
                // returns, we will abort anyway.
                throw se;
                
            }
        }
    };
    
    /**
     * Creates a new {@link javax.xml.transform.Source} for the given
     * {@link XMLStreamReader}.
     *
     * The XMLStreamReader must be pointing at either a
     * {@link javax.xml.stream.XMLStreamConstants#START_DOCUMENT} or
     * {@link javax.xml.stream.XMLStreamConstants#START_ELEMENT} event.
     *
     * @param reader XMLStreamReader that will be exposed as a Source
     * @throws IllegalArgumentException iff the reader is null
     * @throws IllegalStateException iff the reader is not pointing at either a
     * START_DOCUMENT or START_ELEMENT event
     */
    public StAXSource(XMLStreamReader reader) {
        if( reader == null )
            throw new IllegalArgumentException();
        
        int eventType = reader.getEventType();
        if (!(eventType == XMLStreamConstants.START_DOCUMENT)
        && !(eventType == XMLStreamConstants.START_ELEMENT)) {
            throw new IllegalStateException();
        }
        
        this.reader =
                new XMLStreamReaderToContentHandler(
                reader,
                repeater);
        
        super.setXMLReader(pseudoParser);
        // pass a dummy InputSource. We don't care
        super.setInputSource(new InputSource());
    }
    
    /**
     * Creates a new {@link javax.xml.transform.Source} for the given
     * {@link XMLEventReader}.
     *
     * The XMLEventReader must be pointing at either a
     * {@link javax.xml.stream.XMLStreamConstants#START_DOCUMENT} or
     * {@link javax.xml.stream.XMLStreamConstants#START_ELEMENT} event.
     *
     * @param reader XMLEventReader that will be exposed as a Source
     * @throws IllegalArgumentException iff the reader is null
     * @throws IllegalStateException iff the reader is not pointing at either a
     * START_DOCUEMENT or START_ELEMENT event
     */
    public StAXSource(XMLEventReader reader) {
        if( reader == null )
            throw new IllegalArgumentException();
        
        // TODO: detect IllegalStateException for START_ELEMENT|DOCUMENT
        // bugid 5046340 - peek not implemented
        // XMLEvent event = staxEventReader.peek();
        
        this.reader =
                new XMLEventReaderToContentHandler(
                reader,
                repeater);
        
        super.setXMLReader(pseudoParser);
        // pass a dummy InputSource. We don't care
        super.setInputSource(new InputSource());
    }
    
}
