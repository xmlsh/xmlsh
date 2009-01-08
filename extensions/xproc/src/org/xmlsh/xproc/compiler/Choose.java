/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.xproc.compiler;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;

/*
 <p:choose
  name? = NCName>
    (p:xpath-context?,
     p:variable*,
     p:when*,
     p:otherwise?)
</p:choose>


 */
class Choose extends AbstractStep {
	
	XPathContext	xpath_context;
	List<Variable>	variables = new ArrayList<Variable>();
	List<When>		whens = new ArrayList<When>();
	Otherwise		otherwise;
	
	static Choose create(XdmNode node){
		Choose step = new Choose();
		step.parse(node);
		return step;
	}

	protected void parse(XdmNode node) {
		super.parse(node);
		parseChildren(node);
	}

	private void parseChildren(XdmNode parent) {
		XdmSequenceIterator children = parent.axisIterator(Axis.CHILD);

		
		while( children.hasNext() ){
			XdmItem item=children.next();
			if( item instanceof XdmNode ){
				XdmNode child = (XdmNode) item ;
				if( child.getNodeKind() != XdmNodeKind.ELEMENT )
					continue ;
				
				QName name = child.getNodeName();
				if( name.equals( Names.kXPATH_CONTEXT))
					xpath_context = XPathContext.create(child);
				else
				if( name.equals(Names.kVARIABLE))
					variables.add( Variable.create(child));
				else
				if( name.equals(Names.kWHEN))
					whens.add( When.create(child));
	
				
				
			}
		}
				
		
	}

	@Override
	void serialize(OutputContext c ) {
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
