/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import org.xmlsh.util.NameValue;
import org.xmlsh.util.NameValueList;
import org.xmlsh.util.Util;

import java.util.ArrayList;

@SuppressWarnings("serial") 
public class PortList<P extends IPort> extends NameValueList< P, NamedPort<P>  >
{
	
	public PortList()
	{
		super();
	}
	
	void close() throws CoreException
	{
		for( NameValue<P> e : this )
			e.getValue().release();
	}
	
	PortList( PortList<P> that )
	{
		for(NamedPort<P> e : that ){
			e.getValue().addRef();
			add( e );
		}
	}
	
	P getPort( String name )
	{

		NamedPort<P> np = findName(name);
		return np == null ? null : np.getValue();
	}
	
	P getPort( P value )
	{

		NamedPort<P> np = findValue(value);
		return np == null ? null : np.getValue();
	}
	
	public P removePort(String name) {
		NamedPort<P> np = removeName(name);
		if( np != null )
			return np.getPort();
		return null;
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
