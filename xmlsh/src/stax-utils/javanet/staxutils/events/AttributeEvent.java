/*
 * $Id: AttributeEvent.java,v 1.3 2004/07/15 02:11:01 cniles Exp $
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
import javax.xml.stream.events.Attribute;

/**
 * {@link Attribute} event implementation.
 * 
 * @author Christian Niles
 * @version $Revision: 1.3 $
 */
public class AttributeEvent extends AbstractXMLEvent implements Attribute {

    /** 
     * Whether the attribute was specified in the document. Defaults to
     * <code>true</code>. 
     */
    private boolean specified = true;

    /** The qualified attribute name. */
    private QName name;

    /** The normalized attribute value. */
    private String value;

    /** Type of attribute as specified in the DTD. Defaults to <code>CDATA</code>. */
    private String dtdType = "CDATA";

    /**
     * Constructs an <code>AttributeEvent</code> with the specified name and value.
     * 
     * @param name The qualified attribute name.
     * @param value The attribute value.
     */
    public AttributeEvent(QName name, String value) {

        this.name = name;
        this.value = value;

    }

    /**
     * Constructs a new <code>AttributeEvent</code>.
     * 
     * @param name The qualified attribute name.
     * @param value The attribute value.
     * @param specified Whether the attribute was specified in the document
     *     (<code>true</code), or inherited from a DTD or schema
     *     (<code>false</code>).
     */
    public AttributeEvent(QName name, String value, boolean specified) {

        this.name = name;
        this.value = value;
        this.specified = specified;

    }

    /**
     * Constructs a new <code>AttributeEvent</code>.
     * 
     * @param name The qualified attribute name.
     * @param value The attribute value.
     * @param location The {@link Location} of the attribute.
     */
    public AttributeEvent(QName name, String value, Location location) {

        super(location);
        this.name = name;
        this.value = value;

    }

    /**
     * Constructs a new <code>AttributeEvent</code>.
     * 
     * @param name The qualified attribute name.
     * @param value The attribute value.
     * @param location The {@link Location} of the attribute.
     * @param schemaType The attribute type as specified in the schema.
     */
    public AttributeEvent(QName name, String value, Location location,
            QName schemaType) {

        super(location, schemaType);
        this.name = name;
        this.value = value;

    }

    /**
     * Constructs a new <code>AttributeEvent</code>.
     * 
     * @param name The qualified attribute name.
     * @param value The attribute value.
     * @param specified Whether the attribute was specified in the document
     *     (<code>true</code), or inherited from a DTD or schema
     *     (<code>false</code>).
     * @param location The {@link Location} of the attribute.
     * @param dtdType The attribute type as specified in the DTD.
     * @param schemaType The attribute type as specified in the schema.
     */
    public AttributeEvent(QName name, String value, boolean specified,
            String dtdType, Location location, QName schemaType) {

        super(location, schemaType);
        this.name = name;
        this.value = value;
        this.specified = specified;
        this.dtdType = dtdType;

    }

    /**
     * Copy constructor that optionally allows the name and/or value to be changed.
     * 
     * @param name The new attribute name, or <code>null</code> to use the name from
     * 		the provided attribute.
     * @param value The new attribute value, or <code>null</code> to use the value
     * 		from the provided attribute.
     * @param that The {@link Attribute} event to copy.
     */
    public AttributeEvent(QName name, String value, Attribute that) {

        super(that);
        this.specified = that.isSpecified();
        this.name = (name == null ? that.getName() : name);
        this.value = (value == null ? that.getValue() : value);
        this.dtdType = that.getDTDType();

    }

    /**
     * Copy constructor.
     * 
     * @param that The {@link Attribute} event to copy.
     */
    public AttributeEvent(Attribute that) {

        super(that);
        this.specified = that.isSpecified();
        this.name = that.getName();
        this.value = that.getValue();
        this.dtdType = that.getDTDType();

    }

    /** Returns {@link #ATTRIBUTE}. */
    public int getEventType() {

        return ATTRIBUTE;

    }

    public QName getName() {

        return name;

    }

    public String getValue() {

        return value;

    }

    public boolean isSpecified() {

        return specified;

    }

    public String getDTDType() {

        return dtdType;

    }

}