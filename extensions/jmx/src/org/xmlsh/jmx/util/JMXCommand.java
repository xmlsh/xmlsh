/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.jmx.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;

public abstract class JMXCommand extends XCommand {
	
	
	protected static final String kJMX_NS = "http://www.xmlsh.org/schemas/jmx";
	
	protected XMLStreamWriter mWriter;
	protected SerializeOpts mSerializeOpts;
	protected final static String sCOMMON_OPTS = "c=connect:,u=user:,p=password:";
	public JMXCommand() {
		super();
	}

	protected JMXConnector	getConnector( Options opts ) throws IOException, InvalidArgumentException{
		
		String connect = opts.getOptStringRequired("c");
		
		JMXServiceURL url =
	            new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + connect + "/jmxrmi");
	     
		
        Map<String, String[]> env = new HashMap<String, String[]>();
        
        String userName = opts.getOptString("user", null);
        String password = opts.getOptString("password", null);
        if( userName != null || password != null )
        
	        env.put(JMXConnector.CREDENTIALS, new String[] { userName, password });
			
		
		JMXConnector jmxc = JMXConnectorFactory.connect(url, env );
		return jmxc;
		
		
	}
	
	protected ObjectName	objectName( String  s ) throws MalformedObjectNameException, NullPointerException
	{
		ObjectName name = new ObjectName(s);
		return name ;
		
	}
	
	
	
	protected void closeWriter() throws XMLStreamException {
		mWriter.flush();
		mWriter.close();
	}


	protected void endDocument() throws XMLStreamException {
		mWriter.writeEndDocument();
	}


	protected void endElement() throws XMLStreamException {
		mWriter.writeEndElement();
	}


	protected void startElement(String localname) throws XMLStreamException {
		mWriter.writeStartElement(localname);
	}


	protected void startDocument() throws XMLStreamException {
		mWriter.writeStartDocument();
	}
	
	protected	  void attribute( String localName , String value ) throws XMLStreamException
	{
		mWriter.writeAttribute(localName, value);
	}
	
	protected	  void characters( String value ) throws XMLStreamException
	{
		mWriter.writeCharacters(value);
	}
	
}

//
//
// Copyright (C) 2008,2009,2010,2011,2012 , David A. Lee.
//
// The contents of this file are subject to the "Simplified BSD License" (the
// "License");
// you may not use this file except in compliance with the License. You may
// obtain a copy of the
// License at http://www.opensource.org/licenses/bsd-license.php
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is David A. Lee
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): none.
//
