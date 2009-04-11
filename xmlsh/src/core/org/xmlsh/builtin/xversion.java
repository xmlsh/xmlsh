/**
 * $Id: colon.java 21 2008-07-04 08:33:47Z daldei $
 * $Date: 2008-07-04 04:33:47 -0400 (Fri, 04 Jul 2008) $
 *
 */

package org.xmlsh.builtin;

import java.util.List;

import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.helpers.AttributesImpl;
import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Version;

public class xversion extends BuiltinCommand {

	
	public int run(  List<XValue> args ) throws Exception {
	
	      
		OutputPort stdout = mShell.getEnv().getStdout();
		TransformerHandler hd = stdout.asTransformerHandler(getSerializeOpts());

	
		hd.startDocument();
			
		final String sBuild = "build";
		final String sRelease = "relsase";
		final String sDocRoot = "version";
	
		AttributesImpl attrs = new AttributesImpl();
		attrs.addAttribute("", sBuild, sBuild , "CDATA", Version.getBuildDate() );
		attrs.addAttribute("", sRelease, sRelease , "CDATA", Version.getRelease() );
	
		
		hd.startElement("", sDocRoot,sDocRoot,attrs);
		hd.endElement("", sDocRoot, sDocRoot);
		hd.endDocument();
		stdout.writeSequenceTerminator();
		
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
