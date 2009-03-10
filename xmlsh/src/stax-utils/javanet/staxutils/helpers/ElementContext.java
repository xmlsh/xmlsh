/*
 * $Id: ElementContext.java,v 1.1 2004/07/15 02:13:54 cniles Exp $
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import javanet.staxutils.SimpleNamespaceContext;

/**
 * Encapsulates access to contextual element information, such as the element name,
 * attributes, and namespaces. This class is useful for recording element information
 * in a stack to keep track of the current element c[position in a document.
 * 
 * @author Christian Niles
 * @version $Revision: 1.1 $
 */
public class ElementContext extends SimpleNamespaceContext {

    /** The element name. */
    private QName name;

    /** The encapsulating context. */
    private ElementContext parent;

    /** Ordered list of attributes. */
    private List attributeNames;

    /** Attribute values, keyed by their names. */
    private Map attributes;

    /** Ordered list of namespace prefixes. */
    private List namespacePrefixes;

    /** Whether the element is an empty element or not. */
    private boolean isEmpty;

    /** Whether the context has been closed for further edits. */
    private boolean readOnly;

    /**
     * Constructs a new <code>ElementContext</code> with the provided name and no
     * enclosing context.
     * 
     * @param name The element name.
     */
    public ElementContext(QName name) {

        this.name = name;

    }

    /**
     * Constructs a new <code>ElementContext</code> with the provided name and empty
     * value, and no enclosing context.
     * 
     * @param name The element name.
     * @param isEmpty Whether the element is an empty element or not.
     */
    public ElementContext(QName name, boolean isEmpty) {

        this.name = name;
        this.isEmpty = isEmpty;

    }

    /**
     * Constructs a new <code>ElementContext</code> with the provided name and
     * namespace context.
     * 
     * @param name The element name.
     * @param context The enclosing namespace context.
     */
    public ElementContext(QName name, NamespaceContext context) {

        super(context);
        this.name = name;

    }

    /**
     * Constructs a new <code>ElementContext</code> with the provided name and
     * enclosing context.
     * 
     * @param name The element name.
     * @param parent The enclosing element context.
     */
    public ElementContext(QName name, ElementContext parent) {

        super(parent);
        this.name = name;
        this.parent = parent;

    }

    /**
     * Constructs a new <code>ElementContext</code> with the provided name and
     * enclosing context.
     * 
     * @param name The element name.
     * @param parent The enclosing element context.
     * @param isEmpty Whether the element is an empty element or not.
     */
    public ElementContext(QName name, ElementContext parent, boolean isEmpty) {

        super(parent);
        this.name = name;
        this.parent = parent;
        this.isEmpty = isEmpty;

    }

    /**
     * Returns a reference to the enclosing <code>ElementContext</code>.
     * 
     * @return The enclosing context, or <code>null</code>.
     */
    public ElementContext getParentContext() {

        return parent;

    }

    /**
     * Determines if this context has an enclosing context or not.
     * 
     * @return <code>true</code> if this context is the root context and has no
     * 		enclosing context, <code>false</code> otherwise.
     */
    public boolean isRoot() {

        return parent == null;

    }

    /**
     * Returns the qualified name associated with the context.
     * 
     * @return The qualified name of the context.
     */
    public QName getName() {

        return name;

    }

    /**
     * Returns the current context path.
     * 
     * @return A string representing the context path.
     */
    public String getPath() {

        return appendPath(new StringBuffer()).toString();

    }

    /**
     * @see #getPath()
     */
    public String toString() {

        return getPath();

    }

    /**
     * Appends the current context path to a {@link StringBuffer}.
     * 
     * @param buffer The buffer to which to append the context path.
     * @return The provided buffer.
     */
    public StringBuffer appendPath(StringBuffer buffer) {

        if (parent != null) {

            parent.appendPath(buffer);

        }
        return buffer.append('/').append(name);

    }

    /**
     * Determines the number of enclosing contexts.
     * 
     * @return The number of enclosing contexts.
     */
    public int getDepth() {

        if (parent == null) {

            return 0;

        } else {

            return parent.getDepth() + 1;

        }

    }

    /**
     * Constructs a new child <code>ElementContext</code> with the specified name.
     * 
     * @param name The name associated with the child context.
     * @return The newly constructed child context.
     * @throws IllegalStateException If this context is empty.
     */
    public ElementContext newSubContext(QName name) {

        if (!isEmpty()) {

            return new ElementContext(name, this);

        } else {
            
            throw new IllegalStateException("ElementContext is empty");
            
        }
        
    }

    /**
     * Constructs a new child <code>ElementContext</code> with the specified name
     * and empty value.
     * 
     * @param name The name associated with the child context.
     * @param isEmpty Whether the child context represents an empty element.
     * @return The newly constructed child context.
     * @throws IllegalStateException If this context is empty.
     */
    public ElementContext newSubContext(QName name, boolean isEmpty) {

        if (!isEmpty()) {

            return new ElementContext(name, this, isEmpty);

        } else {
            
            throw new IllegalStateException("ElementContext is empty");
            
        }

    }

    /**
     * Adds an attribute to the context with the specified name and value.
     * 
     * @param name The attribute name.
     * @param value The attribute value.
     * @throws IllegalStateException If the context is read-only.
     */
    public void putAttribute(QName name, String value) {

        if (isReadOnly()) {

            throw new IllegalStateException("ElementContext is readOnly");

        } else if (attributes == null) {

            attributes = new HashMap();
            attributeNames = new ArrayList();

        }

        attributeNames.add(name);
        attributes.put(name, value);

    }

