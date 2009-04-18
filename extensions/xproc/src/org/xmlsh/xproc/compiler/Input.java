/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.xproc.compiler;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;
import org.xmlsh.util.Util;
import org.xmlsh.xproc.util.XProcException;


/*

<p:input
  port = NCName
  sequence? = boolean
  primary? = boolean
  kind? = "document"
  select? = XPathExpression>
    (p:empty |
      (p:document |
       p:inline |
       p:data)+)?
</p:input>

 */


class Input {
	
	String		step_name;		// step's name
	String		port;
	boolean		sequence;
	Boolean		primary;		// Use an object for primary to distinguish between unset/true/false
	boolean		step;			// If true this is a 'step' input as apposed to a 'pipeline' input
	
	String 		kind;
	XPathExpression		select;

	
	BindingList	bindings = new BindingList();
	
	Input(String step_name , boolean step) {this.step_name = step_name;this.step = step ;} 
	
    Input(String step_name, String port, String kind, boolean primary, boolean step ) 
    {
    	this( step_name , step );

		this.kind = kind ;
		this.primary = primary;
		this.port = port;
	}

	static Input create( String step_name , XdmNode node , boolean step )
	{
	
		Input input = new Input(step_name, step);
		input.parse(node);
		return input;
	}

	void parse(XdmNode node) {
		port 		= XProcUtil.getAttrString(node, "port");
		sequence 	= XProcUtil.getAttrBool(node, "sequence", false);
		primary 	= XProcUtil.getAttrBoolean(node, "primary");
		kind		= XProcUtil.getAttrString(node, "kind");
		select		= new XPathExpression( XProcUtil.getAttrString(node, "select"),false);
		
		// parse children
		parseChildren( node );
		
	}

	protected void parseChildren(XdmNode parent) {
		XdmSequenceIterator children = parent.axisIterator(Axis.CHILD);
	
		
		while( children.hasNext()  ){
			XdmItem item=children.next();
			if( item instanceof XdmNode ){
				XdmNode child = (XdmNode) item ;
				if( child.getNodeKind() != XdmNodeKind.ELEMENT )
					continue ;
				
				bindings.add( Binding.create(child));
			}
		}
	}

	void serialize(OutputContext c) throws XProcException {
		
		boolean bRead = false ;
		if( select == null || select.isEmpty() )
		{
			// If this input is the default input and its parent is the same, then dont add
			// an xread
			if( ! step && ! c.isDerivedInput(this)){
				c.addPreamble("xread " + getPortVariable() );
				bRead = true ;
			}
			
		}
		else
		{
			c.addPreamble("xpath " + XProcUtil.quote(select.xpath) + " >{" +getPortVariable()  +"}");
			bRead = true ;
		}
		if(! Util.isEqual(port , "source" ) )
			c.addPreamble("<(" + port + ")");
		

		
		bindings.serialize(c);
		c.addPreambleLine("");
		
		//if(! bindings.hasInputs() )
		if( bRead )
			c.addBody(" <{" + getPortVariable() +"}");
		
		
	}
	String getPortVariable()
	{
		
		return "_" + step_name + "." + port;
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
