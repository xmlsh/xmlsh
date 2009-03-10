/*
 * $Id: NamespaceContextAdapter.java,v 1.1 2004/07/05 23:12:40 cniles Exp $
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
 * {@link NamespaceContext} that wraps another context. This class is useful for
 * hiding the underlying implementation, or adding additional functionality on top of
 * another context.
 * 
 * @author Christian Niles
 * @version $Revision: 1.1 $
 */
public class NamespaceContextAdapter implements NamespaceContext {

    /** The wrapped context. */
    protected NamespaceContext namespaceCtx;

    public NamespaceContextAdapter() {

    }

    public NamespaceContextAdapter(NamespaceContext namespaceCtx) {

        this.namespaceCtx = namespaceCtx;

    }

    public String getNamespaceURI(String prefix) {

        if (namespaceCtx != null) {

            return this.namespaceCtx.getNamespaceURI(prefix);

        } else {

            return null;

        }

    }

    public String getPrefix(String nsURI) {

        if (this.namespaceCtx != null) {

            return this.namespaceCtx.getPrefix(nsURI);

        } else {

            return null;

        }

    }

    public Iterator getPrefixes(String nsURI) {

        if (this.namespaceCtx != null) {

            return this.namespaceCtx.getPrefixes(nsURI);

        } else {

            return Collections.EMPTY_LIST.iterator();

        }

    }

}