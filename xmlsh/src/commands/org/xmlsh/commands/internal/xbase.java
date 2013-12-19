/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands.internal;

import java.io.PrintWriter;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;


public class xbase extends XCommand {


	private static QName mBaseQName = new QName("http://www.w3.org/XML/1998/namespace","base" , "xml");
	

	
	
	@Override
	public int run( List<XValue> args )
	throws Exception 
	{
		
		Options opts = new Options( SerializeOpts.getOptionDefs()  );
		opts.parse(args);
		args = opts.getRemainingArgs();
		
		

		SerializeOpts sopts = getSerializeOpts(opts);

		XMLStreamReader reader = 
			args.isEmpty() ?	getStdin().asXMLStreamReader(sopts) :
			getInput(args.get(0)).asXMLStreamReader(sopts);
				
		
		do {
		
			if( reader.getEventType() == XMLEvent.START_ELEMENT )
				break;
			reader.next();
			
		} while( reader.hasNext() );
		
	
			
	
		Location loc = reader.getLocation();
		String sSystemID  = loc.getSystemId();
		String sBase = reader.getAttributeValue(mBaseQName.getNamespaceURI(), mBaseQName.getLocalPart());
		if( Util.isEmpty(sBase))
			sBase = sSystemID ;
		
		
		
		reader.close();
		
		PrintWriter out = getStdout().asPrintWriter(sopts);
		
		out.println(sBase );
	
		out.flush();
		
		
		return 0;

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
