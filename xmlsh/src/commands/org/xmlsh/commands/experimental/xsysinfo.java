/**
 * $Id: colon.java 21 2008-07-04 08:33:47Z daldei $
 * $Date: 2008-07-04 04:33:47 -0400 (Fri, 04 Jul 2008) $
 *
 */

package org.xmlsh.commands.experimental;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;

public class xsysinfo extends XCommand {

	
	public int run(  List<XValue> args ) throws Exception {
	
		Options opts = new Options(SerializeOpts.getOptionDefs() );
		opts.parse(args);
		// args = opts.getRemainingArgs();
	
		OutputPort stdout = getEnv().getStdout();
		SerializeOpts serializeOpts = getSerializeOpts(opts);
		XMLStreamWriter writer = stdout.asXMLStreamWriter(serializeOpts);
	
		writer.writeStartDocument();

		
		writer.writeStartElement("systeminfo");
		writeNetwork( writer );
		writer.writeEndElement();
		writer.writeEndDocument();
		stdout.writeSequenceTerminator(serializeOpts);
		
		return 0;
		
		}

	private void writeNetwork(XMLStreamWriter writer) throws XMLStreamException, SocketException {
		
		writer.writeStartElement("network");
		Enumeration<NetworkInterface> all = NetworkInterface.getNetworkInterfaces();
		while( all.hasMoreElements() ){
			NetworkInterface net = all.nextElement();
			writer.writeStartElement("interface");
			writer.writeAttribute("display_name", net.getDisplayName());
			writer.writeAttribute("name", net.getName());
			writer.writeAttribute("mtu", String.valueOf(net.getMTU()));
			Enumeration<InetAddress> addrs = net.getInetAddresses();
			while( addrs.hasMoreElements()){
				InetAddress addr = addrs.nextElement();
				writer.writeStartElement("inet");
				writer.writeAttribute("address" , addr.getHostAddress());
				writer.writeAttribute("name", addr.getHostName());
				writer.writeAttribute("loopback", addr.isLoopbackAddress() ? "true" : "false");
				
				
				 
				writer.writeEndElement();
				
			}
			writer.writeEndElement();
		}
		writer.writeEndElement();
		
	}



}
//
//
//Copyright (C) 2008,2009,2010 , David A. Lee.
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
