/*
 * $Id: StartElementEvent.java,v 1.5 2004/07/15 02:11:01 cniles Exp $
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;

import javanet.staxutils.NamespaceContextAdapter;
import javanet.staxutils.StaticNamespaceContext;

/**
 * {@link StartElement} event implementation. This event will coalesce its namespaces
 * into an internal {@link NamespaceContext}, available via
 * {@link #getNamespaceContext()}. It will also create any implicit namespaces
 * necessary to satisfy the element's name and those of its attributes.
 * 
 * @author Christian Niles
 * @version $Revision: 1.5 $
 */
public class StartElementEvent extends AbstractXMLEvent implements StartElement {

    /** The qualified element name. */
    protected QName name;

    /** The element attributes. */
    protected Map attributes;

    /** The element namespaces. */
    protected Map namespaces;

    /** The namespace context. */
    protected NamespaceContext namespaceCtx;

    public StartElementEvent(QName name, NamespaceContext namespaceCtx,
            Location location) {

        super(location);
        this.name = name;
        this.namespaceCtx = new StartElementContext(namespaceCtx);

    }

    public StartElementEvent(QName name, Iterator attributes,
            Iterator namespaces, NamespaceContext namespaceCtx,
            Location location, QName schemaType) {

        super(location, schemaType);
        this.namespaceCtx = new StartElementContext(namespaceCtx);

        mergeNamespaces(namespaces);
        mergeAttributes(attributes);

        QName newName = processQName(name);
        this.name = (newName == null ? name : newName);

    }

    public StartElementEvent(StartElement that) {

        this(that.getName(),
                that.getAttributes(),
                that.getNamespaces(),
                that.getNamespaceContext(),
                that.getLocation(),
                that.getSchemaType());

    }

    /** Returns {@link #START_ELEMENT}. */
    public int getEventType() {

        return START_ELEMENT;

    }

    public QName getName() {

        return name;

    }

    public Attribute getAttributeByName(QName name) {

        if (attributes != null) {

            return (Attribute) attributes.get(name);

        } else {

            return null;

        }

    }

    public Iterator getAttributes() {

        if (attributes != null) {

            return attributes.values().iterator();

        } else {

            return Collections.EMPTY_LIST.iterator();

        }

    }

    public NamespaceContext getNamespaceContext() {

        return namespaceCtx;

    }

    public Iterator getNamespaces() {

        if (namespaces != null) {

            return namespaces.values().iterator();

        } else {

            return Collections.EMPTY_LIST.iterator();

        }

    }

    public String getNamespaceURI(String prefix) {

        return getNamespaceContext().getNamespaceURI(prefix);

    }

    /**
     * Performs the task of adding {@link Attribute}s into the internal
     * {@link #attributes} map. Along the way, it will also create the necessary
     * {@link Namespace} events to satisfy attribute namespaces.
     * 
     * @param iter An iterator over a set of {@link Attributes}.
     */
    private void mergeAttributes(Iterator iter) {

        if (iter == null) {

            return;

        }

        while (iter.hasNext()) {

            Attribute attr = (Attribute) iter.next();

            if (attributes == null) {

                attributes = new HashMap();

            }

            // check if the attribute QName has the proper mapping
            QName attrName = attr.getName();
            QName newName = processQName(attrName);
            if (newName != null) {

                // need to generate a new attribute with the new qualified name
                Attribute newAttr = new AttributeEvent(newName, null, attr);
                attributes.put(newName, newAttr);

            } else {

                // the attribute is fine
                attributes.put(attrName, attr);

            }

        }

    }

    /**
     * Performs the task of adding {@link Namespace}s into the internal
     * {@link #namespaces} map.
     * 
     * @param iter An iterator over a set of {@link Namespaces}.
     */
    private void mergeNamespaces(Iterator iter) {

        if (iter == null) {

            return;

        }

        // for each namespace, add it to the context, and place it in the list
        while (iter.hasNext()) {

            Namespace ns = (Namespace) iter.next();
            String prefix = ns.getPrefix();

            if (namespaces == null) {

                namespaces = new HashMap();

            }

            if (!namespaces.containsKey(prefix)) {

                namespaces.put(prefix, ns);

            }

        }

    }

