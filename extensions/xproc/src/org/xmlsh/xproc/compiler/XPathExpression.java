/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.xproc.compiler;

import org.xmlsh.util.Util;

class XPathExpression {
	String		xpath;		// xpath expression
	boolean		literal;		// literal string value

	XPathExpression( String xpath , boolean literal )
	{
		this.xpath = xpath ;
		this.literal = literal ;
	}
	



	public void serialize(OutputContext c) 
		{
			if( literal )
				c.addBody( XProcUtil.quote(xpath));
			else
				c.addBody(" $(xmlns:p=java:org.xmlsh.xproc.util.XPathFunctions xpath " + XProcUtil.quote(xpath) + " <{" + c.getPrimaryInput().getPortVariable()+"} ) ");
			
		}




	public boolean isEmpty() {
		return Util.isEmpty(xpath);
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
