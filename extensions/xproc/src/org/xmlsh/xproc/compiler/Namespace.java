/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.xproc.compiler;

import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NodeInfo;

class Namespace {
	private		String	prefix;
	private		String	uri;
	Namespace(String prefix, String uri) {
		super();
		this.prefix = prefix;
		this.uri = uri;
	}
	Namespace(NodeInfo node ,  int code ) {
		NamePool np = node.getNamePool();
		this.prefix = np.getPrefixFromNamespaceCode(code);
		this.uri = np.getURIFromNamespaceCode(code);
	}
	/**
	 * @return the prefix
	 */
	String getPrefix() {
		return prefix;
	}
	/**
	 * @return the uri
	 */
	 String getUri() {
		return uri;
	}


}



//
//
//Copyright (C) 2008,2009 , David A. Lee.
//
//The contents of this file are subject to the "Simplified BSD License" (the "License");
//you may not use this file except in compliance with the License. You may obtain a copy of the
//License at http://www.opensource.org/licenses/bsd-license.php 
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied.
//See the License for the specific language governing rights and limitations under the License.
//
//The Original Code is: all this file.
//
//The Initial Developer of the Original Code is David A. Lee
//
//Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
//Contributor(s): none.
//
