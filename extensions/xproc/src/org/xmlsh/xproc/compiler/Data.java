/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.xproc.compiler;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
/*
 <p:data
  href = anyURI
  wrapper? = QName
  content-type? = string />

 */
class Data extends Binding {

	String	href;
	QName  wrapper;
	String	content_type;
	
	@Override
	void parse(XdmNode node) {
		href = XProcUtil.getAttrString(node, "href");
		wrapper  = XProcUtil.getAttrQName( node , "wrapper");
		content_type =  XProcUtil.getAttrString(node, "content-type");
	}

	@Override
	void serialize(OutputContext c) {
		// TODO Auto-generated method stub
		
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
