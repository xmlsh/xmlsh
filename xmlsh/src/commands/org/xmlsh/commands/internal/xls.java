/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.commands.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.types.XFile;
import org.xmlsh.util.Util;

public class xls extends XCommand {

	
	private boolean opt_a = false ;
	private boolean opt_R = false ;
	private boolean opt_l = false ;
	public int run(  List<XValue> args  )	throws Exception
	{
		Options opts = new Options("a=all,l=long,R=recurse",args);
		opts.parse();
		args = opts.getRemainingArgs();
		
		

	      
		OutputPort stdout = getStdout();
		XMLStreamWriter writer = stdout.asXMLStreamWriter(getSerializeOpts());
		writer.writeStartDocument();
		
		Attributes attrs = new AttributesImpl();
		String sDocRoot = "dir";
		writer.writeStartElement(sDocRoot);
		
		
		if( args == null )
			args = new ArrayList<XValue>();
		if( args.size() == 0 )
			args.add(new XValue(""));
		
		opt_l = opts.hasOpt("l");
		opt_a = opts.hasOpt("a");
		opt_R = opts.hasOpt("R");
		for( XValue arg : args ){
			
			// Must go to Shell API to get raw files
			File dir = getEnv().getShell().getFile(arg.toString());
			ls(writer, dir , true);
		}
		writer.writeEndElement();
		writer.writeEndDocument();
		stdout.writeSequenceTerminator();
		
		
		return 0;
	}

	private void ls(XMLStreamWriter writer, File dir, boolean top ) throws XMLStreamException {
		if( !dir.isDirectory() ){

			new XFile(dir).serialize(writer, opt_l,true);
		} else {
			
			if( ! top )
				new XFile(dir).serialize(writer, opt_l,false);
				
			
			if( top || opt_R ){
				File [] files =  dir.listFiles();
				
				
				Util.sortFiles(files);
				
				for( File f : files ){
					
					if( ! opt_a && f.getName().startsWith("."))
						continue;
					
					ls( writer  , f , false  );
	
				}
			}
			if( ! top )
				writer.writeEndElement();
		}
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
