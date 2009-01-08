/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.xproc.compiler;

import java.util.ArrayList;
import java.util.List;

import net.sf.saxon.om.NamePool;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;
/*
 
 (p:for-each|p:viewport|p:choose|p:group|p:try|p:standard-step|pfx:user-pipeline)+
 
 */
abstract class AbstractStep {
	
	String 				name;		// step name
	List<Namespace>		namespaces = new ArrayList<Namespace>();
	
	

	static AbstractStep create(XdmNode node) 
	{
		QName name = node.getNodeName();
		if( name.equals(Names.kFOR_EACH))
			return ForEach.create(node);
		else
		if( name.equals(Names.kVIEWPORT))
			return Viewport.create(node);
		else
		if( name.equals(Names.kCHOOSE))	
			return Choose.create(node);
		else
		if( name.equals(Names.kGROUP))
			return Group.create(node);
		else
		if( name.equals(Names.kTRY))
			return Try.create(node);
		else
			return AtomicStep.create(node);
		
	}
	
	
	protected void parse( XdmNode node )
	{
		name	= XProcUtil.getAttrString(node, "name");
		parseNamespaces(node);
	}


	private void parseNamespaces(XdmNode node) {

		int [] ns = node.getUnderlyingNode().getDeclaredNamespaces(null);
		if( ns == null )
			return  ;
		

		
		for( int code : ns ){
			namespaces.add(new Namespace(node.getUnderlyingNode(),code));
		}
		
/*		
		
		XdmSequenceIterator attrs = node.axisIterator(Axis.NAMESPACE);
		while( attrs.hasNext() ){
			XdmItem item=attrs.next();
			if( item instanceof XdmNode ){
				XdmNode child = (XdmNode) item ;

				namespaces.add(child);


			}
		}
*/
	}


		


	abstract void serialize(OutputContext c);

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
