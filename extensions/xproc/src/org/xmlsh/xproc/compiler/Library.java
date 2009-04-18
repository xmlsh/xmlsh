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

/*
 <p:library
  psvi-required? = boolean
  xpath-version? = string
  exclude-inline-prefixes? = prefix list>
    (p:import |
     p:declare-step |
     p:pipeline)*
</p:library>

 */
class Library {
	boolean 				psvi_required;
	String 					xpath_version;
	String[]	 			exclude_inline_prefixes;
	
	
	// Step declarations including libraries
	List<DeclareStep>	steps = new ArrayList<DeclareStep>();
	List<Import>		imports = new ArrayList<Import>();
	
	
	

	static Library create( XdmNode node )
	{
		Library step = new Library();
		step.parse(node);
		return step;
	}
	
	
	void parse(XdmNode node)
	{
		psvi_required 	= XProcUtil.getAttrBool(node,"psvi-required",false);
		xpath_version 	= XProcUtil.getAttrString(node, "xpath-version");
		exclude_inline_prefixes = XProcUtil.getAttrList( node , "exclude-inline-prefixes");
		
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
				QName name = child.getNodeName();
				if( name.equals(Names.kDECLARE_STEP))
					steps.add( DeclareStep.create(child));
				else
				if( name.equals( Names.kIMPORT))
					imports.add( Import.create(child));
				else
				if( name.equals( Names.kPIPELINE))
					steps.add( Pipeline.create(child));
			}
			
			
			
		}
		
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
