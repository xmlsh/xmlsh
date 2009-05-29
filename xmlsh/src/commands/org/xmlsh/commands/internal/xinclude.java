/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands.internal;

import java.util.List;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.AugmentedSource;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XdmNode;
import org.xml.sax.XMLReader;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

public class xinclude extends XCommand {

	
	
	

	@Override
	public int run(List<XValue> args) throws Exception {
		
		InputPort stdin = null;
		if( args.size() > 0 )
			stdin = getInput( args.get(0));
		else
			stdin = getStdin();
		if( stdin == null )
			throw new InvalidArgumentException("Cannot open input");
		try {
			
			SerializeOpts opts = getSerializeOpts();
			
			
			Processor processor = Shell.getProcessor();
			
			
			// Use a reader manually created so that it doesnt get stuck in the pool
			// This is to work around a Saxon 9 bug (5/26/2009) where xinclude settings "stick" in the Configurations
			// even if made to augmented sources.
			
			XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
			
			// Force conversion of stdin to a byte stream , xinclude is done at the parsing level
			Source ssource = new StreamSource(stdin.asInputStream(opts ));
			ssource.setSystemId( stdin.getSystemId());
			AugmentedSource source = AugmentedSource.makeAugmentedSource(ssource);
			source.setXIncludeAware(true);
			source.setXMLReader(reader);
			XdmNode node = processor.newDocumentBuilder().build(source);

			OutputPort stdout = getStdout();
			Util.writeXdmValue(node, stdout.asDestination(opts));
	
			stdout.writeSequenceTerminator();
		} 
		finally {
			
			stdin.close();
		}
		return 0;
		
		
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
