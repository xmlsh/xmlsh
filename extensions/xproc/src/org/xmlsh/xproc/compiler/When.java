/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.xproc.compiler;

import java.util.ArrayList;
import java.util.List;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;
import org.xmlsh.xproc.util.XProcException;


/*
 <p:when
  test = XPathExpression>
    (p:xpath-context?,
     (p:output |
      p:log)*,
     subpipeline)
</p:when>


 */
class When {
	

	XPathContext	xpath_context;

	List<OutputOrLog>	outputs = new ArrayList<OutputOrLog>();
	SubPipeline 	subpipeline = new SubPipeline();
	
	String	test;
	
	
	static When create(XdmNode node){
		When when = new When();
		when.parse(node);
		return when;
	}

	protected void parse(XdmNode node) {
		test = XProcUtil.getAttrString(node, "test");
		parseChildren(node);
		
	}

	private void parseChildren(XdmNode parent) {
		Output			output = null;
		Log				log = null ;
		
		XdmSequenceIterator children = parent.axisIterator(Axis.CHILD);
		while( children.hasNext() ){
			XdmItem item=children.next();
			if( item instanceof XdmNode ){
				XdmNode child = (XdmNode) item ;
				if( child.getNodeKind() != XdmNodeKind.ELEMENT )
					continue ;
				QName name = child.getNodeName();
				
				if( name.equals(Names.kXPATH_CONTEXT))
					xpath_context = XPathContext.create(child);
				else
				if( name.equals(Names.kOUTPUT))
					output = Output.create(child);
				else
				if( name.equals(Names.kLOG))
					log = Log.create(child);
				
				if(output != null || log != null ){
					
					outputs.add( new OutputOrLog(output,log));

					output = null;
					log = null;
					continue ;
				}
				
				// Sub pipeline 
				subpipeline.parse(child);
			}
		}
	}

	public void serialize(OutputContext c, boolean first) throws XProcException {
		// {if/elif} xpath -b {expr} ; then 
		//     subpipe
		// 
		if( first )
			c.addBody("if ");
		else
			c.addBody("elif ");
		
		c.addBody("xpath -b " + XProcUtil.quote(test) + " ");
		if( xpath_context != null )
			xpath_context.serialize(c);
		else {
			Input primaryInput = c.getPrimaryInput();
			if( primaryInput != null )
				c.addBodyLine("<{" + primaryInput.getPortVariable() + "}");
			else
				c.addBodyLine("<{_context}");
		}
		c.addBodyLine("then ");
		subpipeline.serialize(c);
		c.addBodyLine("");
		
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
