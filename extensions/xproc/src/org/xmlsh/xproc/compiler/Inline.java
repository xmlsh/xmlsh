/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.xproc.compiler;

import net.sf.saxon.s9api.XdmNode;
import org.xmlsh.util.Util;
/*
 <p:inline
  exclude-inline-prefixes? = prefix list>
    anyElement
 </p:inline>

 */
class Inline extends Binding {

	String[]		exclude_inline_prefixes;
	XdmNode			node;
	
	
	@Override
	void parse(XdmNode node) {
		exclude_inline_prefixes = XProcUtil.getAttrList(node, "exclude-inline-prefixes");
		node = XProcUtil.getFirstChild(node);
		
	}


	@Override
	void serialize(OutputContext c) {
		c.addBody("<<EOF");
		c.addBody(node.toString());
		c.addBody("\nEOF\n");

		
	}

}



//
//
//Copyright (C) 2008, David A. Lee.
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
