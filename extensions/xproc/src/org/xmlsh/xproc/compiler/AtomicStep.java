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
<p:atomic-step
name? = NCName>
  (p:input |
   p:with-option |
   p:with-param |
   p:log)*
</p:atomic-step>
*/


class AtomicStep  extends AbstractStep {

	QName		type;
	InputList			inputs 		= new InputList();
	List<WithOption>	withoptions = new ArrayList<WithOption>();
	List<WithParam>		withparams  = new ArrayList<WithParam>();
	List<Log>			logs		= new ArrayList<Log>();
	
	
	static AtomicStep		create( XdmNode node )
	{
		AtomicStep step = new AtomicStep();
		step.parse(node);
		return step;
		
	}
	
	protected void parse( XdmNode node )
	{
		super.parse(node);
		type = node.getNodeName();
		
		
		
		
		XdmSequenceIterator attrs = node.axisIterator(Axis.ATTRIBUTE);
		while( attrs.hasNext() ){
			XdmItem item=attrs.next();
			if( item instanceof XdmNode ){
				XdmNode child = (XdmNode) item ;
				/*
				 * Syntactic equivilence
				 * attribute names translate to with-options
				 */
				if( child.getNodeKind() == XdmNodeKind.ATTRIBUTE){
					QName attrName = child.getNodeName();
					if( attrName.getPrefix().equals("")){
						
						withoptions.add(new WithOption( attrName.getLocalName() , XProcUtil.quote(child.getStringValue())));
						
					}
					continue ;
					
				}
			}
		}
		
		
		
		
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
				if( name.equals( Names.kINPUT))
					inputs.add( Input.create(child));
				else
				if( name.equals( Names.kWITH_OPTION))
					withoptions.add( WithOption.create(child));
				else
				if( name.equals( Names.kWITH_PARAM))
					withparams.add( WithParam.create(child));
				else
				if( name.equals( Names.kLOG))
					logs.add( Log.create(child));

				

				

			}
		}
	}
	
	
	
	@Override
	void serialize(OutputContext c) {
		
		for( Namespace ns : namespaces ){

			
			c.addBody("xmlns:" + ns.getPrefix() + "=\"" + ns.getUri() + "\" ");
			
			
			
		}
		
		
		
		c.addBody( c.getStepPrefix() + ":");
		c.addBody( type.getLocalName() );
		
		// Add namespaces
		
		
		for( WithOption o : withoptions )
		{
			c.addBody(" ");
			o.serialize(c);

		}
		
		// Add Input as "<" or "<<" or "<|" 
		
		
		
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
