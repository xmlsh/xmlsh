/*
 * $Id: EmptyNamespaceContext.java,v 1.2 2004/07/08 14:29:42 cniles Exp $
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

import java.util.Collections;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;

/**
 * {@link ExtendedNamespaceContext} that contains no namespaces.
 * 
 * @author Christian Niles
 * @version $Revision: 1.2 $
 */
public final class EmptyNamespaceContext
        implements
            ExtendedNamespaceContext,
            StaticNamespaceContext {

    public static final EmptyNamespaceContext INSTANCE = new EmptyNamespaceContext();

    public static final NamespaceContext getInstance() {

        return INSTANCE;

    }

    public String getNamespaceURI(String prefix) {

        return null;

    }

    public String getPrefix(String nsURI) {

        return null;

    }

    public Iterator getPrefixes(String nsURI) {

        return Collections.EMPTY_SET.iterator();

    }

    public NamespaceContext getParent() {

        return null;

    }

    public boolean isPrefixDeclared(String prefix) {

        return false;

    }

    public Iterator getPrefixes() {

        return Collections.EMPTY_LIST.iterator();

    }

    public Iterator getDeclaredPrefixes() {

        return Collections.EMPTY_LIST.iterator();

    }

}