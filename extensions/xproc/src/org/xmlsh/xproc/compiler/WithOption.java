/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.xproc.compiler;

import java.io.PrintWriter;

import net.sf.saxon.s9api.XdmNode;

/*
 <p:with-option
  name = QName
  select = XPathExpression>
    ((p:empty |
      p:pipe |
      p:document |
      p:inline |
      p:data)? &
     p:namespaces*)
</p:with-option>



 */
class WithOption extends AbstractData {

	WithOption()
	{
	}
	
	// Shortcut syntax for with-option attributes
	WithOption( String name , String select )
	{
		this.name = name ;
		this.select = new XPathExpression(select,true) ;
	}
	
	
	static WithOption create(XdmNode node) {
		WithOption with = new WithOption();
		with.parse(node);
		return with;
	}

	void serialize(OutputContext c) {
		c.addBody(" -" + name + " " );
		if( select != null )
			select.serialize(c);
		
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
