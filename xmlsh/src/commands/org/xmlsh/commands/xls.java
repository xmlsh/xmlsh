/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.types.XFile;
import org.xmlsh.util.Util;

public class xls extends XCommand {

	

	public int run(  List<XValue> args  )	throws Exception
	{
		Options opts = new Options("l",args);
		opts.parse();
		args = opts.getRemainingArgs();
		
		

	      
		TransformerHandler hd = getStdout().asTransformerHandler();

		hd.startDocument();
		
		Attributes attrs = new AttributesImpl();
		String sDocRoot = "dir";
		hd.startElement("", sDocRoot,sDocRoot,attrs);
		
		
		if( args == null )
			args = new ArrayList<XValue>();
		if( args.size() == 0 )
			args.add(new XValue(""));
		
		boolean longMode = opts.hasOpt("l");
		for( XValue arg : args ){
			
			// Must go to Shell API to get raw files
			File dir = getEnv().getShell().getFile(arg.toString());
			if( !dir.isDirectory() ){

				new XFile(dir).serialize(hd, longMode);
			} else {
	
				File [] files =  dir.listFiles();
				Util.sortFiles(files);
				for( File f : files ){
		
					
		
					new XFile(f ).serialize(hd,longMode);
					
		
					
				}
			}
		}
		hd.endElement("", sDocRoot,sDocRoot);
		hd.endDocument();
		
		return 0;
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
