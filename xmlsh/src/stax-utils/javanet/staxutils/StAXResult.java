/* $Id: StAXResult.java,v 1.2 2004/06/24 18:04:57 ryan_shoemaker Exp $
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

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.sax.SAXResult;

/**
 * A JAXP {@link javax.xml.transform.Result} implementation that produces
 * a result on the specified {@link javax.xml.stream.XMLStreamWriter} or
 * {@link javax.xml.stream.XMLEventWriter}.
 *
 * <p>
 * Please note that you may need to call flush() on the underlying
 * XMLStreamWriter or XMLEventWriter after the transform is complete.
 * <p>
 * 
 * The fact that JAXBResult derives from SAXResult is an implementation
 * detail. Thus in general applications are strongly discouraged from
 * accessing methods defined on SAXResult.
 *
 * <p>
 * In particular it shall never attempt to call the following methods:
 *
 * <ul>
 *    <li>setHandler</li>
 *    <li>setLexicalHandler</li>
 *    <li>setSystemId</li>
 * </ul>
 *
 * <p>
 * Example:
 *
 * <pre>
    // create a DOMSource
    Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(...);
    Source domSource = new DOMSource(doc);

    // create a StAXResult
    XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(System.out);
    Result staxResult = new StAXResult(writer);

    // run the transform
    TransformerFactory.newInstance().newTransformer().transform(domSource, staxResult);
 * </pre>
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version 1.0
 */
public class StAXResult extends SAXResult {

    /**
     * Create a new {@link javax.xml.transform.Result} that produces
     * a result on the specified {@link javax.xml.stream.XMLStreamWriter}
     *
     * @param writer the XMLStreamWriter
     * @throws IllegalArgumentException iff the writer is null
     */
    public StAXResult(XMLStreamWriter writer) {
        if( writer == null ) {
            throw new IllegalArgumentException();
        }

        super.setHandler(new ContentHandlerToXMLStreamWriter( writer ));
    }

    /**
     * Create a new {@link javax.xml.transform.Result} that produces
     * a result on the specified {@link javax.xml.stream.XMLEventWriter}
     *
     * @param writer the XMLEventWriter
     * @throws IllegalArgumentException iff the writer is null
     */
    public StAXResult(XMLEventWriter writer) {
        if( writer == null ) {
            throw new IllegalArgumentException();
        }

        super.setHandler(new ContentHandlerToXMLEventWriter( writer ));
    }
}
