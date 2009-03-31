/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.xproc.compiler;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;

abstract class Binding {

	public static Binding create(XdmNode node) 
	{

		if( node == null )
			return null ;
		Binding decl = null;
		QName name = node.getNodeName();
		if( name.equals(Names.kEMPTY ))
			decl = new Empty();
		else
		if( name.equals(Names.kDOCUMENT ))
			decl = new Document();
		else
		if( name.equals(Names.kINLINE))
			decl = new Inline();
		else
		if( name.equals(Names.kDATA))
			decl = new Data();
		else
		if( name.equals( Names.kPIPE))
			decl = new Pipe();
		if( decl != null )
			decl.parse(node);
		return decl;	
		
	}

	abstract void parse(XdmNode node) ;

	abstract void serialize(OutputContext c);
	
	abstract boolean isInput();

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
