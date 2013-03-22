/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.jmx;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.management.Descriptor;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.xml.stream.XMLStreamException;

import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XValue;
import org.xmlsh.jmx.util.JMXCommand;
import org.xmlsh.sh.shell.SerializeOpts;

public class list extends JMXCommand {
	private boolean bListAttributes = false ;

	@Override
	public int run(List<XValue> args) throws Exception {
		
		
		Options opts = new Options(sCOMMON_OPTS + ",d=domains,n=names,a=attributes",SerializeOpts.getOptionDefs());
		opts.parse(args);
		mSerializeOpts = this.getSerializeOpts(opts);
		bListAttributes = opts.hasOpt("a");
		
		args = opts.getRemainingArgs();
		

		
		boolean bDomains = opts.hasOpt("d") ? true : false ;
		
		
		
		
		OutputPort out = this.getStdout();
		mWriter = out.asXMLStreamWriter( mSerializeOpts  );
		

		JMXConnector jmx = getConnector(opts);
		
		try {
			MBeanServerConnection mbean = jmx.getMBeanServerConnection();
			
			startDocument();
			
			if( bDomains ){
				
				mWriter.writeStartElement("","domains",kJMX_NS);
				mWriter.writeDefaultNamespace(kJMX_NS);
				
				
				
				String[] domains = mbean.getDomains();
				writeDomains(domains);
				
			} else
			{
				
				
				mWriter.writeStartElement("","names",kJMX_NS);
				mWriter.writeDefaultNamespace(kJMX_NS);
				
				
				Set<ObjectName> names = mbean.queryNames(null,null);
				TreeSet<ObjectName> sortNames = new TreeSet<ObjectName>(names);
				for( ObjectName name : sortNames ){
					
					startElement("name");
					attribute( "domain" , name.getDomain() );
					
					Hashtable<String,String> props = name.getKeyPropertyList();
					writeProperties(props);
					
					if( bListAttributes ){
						MBeanInfo info = mbean.getMBeanInfo(name);
						MBeanAttributeInfo[] attrs = info.getAttributes();
						writeAttributes( attrs );
					}
					
					endElement();
					
				}
				endElement();
			}
			
			endDocument();;
			closeWriter();	
			out.release();
		} finally {
			jmx.close();
		}
		return 0;
		
		
		
	}

	private void writeAttributes(MBeanAttributeInfo[] attrs) throws XMLStreamException {
		startElement( "attributes");
		for( MBeanAttributeInfo attrInfo : attrs ){
			startElement("attribute");
			attribute("name" , attrInfo.getName() );
			attribute("type" , attrInfo.getType() );
			attribute("description",attrInfo.getDescription() );
			Descriptor descriptor  = attrInfo.getDescriptor();
			if( descriptor != null){
				startElement("descriptors");
				for( String fname : descriptor.getFieldNames() ){
					startElement("descriptor");
					attribute("name" , fname );
					attribute("value" , descriptor.getFieldValue(fname).toString() );
					endElement();
				}
				endElement();
			}
			endElement();
		}
		endElement();
	}

	private void writeDomains(String[] domains) throws XMLStreamException {
		Arrays.sort(domains);
		for( String s : domains ){
			startElement("domain");
			attribute("name" , s );
			endElement();
		}
	}

	private void writeProperties(Hashtable<String, String> props) throws XMLStreamException {
		startElement("properties");
		for( Entry<String,String> entry : props.entrySet() ){
			startElement("property");
			attribute("key" , entry.getKey() );
			attribute("value" , entry.getValue() );
			endElement();
		}
		endElement();
	}

}



//
//
//Copyright (C) 2008,2009,2010,2011,2012 David A. Lee.
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
