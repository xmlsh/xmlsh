/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.builtin;

import java.io.File;
import java.io.OutputStream;
import java.util.List;

import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.helpers.AttributesImpl;
import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.CommandFactory;
import org.xmlsh.core.ICommand;
import org.xmlsh.core.Options;
import org.xmlsh.core.XValue;
import org.xmlsh.core.Options.OptionValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

public class xwhich extends BuiltinCommand {

	
	private static final String typenames[] = new String[] {
		"builtin" , "internal" , "user" , "external" , "script"

	};
	
	public int run( Shell shell,String cmd ,  List<XValue> args ) throws Exception {
			
		Options opts = new Options( "n" , args );
		opts.parse();
		
		boolean bNoWrite = opts.hasOpt("n");

		
		
		List<XValue> xvargs = Util.expandSequences(opts.getRemainingArgs());
		
		if( xvargs.size() < 1  ){
			shell.printErr("usage: " + cmd + " command ...");
			return 1;
		}
		
		OutputStream stdout = shell.getEnv().getStdout();
		final  String sDocRoot = cmd;
	      
		TransformerHandler hd = null;
		
		
		if( !bNoWrite ){
			hd = Util.getTransformerHander(stdout);
	
			hd.startDocument();
			
			AttributesImpl attrs = new AttributesImpl();
	
			hd.startElement("", sDocRoot,sDocRoot,attrs);
		}
		
		int bad = 0;

		for( XValue xname : xvargs ){
			
			String name = xname.toString();
			ICommand command = CommandFactory.getInstance().getCommand(shell , name );
			if( command != null ){
				
				if( ! bNoWrite ){
					AttributesImpl atts = new AttributesImpl();
					
					final  String sCmd = "command";
					final	String sName = "name";
					final 	String sType = "type";
					final  String sPath = "path";
					
					atts.addAttribute("", sName, sName , "CDATA", name );
					
		
					
					String type = typenames[command.getType().ordinal()];
					
					atts.addAttribute("", sType, sType, "CDATA", type );
					
					File file = command.getFile();
					if( file != null )
						atts.addAttribute("", sPath, sPath , "CDATA", file.getCanonicalPath() );
		
					hd.startElement("", sCmd, sCmd, atts);
		
					hd.endElement("", sCmd, sCmd);
				}
			} else
				bad++;
		
		}
		if( ! bNoWrite ){
			hd.endElement("",sDocRoot,sDocRoot);
			hd.endDocument();
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
