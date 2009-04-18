/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.xproc.compiler;

import net.sf.saxon.s9api.XdmNode;
/*
 <p:option
  name = QName
  required? = boolean />
  
  
  <p:option
  name = QName
  select = XPathExpression />
 
 */
public class Option {
	String		name;
	boolean 	required;
	XPathExpression		select;
	
	static Option create( XdmNode node )
	{
		Option 	opt = new Option( );
		opt.parse( node );
		return opt;
	}
	
	void parse(XdmNode node) {
		
		name = XProcUtil.getAttrString(node, "name");
		required = XProcUtil.getAttrBool( node ,"required" , false );
		select = new XPathExpression(XProcUtil.getAttrString(node, "select"),false);
		
		
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
