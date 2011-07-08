/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands.internal;

import java.io.OutputStream;
import java.util.List;

import javanet.staxutils.ContentHandlerToXMLStreamWriter;

import javax.xml.stream.XMLStreamWriter;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Serializer;
import nu.xom.converters.SAXConverter;
import nu.xom.xinclude.XIncluder;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;

public class xinclude extends XCommand {

	
	
	
	

	@Override
	public int run(List<XValue> args) throws Exception {
		
		

		Options opts = new Options( "xs=xomserialize",SerializeOpts.getOptionDefs() );
		opts.parse(args);
		args = opts.getRemainingArgs();
		boolean xs = opts.hasOpt("xs");
		
		InputPort stdin = null;
		if( args.size() > 0 )
			stdin = getInput( args.get(0));
		else
			stdin = getStdin();
		if( stdin == null )
			throw new InvalidArgumentException("Cannot open input");
		try {
			
			SerializeOpts sopts = getSerializeOpts(opts);
			Builder builder = new Builder();

			Document input = builder.build( stdin.asInputStream(sopts) , stdin.getSystemId() );
			XIncluder.resolveInPlace(input, builder);
			
			
			
			
			
			OutputPort stdout = getStdout();
			OutputStream os = stdout.asOutputStream(sopts);
			
			// XOM Serialization 
			if( xs ){
			
				Serializer ser = new Serializer(os , sopts.getEncoding());
				ser.write(input);
				os.close();
				
			
			} else {
				
				XMLStreamWriter w = stdout.asXMLStreamWriter(sopts);
				
				ContentHandlerToXMLStreamWriter	handler = new ContentHandlerToXMLStreamWriter(w);
				
				SAXConverter sax = new SAXConverter( handler );
				sax.convert(input);
				
				w.flush();
				w.close();
				stdout.writeSequenceTerminator(sopts);
			}
			stdout.release();
		} 
		finally {
			
			stdin.close();
		}
		return 0;
		
		
	}

}



//
//
//Copyright (C) 2008,2009,2010,2011 , David A. Lee.
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
