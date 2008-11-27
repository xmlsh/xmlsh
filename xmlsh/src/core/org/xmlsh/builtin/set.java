/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.builtin;

import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.helpers.AttributesImpl;
import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.XEnvironment;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.util.Util;

public class set extends BuiltinCommand {

	static final String sDocRoot = "env";
	public int run(   List<XValue> args ) throws Exception {
		if( args == null || args.size() == 0 )	
			printVars(  );
		else
		
			mShell.setArgs(args);
		return 0;
	}

	private void printVars() throws Exception {
		

		XEnvironment env = mShell.getEnv();
		
		OutputStream stdout = env.getStdout().asOutputStream();
	      
		TransformerHandler hd = Util.getTransformerHander(stdout);

		hd.startDocument();
		
		AttributesImpl attrs = new AttributesImpl();

		hd.startElement("", sDocRoot,sDocRoot,attrs);
		
		
		
		Collection<String> names = env.getVarNames();
		for( String name : names ){
			XVariable var = env.getVar(name);
			var.serialize(hd);
			
		}
		hd.endElement("",sDocRoot,sDocRoot);
		hd.endDocument();
		
		
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
