/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.shell;

import java.util.Properties;

import net.sf.saxon.Configuration;
import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.lib.SerializerFactory;
import net.sf.saxon.serialize.XMLEmitter;
import net.sf.saxon.serialize.XMLIndenter;

public class XmlshSerializerFactory extends SerializerFactory {

	public XmlshSerializerFactory(Configuration config) {
		super(config);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see net.sf.saxon.lib.SerializerFactory#newXMLIndenter(net.sf.saxon.serialize.XMLEmitter, java.util.Properties)
	 */
	@Override
	protected ProxyReceiver newXMLIndenter(XMLEmitter next, Properties outputProperties) {
		// TODO Auto-generated method stub
		 XMLIndenter r = new XmlshXMLIndenter(next);
	        r.setOutputProperties(outputProperties);
	     return r;
	}
	

}



//
//
//Copyright (C) 2008-2014   David A. Lee.
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