    /**
     * Adds a namespace declaration to this context with the specified prefix and
     * namespace uri.
     * 
     * @param prefix The namespace prefix.
     * @param nsURI The namespace uri.
     */
    public void putNamespace(String prefix, String nsURI) {

        if (isReadOnly()) {

            throw new IllegalStateException("ElementContext is readOnly");

        }

        if (namespacePrefixes == null) {

            namespacePrefixes = new ArrayList();

        }

        if (prefix.length() == 0) {

            // default namespace
            namespacePrefixes.add(prefix);
            super.setDefaultNamespace(nsURI);

        } else {

            namespacePrefixes.add(prefix);
            super.setPrefix(prefix, nsURI);

        }

    }

    /**
     * Returns the number of attributes defined in this context.
     * 
     * @return The number of attributes defined in the context.
     */
    public int attributeCount() {

        if (attributes != null) {

            return attributes.size();

        } else {

            return 0;

        }

    }

    /**
     * Returns the value of the <code>idx</code><sup>th</sup> attribute defined on
     * the context.
     * 
     * @param idx The zero-based index of the attribute value to retrieve.
     * @return The value of the <code>idx</code><sup>th</sup> attribute defined on
     * 		the context.
     * @throws IndexOutOfBoundsException If the index is out of bounds.
     */
    public String getAttribute(int idx) {

        return getAttribute(getAttributeName(idx));

    }

    /**
     * Returns the name of the <code>idx</code><sup>th</sup> attribute defined on
     * the context.
     * 
     * @param idx The zero-based index of the attribute name to retrieve.
     * @return The name of the <code>idx</code><sup>th</sup> attribute defined on
     * 		the context.
     * @throws IndexOutOfBoundsException If the index is out of bounds.
     */
    public QName getAttributeName(int idx) {

        if (attributeNames != null) {

            return (QName) attributeNames.get(idx);

        } else {

            throw new IndexOutOfBoundsException("Attribute index " + idx
                    + " doesn't exist");

        }

    }

    /**
     * Returns the value of a named attribute.
     * 
     * @param name The name of the attribute value to retrieve.
     * @return The value of the named attribute, or <code>null</code>.
     */
    public String getAttribute(QName name) {

        if (attributes != null) {

            return (String) attributes.get(name);

        } else {

            return null;

        }

    }

    /**
     * Determines if an attribute with the specified name exists in this context.
     * 
     * @param name The name of the attribute.
     * @return <code>true</code> if an attribute with the specified name has been
     * 		defined in this context, <code>false</code> otherwise.
     */
    public boolean attributeExists(QName name) {

        if (attributes != null) {

            return attributes.containsKey(name);

        } else {

            return false;

        }

    }

    /**
     * Returns an {@link Iterator} over the names of all attributes defined in this
     * context. The returned iterator will not support the {@link Iterator#remove()}
     * operation.
     * 
     * @return An {@link Iterator} over the names of all attributes defined in this
     *     context.
     */
    public Iterator attributeNames() {

        if (attributeNames != null) {

            return Collections.unmodifiableList(attributeNames).iterator();

        } else {

            return Collections.EMPTY_LIST.iterator();

        }

    }

    /**
     * Determines the number of namespaces declared in this context.
     * 
     * @return The number of namespaces declared in this context.
     */
    public int namespaceCount() {

        if (namespacePrefixes != null) {

            return namespacePrefixes.size();

        } else {

            return 0;

        }

    }

    /**
     * Returns the URI of the <code>idx</code><sup>th</sup> namespace declaration
     * defined in this context.
     * 
     * @param idx The index of the namespace URI to return.
     * @return The URI of the <code>idx</code><sup>th</sup> namespace declaration
     * 		defined in this context.
     * @throws IndexOutOfBoundsException If the index is out of bounds.
     */
    public String getNamespaceURI(int idx) {

        return this.getNamespaceURI(getNamespacePrefix(idx));

    }

    /**
     * Returns the prefix of the <code>idx</code><sup>th</sup> namespace declaration
     * defined in this context.
     * 
     * @param idx The index of the namespace prefix to return.
     * @return The prefix of the <code>idx</code><sup>th</sup> namespace declaration
     * 		defined in this context.
     * @throws IndexOutOfBoundsException If the index is out of bounds.
     */
    public String getNamespacePrefix(int idx) {

        if (namespacePrefixes != null) {

            return (String) namespacePrefixes.get(idx);

        } else {

            throw new IndexOutOfBoundsException("Namespace index " + idx
                    + " doesn't exist");

        }

    }

    /**
     * Whether this context may be edited or not.
     * 
     * @return <code>true</code> if no additional modifications may be made to this
     * 		context, <code>false</code> otherwise.
     */
    public boolean isReadOnly() {

        return readOnly;

    }

    /**
     * Prevents any further additions to this context.
     */
    public void setReadOnly() {

        this.readOnly = true;

    }

    /**
     * Whether this context represents an emtpy element. Empty contexts may not
     * enclose any other contexts.
     * 
     * @return <code>true</code> if this context represents an emtpy element,
     * 		<code>false</code> otherwise.
     */
    public boolean isEmpty() {

        return isEmpty;

    }

}