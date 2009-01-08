/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.xproc.compiler;

import java.util.ArrayList;
import java.util.List;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;

/*
 
 
<p:output
port = NCName
sequence? = boolean
primary? = boolean />

<p:output
  port = NCName
  sequence? = boolean
  primary? = boolean>
    (p:empty |
      (p:pipe |
       p:document |
       p:inline |
       p:data)+)?
</p:output>



*/



class Output {
	String port;
	boolean sequence;
	boolean primary;

	BindingList	bindings = new BindingList();
	
	Output()
	{
	}
	
	Output(String port, boolean primary) {
		this.port = port ;
		this.primary = primary;
	}

	void parse(XdmNode node) {
		port = XProcUtil.getAttrString(node, "port");
		sequence = XProcUtil.getAttrBool( node ,"sequence" , false );
		primary = XProcUtil.getAttrBool(node, "primary" , false );
		// parse children
		parseChildren( node );
		
	}

	protected void parseChildren(XdmNode parent) {
		XdmSequenceIterator children = parent.axisIterator(Axis.CHILD);
		while( children.hasNext() ){
			XdmItem item=children.next();
			if( item instanceof XdmNode ){
				XdmNode child = (XdmNode) item ;
				if( child.getNodeKind() != XdmNodeKind.ELEMENT )
					continue ;
				bindings.add( Binding.create(child));
			}
		}
	}

	public static Output create(XdmNode child) {
		Output 	out = new Output( );
		out.parse( child );
		return out;	
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
