/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathVariableResolver;

public class XValueVariableResolver implements XPathVariableResolver {

	Map<String,XValue>		mVariables = new HashMap<String,XValue>();
	
	public void	add( String name , XValue value )
	{
		mVariables.put(name, value);
	}
	
	
	public Object resolveVariable(QName variableName) {
	
		XValue value = mVariables.get( variableName.getLocalPart());
		if( value == null )
			return null;
		return value.asXdmValue().getUnderlyingValue();
		
		
	}

}



//
//
//Copyright (C) 2008-2014    David A. Lee.
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
