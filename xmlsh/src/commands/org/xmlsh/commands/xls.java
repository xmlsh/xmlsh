/**
 * $Id: $
 * $DateTime: $
 *
 */

package org.xmlsh.commands;

import java.io.File;
import java.io.OutputStream;

import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XEnvironment;
import org.xmlsh.core.XValue;
import org.xmlsh.types.XFile;
import org.xmlsh.util.Util;
import org.xmlsh.util.XMLException;

public class xls extends XCommand {

	/**
	 * @param args
	 * @throws XMLException 
	 */
	public static void main(String[] args) throws Exception {
		
		xls cmd = new xls();

		cmd.run( args );
		
	}

	public int run( XValue args[] , XEnvironment env )	throws Exception
	{
		
		OutputStream stdout = env.getStdout();
	      
		TransformerHandler hd = Util.getTransformerHander(stdout);

		hd.startDocument();
		
		Attributes attrs = new AttributesImpl();
		String sDocRoot = "dir";
		hd.startElement("", sDocRoot,sDocRoot,attrs);
		
		
		if( args.length == 0)
			args = new XValue[] { new XValue("") };
		
		
		for( XValue arg : args ){
			
			File dir = env.getShell().getFile(arg.toString());
			if( !dir.isDirectory() ){
				new XFile(dir).serialize(hd);
			} else {
	
				File [] files =  dir.listFiles();
				for( File f : files ){
		
					
		
					new XFile(f ).serialize(hd);
					
		
					
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
