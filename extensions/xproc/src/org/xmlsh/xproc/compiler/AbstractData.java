/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.xproc.compiler;

import java.util.ArrayList;
import java.util.List;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;

/*
 * Base class for Variable, WithOption
 */
class AbstractData 
{
	String				name;
	XPathExpression 	select;
	Binding	binding;
	List<Namespaces> namespacess = new ArrayList<Namespaces>();// double-ss
	
	void parse( XdmNode node )
	{
		name 	= XProcUtil.getAttrString(node, "name");
		select 	= new XPathExpression(XProcUtil.getAttrString(node, "select"),false);
		parseChildren(node);
		
	}

	protected void parseChildren(XdmNode node) {
		XdmNode child = XProcUtil.getFirstChild(node);
		if( child !=null ){
			QName name = node.getNodeName();
			if( name.equals(Names.kNAMESPACES))
				namespacess.add(Namespaces.create(node));
			else
				binding = Binding.create(child);
		}

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