    /**
     * Processes a {@link QName}, possibly rewriting it to match the current
     * namespace context. If necessary, a new {@link Namespace} will be added to
     * support the name's prefix.
     * 
     * @param name The {@link QName} to process.
     * @return The new name, or <code>null</code> if no changes were necessary.
     */
    private QName processQName(QName name) {

        String nsURI = name.getNamespaceURI();
        String prefix = name.getPrefix();

        if (nsURI == null || nsURI.length() == 0) {

            // name belongs to no namespace. This can only be okay if the name is
            // an attribute name, or the default namespace hasn't been overridden.
            // either way, no prefix should be allowed on the name, so the best we
            // can do is rewrite the name to make sure no prefix is present

            // clear any prefix from the name
            if (prefix != null && prefix.length() > 0) {

                return new QName(name.getLocalPart());

            } else {

                return name;

            }

        }

        // namespace uri is non-null after this point

        String resolvedNS = namespaceCtx.getNamespaceURI(prefix);
        if (resolvedNS == null) {

            // if the prefix is not empty, then we should default the prefix
            if (prefix != null && prefix.length() > 0) {

                if (namespaces == null) {

                    namespaces = new HashMap();

                }
                namespaces.put(prefix, new NamespaceEvent(prefix, nsURI));

            }

            return null;

        } else if (!resolvedNS.equals(nsURI)) {

            // The prefix is bound to a different namespace, so we'll have to
            // search for existing prefixes bound to the namespace uri, or
            // generate a new namespace binding.
            String newPrefix = namespaceCtx.getPrefix(nsURI);
            if (newPrefix == null) {

                // no existing prefix; need to generate a new prefix
                newPrefix = generatePrefix(nsURI);

            }

            // return the newly prefixed name
            return new QName(nsURI, name.getLocalPart(), newPrefix);

        } else {

            // prefix has already been bound to the namespace; nothing to do
            return null;

        }

    }

    /**
     * Generates a new namespace prefix for the specified namespace URI that
     * doesn't collide with any existing prefix.
     * 
     * @param nsURI The URI for which to generate a prefix.
     * @return The new prefix.
     */
    private String generatePrefix(String nsURI) {

        String newPrefix;
        int nsCount = 0;
        do {

            newPrefix = "ns" + nsCount;
            nsCount++;

        } while (namespaceCtx.getNamespaceURI(newPrefix) != null);

        if (namespaces == null) {

            namespaces = new HashMap();

        }
        namespaces.put(newPrefix, new NamespaceEvent(newPrefix, nsURI));

        return newPrefix;

    }

    /**
     * Adapts another {@link NamespaceContext} to expose this tag's declared
     * namespaces.
     *
     * @author Christian Niles
     * @version $Revision: 1.5 $
     */
    private final class StartElementContext extends NamespaceContextAdapter
            implements
                StaticNamespaceContext {

        public StartElementContext(NamespaceContext namespaceCtx) {

            super(namespaceCtx);

        }

        public String getNamespaceURI(String prefix) {

            if (namespaces != null && namespaces.containsKey(prefix)) {

                Namespace namespace = (Namespace) namespaces.get(prefix);
                return namespace.getNamespaceURI();

            } else {

                return super.getNamespaceURI(prefix);

            }

        }

        public String getPrefix(String nsURI) {

            for (Iterator i = getNamespaces(); i.hasNext();) {

                Namespace ns = (Namespace) i.next();
                if (ns.getNamespaceURI().equals(nsURI)) {

                    return ns.getPrefix();

                }

            }

            return super.getPrefix(nsURI);

        }

        public Iterator getPrefixes(String nsURI) {

            // lazily-loaded set to store found prefixes
            List prefixes = null;

            // add our prefixes first
            if (namespaces != null) {

                for (Iterator i = namespaces.values().iterator(); i.hasNext();) {

                    Namespace ns = (Namespace) i.next();
                    if (ns.getNamespaceURI().equals(nsURI)) {

                        if (prefixes == null) {

                            prefixes = new ArrayList();

                        }

                        String prefix = ns.getPrefix();
                        prefixes.add(prefix);

                    }

                }

            }

            // copy parent prefixes that aren't redefined by this context
            Iterator parentPrefixes = super.getPrefixes(nsURI);
            while (parentPrefixes.hasNext()) {

                String prefix = (String) parentPrefixes.next();

                // only add the prefix if we haven't redefined it
                if (namespaces != null && !namespaces.containsKey(prefix)) {

                    if (prefixes == null) {

                        prefixes = new ArrayList();

                    }
                    prefixes.add(prefix);

                }

            }

            return prefixes == null
                    ? Collections.EMPTY_LIST.iterator()
                    : prefixes.iterator();

        }

    }

}