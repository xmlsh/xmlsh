/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.builtin;

import java.io.File;
import java.util.List;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.helpers.AttributesImpl;
import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.CommandFactory;
import org.xmlsh.core.ICommand;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

public class xwhich extends BuiltinCommand {

	
	private static final String typenames[] = new String[] {
		"builtin" , "internal" , "user" , "external" , "script" , "function"

	};
	
	public int run(   List<XValue> args ) throws Exception {
			
		Options opts = new Options( "n" , args );
		opts.parse();
		
		boolean bNoWrite = opts.hasOpt("n");

		
		
		List<XValue> xvargs = Util.expandSequences(opts.getRemainingArgs());
		
		if( xvargs.size() < 1  ){
			mShell.printErr("usage: " + mName + " command ...");
			return 1;
		}
		
		final  String sDocRoot =mName;
	      
		XMLStreamWriter 	out = null ;
		OutputPort stdout = mShell.getEnv().getStdout();	
		
		if( !bNoWrite ){

			out = stdout.asXMLStreamWriter(getSerializeOpts());
			out.writeStartDocument();
			out.writeStartElement(sDocRoot);
			
		}
		
		int bad = 0;

		for( XValue xname : xvargs ){
			
			String name = xname.toString();
			ICommand command = CommandFactory.getInstance().getCommand(mShell , name );
			if( command != null ){
				
				if( ! bNoWrite ){
			
					
					final  String sCmd = "command";
					final	String sName = "name";
					final 	String sType = "type";
					final  String sPath = "path";
					final String sModule = "module";
					
					out.writeStartElement(sCmd);
					out.writeAttribute(sName, name);
					
				
					
					String type = typenames[command.getType().ordinal()];
					out.writeAttribute(sType, type);
				
					File file = command.getFile();
					if( file != null )
						out.writeAttribute(sPath, file.getCanonicalPath() );
					String module = command.getModule();
					if( module != null )
						out.writeAttribute(sModule, module);
					
				}
			} else
				bad++;
		
		}
		if( ! bNoWrite ){
			out.writeEndElement();
			out.writeEndDocument();
			stdout.writeSequenceTerminator();
		}
		
		
		return bad;
		
			
				
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
